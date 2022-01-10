package com.isdenmois.ebookparser.parsers

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class utilsTest {
    @Test
    fun emptyArray() {
        expectThat(byteArrayOf().indexOf(byteArrayOf())).isEqualTo(-1)
        expectThat(byteArrayOf().indexOf("cover.jpg".toByteArray())).isEqualTo(-1)
        expectThat("<byte>test</byte>".toByteArray().indexOf(byteArrayOf())).isEqualTo(0)
    }

    @Test
    fun hasSubarray() {
        val array = "<body></body><binary id=\"cover.jpg\">test</binary>".toByteArray()

        expectThat(array.indexOf("<binary".toByteArray())).isEqualTo(13)
        expectThat(array.indexOf("id=\"cover.jpg\"".toByteArray())).isEqualTo(21)
        expectThat(array.indexOf("</binary>".toByteArray())).isEqualTo(40)
        expectThat(array.indexOf("</binary>h".toByteArray())).isEqualTo(-1)
    }

    @Test
    fun searchWithStartIndex() {
        val array = "<body></body><binary id=\"cover.jpg\">test</binary>".toByteArray()

        expectThat(array.indexOf("<binary".toByteArray(), 12)).isEqualTo(13)
        expectThat(array.indexOf("<binary".toByteArray(), 13)).isEqualTo(13)
        expectThat(array.indexOf("<binary".toByteArray(), 14)).isEqualTo(-1)
        expectThat(array.indexOf("id=\"cover.jpg\"".toByteArray(), 20)).isEqualTo(21)
        expectThat(array.indexOf("id=\"cover.jpg\"".toByteArray(), 21)).isEqualTo(21)
        expectThat(array.indexOf("id=\"cover.jpg\"".toByteArray(), 22)).isEqualTo(-1)
        expectThat(array.indexOf("</binary>".toByteArray(), 39)).isEqualTo(40)
        expectThat(array.indexOf("</binary>".toByteArray(), 40)).isEqualTo(40)
        expectThat(array.indexOf("</binary>".toByteArray(), 41)).isEqualTo(-1)
    }
}
