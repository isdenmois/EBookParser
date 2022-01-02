package com.isdenmois.ebookparser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
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

    fun decodeAndCache(data: ByteArray, filename: String): Bitmap {
        val bitmap = decodeByteArray(data)

        saveBitmap(bitmap, filename)

        return bitmap
    }

    fun decodeAndCache(stream: InputStream, filename: String): Bitmap {
        val bitmap = decodeStream(stream)

        saveBitmap(bitmap, filename)

        return bitmap
    }

    fun fromFile(filename: String): Bitmap? {
        if (EBookParser.cacheDirectory == null) return null
        val cacheImage = getCacheFile(filename)

        if (cacheImage.exists()) {
            return decodeFile(cacheImage)
        }

        return null
    }

    private fun saveBitmap(bitmap: Bitmap, filename: String) {
        if (EBookParser.cacheDirectory == null) return
        val cacheImage = getCacheFile(filename)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, cacheImage.outputStream())
    }

    private fun getCacheFile(filename: String) = File(EBookParser.cacheDirectory, "${filename}.jpg")

    private fun decodeFile(file: File): Bitmap {
        return BitmapFactory.decodeStream(file.inputStream())
    }

    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        if (bitmap.height > max) {
            val ratio = bitmap.height.toFloat() / bitmap.width.toFloat()

            return Bitmap.createScaledBitmap(bitmap, (max / ratio).roundToInt(), max, false)
        }

        return bitmap
    }
}
