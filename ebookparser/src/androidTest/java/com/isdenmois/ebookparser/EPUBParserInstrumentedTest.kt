package com.isdenmois.ebookparser

import strikt.api.expectThat
import strikt.assertions.*
import strikt.java.*

import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EPUBParserInstrumentedTest {
    @Test
    fun parseEpubPlain() {
        val file = TestFileLoader.loadFile("plain.epub")
        val eBookFile = EBookParser.parseBook(file)

        expectThat(eBookFile).isNotNull()

        expectThat(eBookFile!!.title).isEqualTo("A Darker Shade of Magic")
        expectThat(eBookFile.author).isEqualTo("V.E Schwab")
        expectThat(eBookFile.cover).isNotNull()
        expectThat(eBookFile.file).isSameInstanceAs(file)
    }

    @Test
    fun parseEpubWithArtWorkOnly() {
        val file = TestFileLoader.loadFile("artwork.epub")
        val eBookFile = EBookParser.parseBook(file)

        expectThat(eBookFile).isNotNull()

        expectThat(eBookFile!!.title).isEqualTo("The Stand")
        expectThat(eBookFile.author).isEqualTo("Stephen King")
        expectThat(eBookFile.cover).isNotNull()
    }

    @Test
    fun parseEpubNoCoverId() {
        val file = TestFileLoader.loadFile("no-cover-id.epub")
        val eBookFile = EBookParser.parseBook(file)

        expectThat(eBookFile).isNotNull()

        expectThat(eBookFile!!.title).isEqualTo("Gentlemen and Players")
        expectThat(eBookFile.author).isEqualTo("Joanne Harris")
        expectThat(eBookFile.cover).isNotNull()
    }

    @Test
    fun parseWithNoCover() {
        val file = TestFileLoader.loadFile("empty-cover.epub")
        val eBookFile = EBookParser.parseBook(file)

        expectThat(eBookFile).isNotNull()

        expectThat(eBookFile!!.title).isEqualTo("Look what I've done")
        expectThat(eBookFile.author).isEqualTo("Some Weirdo")
        expectThat(eBookFile.cover).isNull()
    }
}
