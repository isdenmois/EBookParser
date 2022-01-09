package com.isdenmois.ebookparser.parsers

import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

fun Pattern.matches(source: String): String? {
    val matcher = this.matcher(source)
    if (matcher.find()) {
        return matcher.group(1)
    }

    return null
}

fun ByteArray.findArrayIndex(subarray: ByteArray, start: Int = 0, size: Int = this.size): Int {
    if (size <= 0) return -1
    if (subarray.isEmpty()) return 0

    for (i in start until size) {
        if (hasSubarray(this, subarray, i)) {
            return i
        }
    }

    return -1
}

fun ByteArray.indexOfFrom(item: Byte, start: Int = 0, size: Int = this.size): Int {
    if (size <= 0) return -1

    for (i in start until size) {
        if (this[i] == item) {
            return i
        }
    }

    return -1
}

fun hasSubarray(array: ByteArray, subarray: ByteArray, i: Int): Boolean {
    if (array[i] == subarray[0] && array.size >= i + subarray.size) {
        for (j in subarray.indices) {
            if (array[i + j] != subarray[j]) {
                return false
            }
        }

        return true
    }

    return false
}

fun ZipFile.find(filter: (ZipEntry) -> Boolean): ZipEntry? {
    val zipEntries = entries()
    var entry: ZipEntry

    while (zipEntries.hasMoreElements()) {
        entry = zipEntries.nextElement()
        if (filter(entry)) {
            return entry
        }
    }

    return null
}

fun ZipFile.getEntry(pattern: Pattern): ZipEntry? {
    val zipEntries = entries()
    var entry: ZipEntry

    while (zipEntries.hasMoreElements()) {
        entry = zipEntries.nextElement()

        if (pattern.matcher(entry.name).find()) {
            return entry
        }
    }

    return null
}
