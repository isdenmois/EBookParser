package com.isdenmois.ebookparser

import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import strikt.api.expectThat
import strikt.assertions.*
import strikt.java.*

@RunWith(AndroidJUnit4::class)
class FB2ParserInstrumentedTest {
    @Test
    fun parsePlain() {
        val file = TestFileLoader.loadFile("murakami.fb2")
        val eBookFile = EBookParser.parseBook(file)

        expectThat(eBookFile).isNotNull()

        expectThat(eBookFile!!.title).isEqualTo("Норвежский лес")
        expectThat(eBookFile.author).isEqualTo("Харуки Мураками")
        expectThat(eBookFile.cover).isNotNull()
        expectThat(eBookFile.file).isSameInstanceAs(file)
    }

    @Test
    fun parseZip() {
        val file = TestFileLoader.loadFile("hp1.fb2.zip")
        val eBookFile = EBookParser.parseBook(file)

        expectThat(eBookFile).isNotNull()

        expectThat(eBookFile!!.title).isEqualTo("Гарри Поттер и философский камень")
        expectThat(eBookFile.author).isEqualTo("Джоан Роулинг")
        expectThat(eBookFile.cover).isNotNull()
    }

    @Test
    fun parseWithNoCover() {
        val file = TestFileLoader.loadFile("empty-cover.fb2")
        val eBookFile = EBookParser.parseBook(file)

        expectThat(eBookFile).isNotNull()

        expectThat(eBookFile!!.title).isEqualTo("Все ок")
        expectThat(eBookFile.author).isEqualTo("Макс Фрай")
        expectThat(eBookFile.cover).isNull()
    }
}
