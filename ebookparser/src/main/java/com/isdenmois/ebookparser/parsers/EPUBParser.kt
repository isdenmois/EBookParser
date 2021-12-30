package com.isdenmois.ebookparser.parsers

import android.graphics.Bitmap
import android.util.Log
import com.isdenmois.ebookparser.BitmapDecoder
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

import com.isdenmois.ebookparser.EBookFile

class EPUBParser(private val file: File) : BookParser {
    private lateinit var zipFile: ZipFile
    private var opfPath: String? = null
    private var xpp: XmlPullParser? = null

    override fun parse(): EBookFile? {
        if (!file.canRead()) {
            return null
        }
        Log.d("EPUBParser", "Open file: $file.path")

        val start = System.currentTimeMillis()

        try {
            zipFile = ZipFile(file)
        } catch (e: ZipException) {
            e.printStackTrace()
            return null
        }

        zipFile.use {
            val xpp = getXpp() ?: return null
            var eventType = xpp.eventType

            var title: String? = null
            var imageID: String? = null
            var coverPath: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagname = xpp.name?.replace("opf:", "")

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (title == null && tagname == "dc:title") {
                            title = xpp.nextText()
                        }

                        if (imageID == null && tagname == "meta") {
                            if (xpp.getAttributeValue(null, "name") == "cover") {
                                imageID = xpp.getAttributeValue(null, "content")
                            }
                        }

                        if (tagname == "item" && isCover(xpp, imageID)) {
                            coverPath = xpp.getAttributeValue(null, "href")
                            if (opfPath!!.contains('/')) {
                                coverPath = "${opfPath!!.substring(0, opfPath!!.lastIndexOf('/'))}/$coverPath"
                            }
                            break;
                        }
                    }
                }

                eventType = xpp.next()
            }

            val coverEntry = if (coverPath != null) zipFile.getEntry(coverPath) else null
            val coverStream = if (coverEntry != null) zipFile.getInputStream(coverEntry) else getITunesArtwork()
            var cover: Bitmap? = null

            coverStream?.use {
                cover = BitmapDecoder.decodeStream(coverStream)
            }

            Log.d("TextReader/EPUB", "Parse took " + (System.currentTimeMillis() - start))

            return EBookFile(
                title = title ?: file.name,
                file = file,
                cover = cover,
            )
        }
    }

    private fun isCover(xpp: XmlPullParser, imageID: String?): Boolean {
        if (imageID != null) {
            return imageID == xpp.getAttributeValue(null, "id")
        }

        val href = xpp.getAttributeValue(null, "href") ?: return false
        val mediaType = xpp.getAttributeValue(null, "media-type") ?: return false

        return href.contains("cover", true) && mediaType.startsWith("image/", true)
    }

    private fun getXpp(): XmlPullParser? {
        if (xpp != null) {
            return xpp
        }

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val container: ZipEntry = zipFile.getEntry("META-INF/container.xml") ?: return null

        val doc = docBuilder.parse(zipFile.getInputStream(container))
        opfPath = doc.getElementsByTagName("rootfile")
            ?.item(0)?.attributes?.getNamedItem("full-path")?.textContent
            ?: return null

        val opfEntry = zipFile.getEntry(opfPath) ?: return null
        val stream = zipFile.getInputStream(opfEntry)

        parser.setInput(stream, "UTF-8")

        return parser
    }

    private fun getITunesArtwork(): InputStream? {
        val iTunesArtwork = zipFile.getEntry("iTunesArtwork") ?: return null

        return zipFile.getInputStream(iTunesArtwork)
    }
}
