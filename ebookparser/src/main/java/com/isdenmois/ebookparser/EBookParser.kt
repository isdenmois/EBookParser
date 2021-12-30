package com.isdenmois.ebookparser

import com.isdenmois.ebookparser.parsers.EPUBParser
import com.isdenmois.ebookparser.parsers.FB2Parser
import com.isdenmois.ebookparser.parsers.UnknownParser
import java.io.File

object EBookParser {
    fun parseBook(file: File): EBookFile? {
        val parser = when {
            file.name.endsWith(".epub", true) -> EPUBParser(file)
            file.name.endsWith(".fb2", true) -> FB2Parser(file)
            file.name.endsWith(".fb2.zip", true) -> FB2Parser(file)
            else -> UnknownParser()
        }

        return parser.parse()
    }
}
