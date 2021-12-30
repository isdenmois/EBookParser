package com.isdenmois.ebookparser

import android.graphics.Bitmap
import java.io.File

data class EBookFile(
    val title: String,
    val author: String? = null,
    val cover: Bitmap? = null,
    val file: File,
)
