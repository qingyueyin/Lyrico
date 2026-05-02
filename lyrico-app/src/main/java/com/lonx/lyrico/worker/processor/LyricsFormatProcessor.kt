package com.lonx.lyrico.worker.processor

import com.lonx.audiotag.model.AudioTagData
import com.lonx.lyrico.data.model.BatchTaskType
import com.lonx.lyrico.data.model.ConversionMode
import com.lonx.lyrico.data.model.LyricFormat
import com.lonx.lyrico.data.model.LyricRenderConfig
import com.lonx.lyrico.data.model.entity.BatchTaskEntity
import com.lonx.lyrico.data.model.entity.BatchTaskItemEntity
import com.lonx.lyrico.data.repository.SongRepository
import com.lonx.lyrico.utils.LyricDecoder
import com.lonx.lyrico.utils.LyricEncoder
import com.lonx.lyrico.viewmodel.LyricsFormatConfig
import kotlinx.serialization.json.Json

class LyricsFormatProcessor(
    private val songRepository: SongRepository
) : BatchTaskProcessor {

    override suspend fun process(
        task: BatchTaskEntity,
        item: BatchTaskItemEntity,
        onProgress: suspend (Float) -> Unit
    ): BatchTaskProcessResult {
        val config = task.configJson?.let {
            Json.decodeFromString<LyricsFormatConfig>(it)
        } ?: throw BatchTaskSkippedException("No config")

        val song = songRepository.getSongByUri(item.songUri)
            ?: throw BatchTaskSkippedException("Song not found")

        val lyrics = song.lyrics
        if (lyrics.isNullOrBlank()) {
            throw BatchTaskSkippedException("No lyrics")
        }

        val currentFormat = LyricDecoder.detectFormat(lyrics)
        if (currentFormat == config.targetFormat) {
            throw BatchTaskSkippedException("Already target format")
        }

        val convertedLyrics = convertLyricsFormat(lyrics, config.targetFormat)
        if (convertedLyrics == null || convertedLyrics == lyrics) {
            throw Exception("Conversion failed")
        }

        val success = songRepository.patchAudioTags(
            item.songUri,
            AudioTagData(lyrics = convertedLyrics)
        )

        if (!success) {
            throw Exception("Write failed")
        }

        return BatchTaskProcessResult()
    }

    private fun convertLyricsFormat(lyrics: String, targetFormat: LyricFormat): String? {
        return try {
            val lyricsResult = LyricDecoder.decode(lyrics) ?: return null
            if (lyricsResult.original.isEmpty()) return null
            val config = LyricRenderConfig(
                format = targetFormat,
                conversionMode = ConversionMode.NONE,
                showTranslation = lyricsResult.translated != null,
                showRomanization = lyricsResult.romanization != null,
                removeEmptyLines = true,
                onlyTranslationIfAvailable = false
            )
            LyricEncoder.encode(lyricsResult, config)
        } catch (e: Exception) {
            null
        }
    }
}
