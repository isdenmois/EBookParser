package com.isdenmois.ebookparser.parsers

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class ByteInputStreamParserTest {
    @Test
    fun success() {
        val array = "<body></body><binary id=\"cover.jpg\">test</binary>".toByteArray()

        val value = createByteInputParser(array.inputStream()) {
            skipUntil("id=\"cover.jpg\"".toByteArray())
            skipTo('>'.toByte())

            takeUntil('<'.toByte())
        }

        expectThat(String(value!!)).isEqualTo("test")
    }

    @Test
    fun emptyResult() {
        val array = "<body></body><binary id=\"cover.jpg\">test</binary>".toByteArray()

        val value = createByteInputParser(array.inputStream()) {
            skipUntil("id=\"cover.jpg\"")
            skipTo('>'.toByte())

            takeUntil('t'.toByte())
        }

        expectThat(value).isNull()
    }

    @Test
    fun smallBuffer() {
        val array = "<body></body><binary id=\"cover.jpg\" hellow>test</binary>".toByteArray()

        expectThat(array.indexOf(">t".toByteArray())).isEqualTo(42)

        val value = createByteInputParser(array.inputStream(), 42) {
            skipUntil("id=\"cover.jpg\"".toByteArray())
            skipTo('>'.toByte())

            takeUntil('<'.toByte())
        }

        expectThat(String(value!!)).isEqualTo("test")
    }
}