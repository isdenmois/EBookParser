package com.isdenmois.ebookparser.parsers

import com.isdenmois.ebookparser.EBookFile

interface BookParser {
    fun parse(): EBookFile?
}
