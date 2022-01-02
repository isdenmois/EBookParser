package com.isdenmois.ebookparser.parsers

import android.graphics.Bitmap
import com.isdenmois.ebookparser.BitmapDecoder
import com.isdenmois.ebookparser.EBookFile
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

class EPUBParser(private val file: File) : BookParser {
    companion object {
        private val coverImage = Pattern.compile("cover\\.png", Pattern.CASE_INSENSITIVE)

        const val XML_ELEMENT_DCTITLE = "dc:title"
        const val XML_ELEMENT_CREATOR = "dc:creator"
        const val XML_ELEMENT_META = "meta"

        const val XML_ELEMENT_MANIFESTITEM = "item"


        private val xppFactory = XmlPullParserFactory.newInstance()
    }

    private lateinit var zipFile: ZipFile
    private var opfPath: String? = null
    private val builderFactory = DocumentBuilderFactory.newInstance()
    private val docBuilder = builderFactory.newDocumentBuilder()

    private val metadata = object {
        var title: String? = null
        var author: String? = null
        var coverId: String? = null
        var coverPath: String? = null
    }

    override fun parse(): EBookFile? {
        if (!file.canRead()) {
            return null
        }

        try {
            zipFile = ZipFile(file)
        } catch (e: ZipException) {
            e.printStackTrace()
            return null
        }

        zipFile.use {
            return try {
                val xpp = xppFactory.newPullParser()
                xpp.setInput(getOpfStream(), "UTF-8")

                while (xpp.next() != XmlPullParser.END_DOCUMENT) {
                    if (xpp.eventType != XmlPullParser.START_TAG) continue
                    when (xpp.name) {
                        XML_ELEMENT_DCTITLE -> metadata.title = xpp.nextText()
                        XML_ELEMENT_CREATOR -> metadata.author = xpp.nextText()
                        XML_ELEMENT_META -> readMetaCover(xpp)
                        XML_ELEMENT_MANIFESTITEM -> readManifestCover(xpp)
                    }
                }

                EBookFile(
                    title = metadata.title ?: file.name,
                    author = metadata.author,
                    file = file,
                    cover = getCover(),
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun readMetaCover(xpp: XmlPullParser) {
        if (xpp.getAttributeValue(null, "name") == "cover") {
            metadata.coverId = xpp.getAttributeValue(null, "content")
        }
    }

    private fun readManifestCover(xpp: XmlPullParser) {
        if (xpp.getAttributeValue(null, "id") == metadata.coverId) {
            metadata.coverPath = xpp.getAttributeValue(null, "href")
        }
    }

    private fun getOpfStream(): InputStream? {
        val container: ZipEntry = zipFile.getEntry("META-INF/container.xml") ?: return null

        val doc = docBuilder.parse(zipFile.getInputStream(container))
        opfPath = doc.getElementsByTagName("rootfile")
            ?.item(0)?.attributes?.getNamedItem("full-path")?.textContent
            ?: return null

        val opfEntry = zipFile.getEntry(opfPath) ?: return null
        return zipFile.getInputStream(opfEntry)
    }

    private fun getCover(): Bitmap? {
        val cached = BitmapDecoder.fromFile(file.name)

        if (cached != null) {
            return cached
        }

        val stream = getCoverStream() ?: return null

        return BitmapDecoder.decodeAndCache(stream, file.name)
    }

    private fun getCoverStream(): InputStream? {
        var coverPath = metadata.coverPath

        var coverEntry = if (coverPath != null) {
            if (opfPath!!.contains('/')) {
                coverPath = "${opfPath!!.substring(0, opfPath!!.lastIndexOf('/'))}/$coverPath"
            }

            zipFile.getEntry(coverPath)
        } else null

        if (coverEntry == null) coverEntry = getCoverFromImages() ?: return null

        return zipFile.getInputStream(coverEntry)
    }

    private fun getCoverFromImages(): ZipEntry? {
        return zipFile.getEntry("iTunesArtwork") ?: zipFile.getEntry(coverImage)
    }
}
