package com.lonx.lyrics.utils

import com.lonx.lyrics.model.LyricsData
import com.lonx.lyrics.model.LyricsLine
import com.lonx.lyrics.model.LyricsResult
import com.lonx.lyrics.model.LyricsWord
import com.lonx.lyrics.model.isWordByWord

object SodaParser {
    private val SODA_LINE_PATTERN = Regex("""^\[(\d+),(\d+)](.*)$""")
    private val SODA_WORD_PATTERN = Regex("""<(\d+),(\d+),\d+>([^<]*)""")
    private val TAG_PATTERN = Regex("""^\[([A-Za-z][\w-]*):([^]]*)]$""")
    private val LRC_TIME_PATTERN = Regex("""([\[<])(\d{1,}):(\d{2})(?:[.:](\d{1,3}))?([>\]])""")

    fun parse(lyricsData: LyricsData): LyricsResult {
        val rawOriginal = lyricsData.original.orEmpty()
        val rawTranslated = lyricsData.translated.orEmpty()
        val rawRomanization = lyricsData.romanization.orEmpty()
        val tags = extractTags(rawOriginal)

        val original = parseTimedLyrics(rawOriginal)
        val translated = rawTranslated
            .takeIf { it.isNotBlank() }
            ?.let { parseTimedLyrics(it) }
            ?.ifEmpty { null }
        val romanization = rawRomanization
            .takeIf { it.isNotBlank() }
            ?.let { parseTimedLyrics(it) }
            ?.ifEmpty { null }

        val (normalizedOriginal, normalizedTranslated, normalizedRomanization) =
            separateTracks(original, translated, romanization)

        val rawVerbatimLrc = when {
            isSodaFormat(rawOriginal) -> encodeVerbatimLrc(normalizedOriginal)
            isVerbatimLrc(rawOriginal) -> rawOriginal
            else -> ""
        }
        val rawPlainLrc = rawOriginal.takeIf { isPlainLrc(it) }.orEmpty()
        val rawEnhancedLrc = rawOriginal.takeIf { isEnhancedLrc(it) }.orEmpty()

        return LyricsResult(
            tags = tags,
            original = normalizedOriginal,
            translated = normalizedTranslated,
            romanization = normalizedRomanization,
            isWordByWord = normalizedOriginal.isWordByWord(),
            rawPlainLrc = rawPlainLrc,
            rawVerbatimLrc = rawVerbatimLrc,
            rawEnhancedLrc = rawEnhancedLrc
        )
    }

    private fun extractTags(raw: String): Map<String, String> {
        return raw.lines().mapNotNull { line ->
            val match = TAG_PATTERN.matchEntire(line.trim()) ?: return@mapNotNull null
            match.groupValues[1] to match.groupValues[2].trim()
        }.toMap()
    }

    private fun parseTimedLyrics(raw: String): List<LyricsLine> {
        return when {
            isSodaFormat(raw) -> parseSoda(raw)
            hasLrcTimestamps(raw) -> parseLrc(raw)
            else -> emptyList()
        }
    }

