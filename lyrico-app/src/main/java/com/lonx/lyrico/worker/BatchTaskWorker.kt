package com.lonx.lyrico.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.lonx.lyrico.R
import com.lonx.lyrico.data.model.BatchTaskType
import com.lonx.lyrico.data.repository.BatchTaskRepository
import com.lonx.lyrico.worker.processor.BatchTaskProcessorFactory
import com.lonx.lyrico.worker.processor.BatchTaskSkippedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicInteger

class BatchTaskWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val taskRepository: BatchTaskRepository by inject()
    private val processorFactory: BatchTaskProcessorFactory by inject()

    private val taskId: String = inputData.getString(KEY_TASK_ID) ?: ""

    override suspend fun doWork(): Result {
        if (taskId.isEmpty()) return Result.failure()

        val wakeLock = acquireWakeLock()
        BatchTaskNotification.ensureChannel(applicationContext)

        try {
            val task = taskRepository.getTask(taskId) ?: return Result.failure()
            val title = getTaskTitle(task.type)
            val total = task.total

            setForeground(createForegroundInfo(title, 0, total))

            taskRepository.markRunning(taskId)

            val processor = processorFactory.create(task.type)
            val items = taskRepository.getPendingItems(taskId)

            if (items.isEmpty()) {
                taskRepository.markSucceeded(taskId)
                return Result.success()
            }

            val itemTotal = items.size
            val concurrency = parseConcurrency(task.configJson, task.type)
            val semaphore = Semaphore(concurrency)
            val processedCount = AtomicInteger(0)
            val successCount = AtomicInteger(0)
            val failureCount = AtomicInteger(0)

            try {
                coroutineScope {
                    val jobs = items.map { item ->
                        async(Dispatchers.IO) {
                            semaphore.withPermit {
                                if (isStopped) return@async
                                try {
                                    taskRepository.markItemRunning(item.itemId)
                                    val result = processor.process(task, item) { progress ->
                                        taskRepository.updateItemProgress(item.itemId, progress)
                                    }
                                    if (result.updatedFilePath != null && result.updatedFileName != null) {
                                        taskRepository.updateItemFileInfo(
                                            itemId = item.itemId,
                                            filePath = result.updatedFilePath,
                                            fileName = result.updatedFileName
                                        )
                                    }
                                    taskRepository.markItemSucceeded(item.itemId, result.resultJson)
                                    successCount.incrementAndGet()
                                } catch (e: BatchTaskSkippedException) {
                                    taskRepository.markItemSkipped(item.itemId, e.message)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Item processing failed: ${item.fileName}", e)
                                    taskRepository.markItemFailed(item.itemId, e.message)
                                    failureCount.incrementAndGet()
                                } finally {
                                    val current = processedCount.incrementAndGet()
                                    taskRepository.updateProgressFromItems(taskId, item.fileName)
                                    try {
                                        setForeground(
                                            createForegroundInfo(title, current, itemTotal)
                                        )
                                    } catch (e: Exception) {
                                        Log.w(TAG, "Failed to update foreground notification", e)
                                    }
                                }
                            }
                        }
                    }
                    jobs.awaitAll()
                }
            } finally {
                withContext(NonCancellable) {
                    if (isStopped) {
                        taskRepository.markCancelled(taskId)
                    } else {
                        taskRepository.markSucceeded(taskId)
                    }
                }
            }

            return Result.success()
        } finally {
            releaseWakeLock(wakeLock)
        }
    }

    private fun acquireWakeLock(): PowerManager.WakeLock? {
        return try {
            val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "$WAKE_LOCK_TAG_PREFIX:$taskId"
            ).apply {
                setReferenceCounted(false)
                acquire(WAKE_LOCK_TIMEOUT_MILLIS)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire wake lock", e)
            null
        }
    }

    private fun releaseWakeLock(wakeLock: PowerManager.WakeLock?) {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release wake lock", e)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo("Batch Task", 0, 0)
    }

    private fun createForegroundInfo(
        title: String,
        current: Int,
        total: Int
    ): ForegroundInfo {
        val notification = BatchTaskNotification.buildRunningNotification(
            context = applicationContext,
            taskId = taskId,
            title = title,
            currentFile = null,
            current = current,
            total = total
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                BatchTaskNotification.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                BatchTaskNotification.NOTIFICATION_ID,
                notification
            )
        }
    }

    private fun getTaskTitle(type: BatchTaskType): String {
        return when (type) {
            BatchTaskType.MATCH_METADATA -> applicationContext.getString(R.string.batch_task_match_tags)
            BatchTaskType.EDIT_TAGS -> applicationContext.getString(R.string.batch_task_edit_tags)
            BatchTaskType.RENAME_FILES -> applicationContext.getString(R.string.batch_task_rename_files)
            BatchTaskType.CONVERT_LYRICS_FORMAT -> applicationContext.getString(R.string.batch_task_convert_lyrics_format)
            BatchTaskType.SCAN_REPLAY_GAIN -> applicationContext.getString(R.string.batch_task_scan_replay_gain)
        }
    }

    private fun parseConcurrency(configJson: String?, type: BatchTaskType): Int {
        if (configJson == null) return 1
        return try {
            val obj = kotlinx.serialization.json.Json.parseToJsonElement(configJson).jsonObject
            val concurrency = obj["concurrency"]?.jsonPrimitive?.int
            concurrency?.coerceIn(1, 5) ?: 3
        } catch (e: Exception) {
            3
        }
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        private const val TAG = "BatchTaskWorker"
        private const val WAKE_LOCK_TAG_PREFIX = "Lyrico:BatchTask"
        private const val WAKE_LOCK_TIMEOUT_MILLIS = 6 * 60 * 60 * 1000L
    }
}
