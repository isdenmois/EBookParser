package com.isdenmois.ebookparserexample

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.isdenmois.ebookparser.EBookParser
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private val activityScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.tv1)

        EBookParser.cacheDirectory = cacheDir

        activityScope.launch {
            val start = System.currentTimeMillis()

            addText(parseAssets().joinToString("\n") { "${it.title}; hasCover: ${it.cover != null}" })

            val time = System.currentTimeMillis() - start

            Log.d("EBookParser", "Parse took $time")
        }
    }

    private fun addText(text: String) {
        textView.append("\n$text")
    }

    private suspend fun parseAssets() = withContext(Dispatchers.Unconfined) {
        assets.list("")?.map {
            val file = getAssetFile(it)
            val book = EBookParser.parseBook(file)

            return@map book
        }
    }?.filterNotNull() ?: listOf()

    private suspend fun parseAsyncAssets() = withContext(Dispatchers.Unconfined) {
        assets.list("")?.map {
            async {
                val file = getAssetFile(it)
                val book = EBookParser.parseBook(file)

                return@async book
            }
        }?.awaitAll()
    }?.filterNotNull() ?: listOf()

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
