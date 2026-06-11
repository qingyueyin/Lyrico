package com.lonx.lyrico.utils

object LyricsSearchTextExtractor {
    fun extractLines(lyrics: String?): List<String> {
        val raw = lyrics.orEmpty()
        if (raw.isBlank()) return emptyList()

        val decodedLines = LyricDecoder.decode(raw)
            ?.let { result ->
                listOfNotNull(
                    result.original,
                    result.translated,
                    result.romanization
                )
                    .flatten()
                    .map { line -> line.words.joinToString("") { word -> word.text } }
            }
            .orEmpty()

        return decodedLines.ifEmpty { fallbackLyricLines(raw) }
            .map { line -> line.trim() }
            .filter { line -> line.isNotBlank() }
            .distinct()
    }

    fun toSearchText(lyrics: String?): String? {
        return extractLines(lyrics)
            .joinToString("\n")
            .takeIf { it.isNotBlank() }
    }

    private fun fallbackLyricLines(lyrics: String): List<String> {
        return lyrics.lineSequence()
            .map { line ->
                line
                    .replace(lrcTimePattern, "")
                    .replace(ttmlTagPattern, "")
                    .trim()
            }
            .filter { line ->
                line.isNotBlank() &&
                    !line.startsWith("[") &&
                    !line.startsWith("<")
            }
            .toList()
    }

    private val lrcTimePattern = Regex("""\[(?:\d{1,2}:)?\d{1,2}:\d{1,2}(?:[.:]\d{1,3})?]""")
    private val ttmlTagPattern = Regex("""<[^>]+>""")
}
