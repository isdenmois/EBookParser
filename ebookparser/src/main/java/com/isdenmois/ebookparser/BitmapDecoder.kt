package com.isdenmois.ebookparser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.InputStream
import kotlin.math.roundToInt

object BitmapDecoder {
    private const val max = 512

    fun decodeByteArray(data: ByteArray): Bitmap {
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)

        return scaleBitmap(bitmap)
    }

    fun decodeStream(stream: InputStream): Bitmap {
        val bitmap = BitmapFactory.decodeStream(stream)

        return scaleBitmap(bitmap)
    }

    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        if (bitmap.height > max) {
            val ratio = bitmap.height.toFloat() / bitmap.width.toFloat()

            return Bitmap.createScaledBitmap(bitmap, (max / ratio).roundToInt(), max, false)
        }

        return bitmap
    }
}
