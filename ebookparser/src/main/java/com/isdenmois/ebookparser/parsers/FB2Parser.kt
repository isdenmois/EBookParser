package com.isdenmois.ebookparser.parsers

import android.util.Base64;

import android.graphics.Bitmap
import android.util.Log
import com.isdenmois.ebookparser.BitmapDecoder
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipFile
import com.isdenmois.ebookparser.EBookFile

class FB2Parser(private val file: File) : BookParser {
    private var stream: InputStream? = null
    private var zipFile: ZipFile? = null

    override fun parse(): EBookFile? {
        if (!file.canRead()) {
            return null
        }

        val start = System.currentTimeMillis()
        val xpp = getXpp()

        var eventType = xpp.eventType

        var imageID: String? = null
        var decode: ByteArray? = null
        var bitmap: Bitmap? = null
        var title: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagname = xpp.name

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (title == null && tagname == "book-title") {
                        title = xpp.nextText()
                    }
                    if (imageID == null && tagname == "image") {
                        imageID = xpp.getAttributeValue(0);

                        if (imageID != null && imageID.isNotEmpty()) {
                            imageID = imageID.replace("#", "")
                        }
                    }
                    if (imageID != null && tagname == "binary" && imageID == xpp.getAttributeValue(
                            null,
                            "id"
                        )
                    ) {
                        decode = Base64.decode(xpp.nextText(), Base64.DEFAULT);
                        break;
                    }
                }
                else -> {
                }
            }

            eventType = xpp.next()
        }

        stream?.close()
        zipFile?.close()

        if (decode != null) {
            bitmap = BitmapDecoder.decodeByteArray(decode)
        }

        Log.d("TextReader/FB2", "Parse took " + (System.currentTimeMillis() - start));

        return EBookFile(
            title = title ?: file.name,
            file = file,
            cover = bitmap,
        )
    }

    private fun getXpp(): XmlPullParser {
        if (file.name.endsWith(".zip")) {
            zipFile = ZipFile(file)

            for (entry in zipFile!!.entries()) {
                if (entry.name.endsWith(".fb2")) {
                    stream = BufferedInputStream(zipFile!!.getInputStream(entry), 0x1000)
                    break
                }
            }
        } else {
            stream = FileInputStream(file)
        }

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()

        parser.setInput(stream, "UTF-8")

        return parser
    }
}
