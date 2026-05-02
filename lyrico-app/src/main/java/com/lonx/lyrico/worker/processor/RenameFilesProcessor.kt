package com.lonx.lyrico.worker.processor

import com.lonx.audiotag.model.AudioTagData
import com.lonx.lyrico.data.model.CharacterMappingRule
import com.lonx.lyrico.data.model.entity.BatchTaskEntity
import com.lonx.lyrico.data.model.entity.BatchTaskItemEntity
import com.lonx.lyrico.data.repository.SongRepository
import com.lonx.lyrico.utils.ConflictResolver
import com.lonx.lyrico.utils.FileNameSanitizer
import com.lonx.lyrico.utils.FormatParser
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class RenameFilesTaskConfig(
    val renameFormat: String,
    val characterMappingRules: List<CharacterMappingRule> = emptyList()
)

@Serializable
data class RenameFilesTaskResult(
    val originalPath: String? = null,
    val newPath: String
)

class RenameFilesProcessor(
    private val songRepository: SongRepository
) : BatchTaskProcessor {

    override suspend fun process(
        task: BatchTaskEntity,
        item: BatchTaskItemEntity,
        onProgress: suspend (Float) -> Unit
    ): BatchTaskProcessResult {
        val config = task.configJson?.let {
            kotlinx.serialization.json.Json.decodeFromString<RenameFilesTaskConfig>(it)
        } ?: throw BatchTaskSkippedException("No config")

        val song = songRepository.getSongByUri(item.songUri)
            ?: throw BatchTaskSkippedException("Song not found")

        val originalPath = song.filePath
        val file = File(originalPath)
        if (!file.exists()) {
            throw BatchTaskSkippedException("File not found")
        }

        val tagData = convertToAudioTagData(song)

        val tokens = FormatParser.parseFormat(config.renameFormat)
        var newFileName = FormatParser.buildFileName(tokens, tagData)
        newFileName = FileNameSanitizer.sanitize(newFileName, config.characterMappingRules)

        if (newFileName.isEmpty()) {
            newFileName = file.nameWithoutExtension
        }

        val extension = file.extension
        if (extension.isNotEmpty()) {
            newFileName = "$newFileName.$extension"
        }

        val parentDir = file.parent ?: ""
        val newPath = if (parentDir.isNotEmpty()) {
            File(parentDir, newFileName).absolutePath
        } else {
            newFileName
        }

        if (newPath == originalPath) {
            throw BatchTaskSkippedException("Same file name")
        }

        val newFile = File(newPath)
        newFile.parentFile?.mkdirs()

        if (newFile.exists() && newPath != originalPath) {
            val resolvedPath = ConflictResolver.resolveConflict(newPath, setOf())
            val resolvedFile = File(resolvedPath)
            if (file.renameTo(resolvedFile)) {
                return BatchTaskProcessResult(
                    resultJson = kotlinx.serialization.json.Json.encodeToString(
                        RenameFilesTaskResult.serializer(),
                        RenameFilesTaskResult(
                            originalPath = originalPath,
                            newPath = resolvedPath
                        )
                    ),
                    updatedFilePath = resolvedPath,
                    updatedFileName = resolvedFile.name
                )
            } else {
                throw Exception("Failed to rename (conflict)")
            }
        }

        if (file.renameTo(newFile)) {
            return BatchTaskProcessResult(
                resultJson = kotlinx.serialization.json.Json.encodeToString(
                    RenameFilesTaskResult.serializer(),
                    RenameFilesTaskResult(
                        originalPath = originalPath,
                        newPath = newPath
                    )
                ),
                updatedFilePath = newPath,
                updatedFileName = newFile.name
            )
        } else {
            throw Exception("Failed to rename file")
        }
    }

    private fun convertToAudioTagData(song: com.lonx.lyrico.data.model.entity.SongEntity): AudioTagData {
        return AudioTagData(
            title = song.title,
            artist = song.artist,
            album = song.album,
            albumArtist = song.albumArtist,
            genre = song.genre,
            date = song.date,
            trackNumber = song.trackerNumber,
            discNumber = song.discNumber,
            composer = song.composer,
            lyricist = song.lyricist,
            comment = song.comment,
            lyrics = song.lyrics,
            copyright = song.copyright,
            rating = song.rating,
            fileName = song.fileName,
            durationMilliseconds = song.durationMilliseconds,
            bitrate = song.bitrate,
            sampleRate = song.sampleRate,
            channels = song.channels
        )
    }
}
