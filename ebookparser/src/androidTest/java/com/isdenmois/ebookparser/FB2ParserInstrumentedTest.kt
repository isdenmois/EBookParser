package com.isdenmois.ebookparser

import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class FB2ParserInstrumentedTest {
    @Test
    fun parsePlain() {
        val file = TestFileLoader.loadFile("murakami.fb2")
        assertTrue(file.exists())

        val eBookFile = EBookParser.parseBook(file)

        assertNotNull(eBookFile!!)
        assertEquals("Норвежский лес", eBookFile.title)
        assertEquals("Харуки Мураками", eBookFile.author)
        assertNotNull(eBookFile.cover)
        assertNotNull(eBookFile.file)
    }

    @Test
    fun parseZip() {
        val file = TestFileLoader.loadFile("hp1.fb2.zip")
        assertTrue(file.exists())

        val eBookFile = EBookParser.parseBook(file)

        assertNotNull(eBookFile!!)
        assertEquals("Гарри Поттер и философский камень", eBookFile.title)
        assertEquals("Джоан Роулинг", eBookFile.author)
        assertNotNull(eBookFile.cover)
        assertNotNull(eBookFile.file)
    }
}
