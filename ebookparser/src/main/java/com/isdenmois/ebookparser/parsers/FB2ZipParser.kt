package com.isdenmois.ebookparser.parsers

import com.isdenmois.ebookparser.EBookFile
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

class FB2ZipParser(file: File) : FB2Parser(file) {
    private var zipFile: ZipFile? = null

    override fun createInputStream(): InputStream {
        zipFile = ZipFile(file)
        val entry = zipFile!!.entries().nextElement()

        return zipFile!!.getInputStream(entry)
    }

    override fun parse(): EBookFile? {
        val ebook = super.parse()

        zipFile?.close()

        return ebook
    }
}
