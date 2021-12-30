package com.isdenmois.ebookparser.parsers

import com.isdenmois.ebookparser.EBookFile

class UnknownParser : BookParser {
    override fun parse(): EBookFile? {
        return null
    }
}
