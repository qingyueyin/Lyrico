package com.lonx.lyrics.utils

import com.lonx.lyrics.model.LyricsData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SodaParserTest {
    @Test
    fun parseSodaBuildsRawVerbatimLyrics() {
        val raw = """
            [ti:Song]
            [1000,1500]<0,500,0>Hello<500,500,0> world
        """.trimIndent()

        val result = SodaParser.parse(LyricsData(original = raw, type = "soda"))

        assertEquals("Song", result.tags["ti"])
        assertEquals("[00:01.000]Hello[00:01.500][00:01.500] world[00:02.000]", result.rawVerbatimLrc)
        assertTrue(result.isWordByWord)
        assertEquals(2, result.original.single().words.size)
        assertEquals(1000L, result.original.single().words.first().start)
        assertEquals("Hello", result.original.single().words.first().text)
    }

    @Test
    fun parseLrcSeparatesSameTimestampTracks() {
        val raw = """
            [00:01.000]Original
            [00:01.000]Translation
            [00:02.000]Next
        """.trimIndent()

        val result = SodaParser.parse(LyricsData(original = raw))

        assertEquals(2, result.original.size)
        assertEquals("Original", result.original.first().words.single().text)
        assertNotNull(result.translated)
        assertEquals("Translation", result.translated!!.single().words.single().text)
        assertEquals(raw, result.rawPlainLrc)
    }

    @Test
    fun parseEnhancedLrcCreatesWordTiming() {
        val raw = "[00:01.000]<00:01.000>Hello<00:01.500> world<00:02.000>"

        val result = SodaParser.parse(LyricsData(original = raw))

        assertTrue(result.isWordByWord)
        assertEquals(raw, result.rawEnhancedLrc)
        assertEquals(2, result.original.single().words.size)
        assertEquals(1000L, result.original.single().words[0].start)
        assertEquals(1500L, result.original.single().words[0].end)
    }
}
