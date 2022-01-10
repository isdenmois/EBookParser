package com.isdenmois.ebookparser.parsers

import android.util.Log
import java.io.InputStream
import java.lang.Exception

fun <T>createByteInputParser(inputStream: InputStream, bufferSize: Int = 1024, dsl: ByteInputStreamParser.() -> T?): T? {
    val parser = ByteInputStreamParser(inputStream, bufferSize)

    try {
        inputStream.use {
            parser.apply {
                return dsl()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null
}

class ByteInputStreamParser(private val inputStream: InputStream, bufferSize: Int = 1024) {
    private var buffer = ByteArray(bufferSize)
    private var amount = 0
    private var count = 0

    fun skipUntil(token: String) {
        skipUntil(token.toByteArray())
    }

    fun skipUntil(token: ByteArray) {
        skip {
            buffer.indexOf(token, 0, amount)
        }
    }

    fun skipTo(token: Byte) {
        skip {
            val index = buffer.indexOf(token, 0, amount)

            if (index >= 0) {
                index + 1
            } else {
                -1
            }
        }
    }

    fun takeUntil(token: Byte): ByteArray? = take {
        buffer.indexOf(token, 0, amount)
    }

    private fun moveHeadTo(findIndex: () -> Int): Boolean {
        val index = findIndex()

        if (index >= 0) {
            buffer.copyInto(buffer, 0, index)
            amount -= index
            return true
        }

        amount = 0

        return false
    }

    private fun take(indexFound: () -> Int): ByteArray? {
        var stop = 0

        scanArray {
            stop = indexFound()

            stop > 0
        }

        if (stop > 0) {
            return buffer.sliceArray(0 until stop)
        }

        return null
    }

    private fun skip(indexFound: () -> Int) {
        scanArray {
            moveHeadTo(indexFound)
        }
    }

    private fun scanArray(shouldStop: () -> Boolean) {
        if (shouldStop()) {
            return
        }

        while (count != -1) {
            count = inputStream.read(buffer, amount, buffer.size - amount)

            if (count != -1) {
                amount += count

                if (shouldStop()) {
                    return
                }

                if (amount >= buffer.size) {
                    Log.d("EBookParser", "increase buffer ${buffer.size} -> ${buffer.size * 2}")
                    buffer = ByteArray(buffer.size * 2).also {
                        buffer.copyInto(it, 0, 0, amount)
                    }
                }
            }
        }
    }
}