    private fun parseSoda(raw: String): List<LyricsLine> {
        return raw.lines().mapNotNull { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || TAG_PATTERN.matches(trimmed)) return@mapNotNull null

            val match = SODA_LINE_PATTERN.matchEntire(trimmed) ?: return@mapNotNull null
            val lineStart = match.groupValues[1].toLongOrNull() ?: return@mapNotNull null
            val lineDuration = match.groupValues[2].toLongOrNull() ?: 0L
            val content = match.groupValues[3]
            val lineEnd = if (lineDuration > 0L) lineStart + lineDuration else lineStart + DEFAULT_LINE_DURATION

            val words = SODA_WORD_PATTERN.findAll(content).mapNotNull { wordMatch ->
                val offset = wordMatch.groupValues[1].toLongOrNull() ?: return@mapNotNull null
                val duration = wordMatch.groupValues[2].toLongOrNull() ?: 0L
                val text = wordMatch.groupValues[3]
                if (text.isEmpty()) return@mapNotNull null

                val start = lineStart + offset
                LyricsWord(
                    start = start,
                    end = if (duration > 0L) start + duration else start + DEFAULT_WORD_DURATION,
                    text = text
                )
            }.toList()

            val finalWords = words.ifEmpty {
                val clean = content.replace(SODA_WORD_PATTERN, "$3").trim()
                if (clean.isBlank()) return@mapNotNull null
                listOf(LyricsWord(start = lineStart, end = lineEnd, text = clean))
            }

            LyricsLine(
                start = lineStart,
                end = finalWords.maxOfOrNull { it.end } ?: lineEnd,
                words = finalWords
            )
        }.sortedBy { it.start }
    }

    private fun parseLrc(raw: String): List<LyricsLine> {
        val lines = mutableListOf<LyricsLine>()

        raw.lines().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || TAG_PATTERN.matches(line)) return@forEach

            val matches = LRC_TIME_PATTERN.findAll(line).toList()
            if (matches.isEmpty()) return@forEach

            val hasEnhancedWords = matches.any { it.value.startsWith("<") }
            val lineTimes = matches
                .filter { it.value.startsWith("[") }
                .mapNotNull(::parseLrcTimeMs)

            if (hasEnhancedWords) {
                val lineStart = lineTimes.firstOrNull() ?: parseLrcTimeMs(matches.first()) ?: 0L
                parseEnhancedLrcLine(line, matches, lineStart)?.let { lines.add(it) }
            } else {
                val textStart = matches.last().range.last + 1
                val text = line.substring(textStart).trim()
                lineTimes.forEach { start ->
                    if (text.isNotEmpty()) {
                        lines.add(
                            LyricsLine(
                                start = start,
                                end = start + DEFAULT_LINE_DURATION,
                                words = listOf(LyricsWord(start, start + DEFAULT_LINE_DURATION, text))
                            )
                        )
                    }
                }
            }
        }

        return lines.sortedBy { it.start }
    }

    private fun parseEnhancedLrcLine(
        line: String,
        matches: List<MatchResult>,
        lineStart: Long
    ): LyricsLine? {
        val words = mutableListOf<LyricsWord>()

        matches.forEachIndexed { index, match ->
            if (!match.value.startsWith("<")) return@forEachIndexed
            val start = parseLrcTimeMs(match) ?: return@forEachIndexed
            val nextMatchStart = matches.getOrNull(index + 1)?.range?.first ?: line.length
            val text = line.substring(match.range.last + 1, nextMatchStart)
            if (text.isEmpty()) return@forEachIndexed

            val nextTime = matches
                .drop(index + 1)
                .firstNotNullOfOrNull(::parseLrcTimeMs)
            words.add(
                LyricsWord(
                    start = start,
                    end = nextTime ?: (start + DEFAULT_WORD_DURATION),
                    text = text
                )
            )
        }

        if (words.isEmpty()) {
            val fallbackText = line.substring(matches.last().range.last + 1).trim()
            if (fallbackText.isBlank()) return null
            return LyricsLine(
                start = lineStart,
                end = lineStart + DEFAULT_LINE_DURATION,
                words = listOf(LyricsWord(lineStart, lineStart + DEFAULT_LINE_DURATION, fallbackText))
            )
        }

        return LyricsLine(
            start = lineStart,
            end = words.maxOf { it.end },
            words = words
        )
    }

    private fun separateTracks(
        original: List<LyricsLine>,
        translated: List<LyricsLine>?,
        romanization: List<LyricsLine>?
    ): Triple<List<LyricsLine>, List<LyricsLine>?, List<LyricsLine>?> {
        if (!translated.isNullOrEmpty() || !romanization.isNullOrEmpty()) {
            return Triple(original, translated, romanization)
        }

        val groups = original
            .groupBy { it.start }
            .toSortedMap()

        if (groups.none { it.value.size >= 2 }) {
            return Triple(original, null, null)
        }

        val originalLines = mutableListOf<LyricsLine>()
        val translatedLines = mutableListOf<LyricsLine>()
        val romanizationLines = mutableListOf<LyricsLine>()

        groups.values.forEach { sameTimeLines ->
            when {
                sameTimeLines.size >= 3 -> {
                    originalLines.add(sameTimeLines[0])
                    romanizationLines.add(sameTimeLines[1])
                    translatedLines.addAll(sameTimeLines.drop(2))
                }
                sameTimeLines.size == 2 -> {
                    originalLines.add(sameTimeLines[0])
                    translatedLines.add(sameTimeLines[1])
                }
                else -> originalLines.add(sameTimeLines[0])
            }
        }

        return Triple(
            originalLines,
            translatedLines.ifEmpty { null },
            romanizationLines.ifEmpty { null }
        )
    }

    private fun parseLrcTimeMs(matchResult: MatchResult): Long? {
        val min = matchResult.groupValues[2].toLongOrNull() ?: return null
        val sec = matchResult.groupValues[3].toLongOrNull() ?: return null
        val ms = matchResult.groupValues[4]
            .ifBlank { "0" }
            .padEnd(3, '0')
            .take(3)
            .toLongOrNull() ?: return null
        return (min * 60 + sec) * 1000 + ms
    }

    private fun encodeVerbatimLrc(lines: List<LyricsLine>): String {
        return lines.joinToString("\n") { line ->
            line.words.joinToString("") { word ->
                val start = formatLrcTimestamp(word.start)
                val end = formatLrcTimestamp(if (word.end > word.start) word.end else word.start + DEFAULT_WORD_DURATION)
                "[$start]${word.text}[$end]"
            }
        }
    }

    private fun formatLrcTimestamp(timeMs: Long): String {
        val safeMs = timeMs.coerceAtLeast(0L)
        val minutes = safeMs / 60_000
        val seconds = (safeMs % 60_000) / 1000
        val millis = safeMs % 1000
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}.${millis.toString().padStart(3, '0')}"
    }

    private fun isSodaFormat(raw: String): Boolean =
        raw.lineSequence().any { SODA_LINE_PATTERN.matchEntire(it.trim()) != null }

    private fun hasLrcTimestamps(raw: String): Boolean =
        raw.lineSequence().any { LRC_TIME_PATTERN.containsMatchIn(it) }

    private fun isPlainLrc(raw: String): Boolean {
        var hasPlain = false
        raw.lines().forEach { line ->
            val matches = LRC_TIME_PATTERN.findAll(line).toList()
            if (matches.isEmpty()) return@forEach
            if (matches.any { it.value.startsWith("<") }) return false
            hasPlain = true
        }
        return hasPlain
    }

    private fun isVerbatimLrc(raw: String): Boolean =
        raw.lines().any { line ->
            val matches = LRC_TIME_PATTERN.findAll(line).toList()
            matches.count { it.value.startsWith("[") } > 1 && matches.none { it.value.startsWith("<") }
        }

    private fun isEnhancedLrc(raw: String): Boolean =
        raw.lines().any { line ->
            val matches = LRC_TIME_PATTERN.findAll(line).toList()
            matches.any { it.value.startsWith("[") } && matches.any { it.value.startsWith("<") }
        }

    private const val DEFAULT_LINE_DURATION = 2000L
    private const val DEFAULT_WORD_DURATION = 300L
}
