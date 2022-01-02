package com.isdenmois.ebookparser

import android.util.Log
import com.isdenmois.ebookparser.parsers.*
import java.io.File

object EBookParser {
    var cacheDirectory: File? = null
        set(dir) {
            val destination = File(dir, "covers")
            if (!destination.exists()) {
                destination.mkdir()
            }

            if (destination.isDirectory) {
                field = destination
            }
        }

    fun parseBook(file: File): EBookFile? {
        val start = System.currentTimeMillis()
        val parser = when {
            file.name.endsWith(".epub", true) -> EPUBParser(file)
            file.name.endsWith(".fb2", true) -> FB2Parser(file)
            file.name.endsWith(".fb2.zip", true) -> FB2ZipParser(file)
            else -> UnknownParser()
        }

        val eBookFile = parser.parse()
        val time = System.currentTimeMillis() - start

        Log.d("EBookParser", "${file.name}; Parse took $time")

        return eBookFile
    }
}
