package com.isdenmois.ebookparserexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.isdenmois.ebookparser.EBookParser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private val activityScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.tv1)

        activityScope.launch {
            parseAssets().collect { addText(it.title) }
        }
    }

    private fun addText(text: String) {
        textView.append("\n$text")
    }

    private fun parseAssets() = flow {
        assets.list("")?.forEach {
            val book = EBookParser.parseBook(getAssetFile(it))

            Log.d("EBookParser", book.toString())

            if (book != null) {
                emit(book)
            }
        }
    }.flowOn(Dispatchers.Default)

    private suspend fun getAssetFile(asset: String): File {
        val f = File(cacheDir, asset)

        if (!f.exists()) {
            withContext(Dispatchers.IO) {
                var inputStream: InputStream? = null
                val outputStream = FileOutputStream(f)

                try {
                    inputStream = assets.open(asset)
                    val size: Int = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.read(buffer)
                    outputStream.write(buffer)
                } catch (e: Exception) {
                    Log.d("getAssetPath", "Can't parse $asset")
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }
            }
        }

        return f
    }
}
