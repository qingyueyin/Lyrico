package com.lonx.lyrico.viewmodel

import com.lonx.lyrico.data.model.BatchTaskStatus
import com.lonx.lyrico.data.model.BatchTaskType
import com.lonx.lyrico.data.model.entity.BatchTaskEntity

data class BatchTaskUiState(
    val taskId: String,
    val type: BatchTaskType,
    val status: BatchTaskStatus,
    val current: Int,
    val total: Int,
    val successCount: Int,
    val failureCount: Int,
    val skippedCount: Int,
    val currentFile: String?,
    val canCancel: Boolean,
    val isRunning: Boolean
)

fun BatchTaskEntity.toUiState() = BatchTaskUiState(
    taskId = taskId,
    type = type,
    status = status,
    current = current,
    total = total,
    successCount = successCount,
    failureCount = failureCount,
    skippedCount = skippedCount,
    currentFile = currentFile,
    canCancel = status == BatchTaskStatus.RUNNING || status == BatchTaskStatus.QUEUED,
    isRunning = status == BatchTaskStatus.RUNNING
)
