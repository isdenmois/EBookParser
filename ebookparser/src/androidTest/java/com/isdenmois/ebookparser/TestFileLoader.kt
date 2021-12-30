package com.isdenmois.ebookparser

import android.os.FileUtils
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

object TestFileLoader {
    fun loadFile(path: String): File {
        val context = InstrumentationRegistry.getInstrumentation().context
        val file = File(context.cacheDir, path)

        if (!file.exists()) {
            val testInput = context.assets.open(path)

            try {
                FileUtils.copy(testInput, file.outputStream())
            } catch (e: Exception) {
            } finally {
                testInput.close()
            }
        }

        return file
    }
}
