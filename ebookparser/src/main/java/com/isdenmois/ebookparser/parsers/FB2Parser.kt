package com.isdenmois.ebookparser.parsers

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.isdenmois.ebookparser.BitmapDecoder
import com.isdenmois.ebookparser.EBookFile
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.regex.Matcher
import java.util.regex.Pattern

open class FB2Parser(protected val file: File) : BookParser {
    companion object {
        private const val MAX_FB2INFO_SIZE = 8192
        private const val MAX_XMLINFO_SIZE = 80
        private const val MAX_FB2_SIZE = 1_048_576

        private val xmlEncoding = Pattern.compile("(?i).*encoding=[\"'](.*?)[\"'].*")
        private val fb2Annotation = Pattern.compile("(?s)<annotation>(.*?)</annotation>")
        private val fb2FirstName = Pattern.compile("(?s)<first-name>(.*)</first-name>");
        private val fb2LastName = Pattern.compile("(?s)<last-name>(.*)</last-name>");
        private val fb2Author = Pattern.compile("(?s)<author>(.*?)</author>");
        private val fb2Title = Pattern.compile("(?s)<book-title>(.*?)</book-title>")
        private val fb2CoverName = Pattern.compile("(?s)<coverpage>.*href=\"#(.*?)\".*</coverpage>")
    }

    private var annotation: String? = null
    private var buffer = ByteArray(MAX_FB2INFO_SIZE)
    private var amount = 0

    override fun parse(): EBookFile? {
        if (!file.canRead()) {
            return null
        }

        createInputStream().use { input ->
            val source = createSource(input)

            return EBookFile(
                title = parseTitle(source) ?: file.name,
                author = parseAuthors(source),
                cover = parseCover(source, input),
                file = file,
            )
        }
    }

    protected open fun createInputStream(): InputStream {
        return file.inputStream()
    }

    private fun createSource(input: InputStream): String {
        val buffer: ByteArray = readInputStream(input)
        val encoding = getXmlEncoding(buffer)
        var preparedInput = String(buffer, encoding)
        val matcher: Matcher = fb2Annotation.matcher(preparedInput)

        if (matcher.find()) {
            annotation = matcher.group(1)
            preparedInput = matcher.replaceFirst("")
        }

        return preparedInput
    }


    @Throws(IOException::class)
    private fun readInputStream(input: InputStream): ByteArray {
        var counter = 0
        var stopCounter = 0
        var stop = false
        while (!stop and (amount < MAX_FB2INFO_SIZE) && counter != -1) {
            counter = input.read(buffer, amount, MAX_FB2INFO_SIZE - amount)
            amount += counter
            while (stopCounter < amount) {
                if (buffer[stopCounter] == '>'.toByte())
                    if (buffer[stopCounter - 1] == 'o'.toByte())
                        if (buffer[stopCounter - 12] == '<'.toByte())
                            if (buffer[stopCounter - 10] == 't'.toByte()) {
                                stop = true
                                break
                            }
                stopCounter++
            }
        }
        if (amount <= 0) throw IOException("Empty input stream")
        val output = ByteArray(stopCounter)
        System.arraycopy(buffer, 0, output, 0, stopCounter)
        return output
    }

    private fun getXmlEncoding(input: ByteArray): Charset {
        val xmlHeader = String(input, 0, MAX_XMLINFO_SIZE, charset("ISO-8859-1"))
        val encoding = xmlEncoding.matches(xmlHeader) ?: "utf-8"

        return charset(encoding)
    }

    private fun parseTitle(source: String) = fb2Title.matches(source)

    private fun parseAuthors(source: String): String? {
        val author = fb2Author.matches(source) ?: return null

        return parsePerson(author)
    }

    private fun parsePerson(input: String): String? {
        val person = listOfNotNull(
            fb2FirstName.matches(input)?.trim(),
            fb2LastName.matches(input)?.trim(),
        ).joinToString(" ").trim()

        if (person.isBlank()) return null

        return person
    }

    private fun parseCover(source: String, input: InputStream): Bitmap? {
        val cached = BitmapDecoder.fromFile(file.name)

        if (cached != null) {
            return cached
        }

        val id = fb2CoverName.matches(source) ?: return null
        val decode = getCover(input, id) ?: return null

        return BitmapDecoder.decodeAndCache(decode, file.name)
    }

    private fun getCover(input: InputStream, id: String): ByteArray? {
        readBuffer(input)

        val stopBuffer = "</binary>".toByteArray()
        val cover64: ByteArray
        val idPosition = buffer.findArrayIndex("id=\"$id\"".toByteArray(), 0, amount)
        if (idPosition < 0) return null

        val start = buffer.indexOfFrom('>'.toByte(), idPosition, amount) + 1
        if (start <= 0) return null

        val stop = buffer.findArrayIndex(stopBuffer, start, amount + 1) - 1
        if (stop < 0) return null

        val newSize = stop - start + 1
        cover64 = ByteArray(newSize)
        System.arraycopy(buffer, start, cover64, 0, newSize)

        return Base64.decode(cover64, Base64.DEFAULT)
    }

    private fun readBuffer(input: InputStream) {
        val binary = "<binary".toByteArray()
        buffer = ByteArray(MAX_FB2_SIZE).also {
            buffer.copyInto(it, 0, 0, amount)
        }

        try {
            var count = 0

            while (count != -1) {
                count = input.read(buffer, amount, buffer.size - amount)

                if (count != -1) {
                    amount += count

                    val binaryIndex = buffer.findArrayIndex(binary)

                    if (binaryIndex < 0) {
                        amount = 0
                        Log.d("readBuffer", "binaryIndex < 0; amount=${amount}")
                    } else if (binaryIndex > 0) {
                        buffer.copyInto(buffer, 0, binaryIndex)
                        amount -= binaryIndex
                        Log.d("readBuffer", "binaryIndex >= 0; amount=${amount}")
                    }

                    if (amount >= buffer.size) {
                        Log.d("readBuffer", "amount >= buffer.size; amount=${amount}")
                        buffer = ByteArray(buffer.size * 2).also {
                            buffer.copyInto(it, 0, 0, amount)
                        }
                    }
                }
            }
        } catch (e: IOException) {
        }
    }
}
