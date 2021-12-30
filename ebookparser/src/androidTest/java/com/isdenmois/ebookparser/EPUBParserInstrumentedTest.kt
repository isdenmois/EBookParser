package com.isdenmois.ebookparser

import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class EPUBParserInstrumentedTest {
    @Test
    fun parseEpubPlain() {
        val file = TestFileLoader.loadFile("plain.epub")
        assertTrue(file.exists())

        val eBookFile = EBookParser.parseBook(file)

        assertNotNull(eBookFile!!)
        assertEquals("A Darker Shade of Magic", eBookFile.title)
        assertNull(eBookFile.author)
        assertNotNull(eBookFile.cover)
        assertNotNull(eBookFile.file)
    }

    @Test
    fun parseEpubWithArtWorkOnly() {
        val file = TestFileLoader.loadFile("artwork.epub")
        assertTrue(file.exists())

        val eBookFile = EBookParser.parseBook(file)

        assertNotNull(eBookFile!!)
        assertEquals("The Stand", eBookFile.title)
        assertNull(eBookFile.author)
        assertNotNull(eBookFile.cover)
        assertNotNull(eBookFile.file)
    }

    @Test
    fun parseEpubNoCoverId() {
        val file = TestFileLoader.loadFile("no-cover-id.epub")
        assertTrue(file.exists())

        val eBookFile = EBookParser.parseBook(file)

        assertNotNull(eBookFile!!)
        assertEquals("Gentlemen and Players", eBookFile.title)
        assertNull(eBookFile.author)
        assertNotNull(eBookFile.cover)
        assertNotNull(eBookFile.file)
    }
}
