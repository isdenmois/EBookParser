package com.isdenmois.ebookparser.parsers

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class FB2ParserKtTest {
    @Test
    fun emptyArray() {
        expectThat(byteArrayOf().findArrayIndex(byteArrayOf())).isEqualTo(-1)
        expectThat(byteArrayOf().findArrayIndex("cover.jpg".toByteArray())).isEqualTo(-1)
        expectThat("<byte>test</byte>".toByteArray().findArrayIndex(byteArrayOf())).isEqualTo(0)
    }

    @Test
    fun hasSubarray() {
        val array = "<body></body><binary id=\"cover.jpg\">test</binary>".toByteArray()

        expectThat(array.findArrayIndex("<binary".toByteArray())).isEqualTo(13)
        expectThat(array.findArrayIndex("id=\"cover.jpg\"".toByteArray())).isEqualTo(21)
        expectThat(array.findArrayIndex("</binary>".toByteArray())).isEqualTo(40)
    }

    @Test
    fun searchWithStartIndex() {
        val array = "<body></body><binary id=\"cover.jpg\">test</binary>".toByteArray()

        expectThat(array.findArrayIndex("<binary".toByteArray(), 12)).isEqualTo(13)
        expectThat(array.findArrayIndex("<binary".toByteArray(), 13)).isEqualTo(13)
        expectThat(array.findArrayIndex("<binary".toByteArray(), 14)).isEqualTo(-1)
        expectThat(array.findArrayIndex("id=\"cover.jpg\"".toByteArray(), 20)).isEqualTo(21)
        expectThat(array.findArrayIndex("id=\"cover.jpg\"".toByteArray(), 21)).isEqualTo(21)
        expectThat(array.findArrayIndex("id=\"cover.jpg\"".toByteArray(), 22)).isEqualTo(-1)
        expectThat(array.findArrayIndex("</binary>".toByteArray(), 39)).isEqualTo(40)
        expectThat(array.findArrayIndex("</binary>".toByteArray(), 40)).isEqualTo(40)
        expectThat(array.findArrayIndex("</binary>".toByteArray(), 41)).isEqualTo(-1)
    }
}
