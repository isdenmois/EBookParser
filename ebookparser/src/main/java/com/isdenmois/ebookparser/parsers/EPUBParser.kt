package com.isdenmois.ebookparser.parsers

import android.graphics.Bitmap
import com.isdenmois.ebookparser.BitmapDecoder
import com.isdenmois.ebookparser.EBookFile
import java.io.*
import java.nio.charset.Charset
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

class EPUBParser(private val file: File) : BookParser {
    companion object {
        private const val MAX_XMLINFO_SIZE = 80

        private val xmlEncoding = Pattern.compile("(?i).*encoding=[\"'](.*?)[\"'].*")
        private val epubTitle = Pattern.compile("(?s)<dc:title.*?>(.*?)</dc:title>");
        private val epubAuthor = Pattern.compile("(?s)<dc:creator.*?>(.*?)</dc:creator>")
        private val epubCoverId = Pattern.compile("<meta.*?name=\"cover\".*?content=\"(.*?)\"|<meta.*?content=\"(.*?)\".*?name=\"cover\"")
        private val epubCover = Pattern.compile("(?s)<embeddedcover>(.*?)</embeddedcover>")
        private val coverImage = Pattern.compile("cover\\.png", Pattern.CASE_INSENSITIVE)
    }

    private lateinit var zipFile: ZipFile
    private var opfPath: String? = null

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
                val source = createSource()

                EBookFile(
                    title = parseTitle(source) ?: file.name,
                    author = parseAuthor(source),
                    file = file,
                    cover = parseCover(source),
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    @Throws(IOException::class)
    private fun createSource(): String {
        val inputStream = getOpfStream() ?: return ""
        val buffer = ByteArray(MAX_XMLINFO_SIZE)
        inputStream.read(buffer, 0, buffer.size)

        val encoding = getXmlEncoding(buffer)
        val prefix = String(buffer, encoding)

        return prefix + BufferedReader(InputStreamReader(inputStream, encoding))
            .lines()
            .parallel()
            .collect(Collectors.joining("\n"))
    }

    private fun getOpfStream(): InputStream? {
        val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val container: ZipEntry = zipFile.getEntry("META-INF/container.xml") ?: return null

        val doc = docBuilder.parse(zipFile.getInputStream(container))
        opfPath = doc.getElementsByTagName("rootfile")
            ?.item(0)?.attributes?.getNamedItem("full-path")?.textContent
            ?: return null

        val opfEntry = zipFile.getEntry(opfPath) ?: return null
        return zipFile.getInputStream(opfEntry)
    }

    private fun getXmlEncoding(buffer: ByteArray): Charset {
        val xmlHeader = String(buffer, 0, MAX_XMLINFO_SIZE, charset("ISO-8859-1"))
        val matcher: Matcher = xmlEncoding.matcher(xmlHeader)
        val encoding = if (matcher.find()) matcher.group(1) else "utf-8"
        return charset(encoding)
    }

    private fun parseTitle(source: String) = epubTitle.matches(source)

    private fun parseAuthor(source: String) = epubAuthor.matches(source)

    private fun parseCover(source: String): Bitmap? {
        val cached = BitmapDecoder.fromFile(file.name)

        if (cached != null) {
            return cached
        }

        val coverPath = getCoverPath(source)
        val stream = getCover(coverPath) ?: return null

        return BitmapDecoder.decodeAndCache(stream, file.name)
    }

    private fun getCoverPath(source: String) = epubCover.matches(source) ?: getCoverPathByCoverId(source)

    private fun getCoverPathByCoverId(source: String): String? {
        val matcher = epubCoverId.matcher(source)
        if (!matcher.find()) return null
        val coverId = matcher.group(1) ?: matcher.group(2) ?: return null

        return Pattern.compile("item.*?href=\"(.*?)\".*?id=\"$coverId\"").matches(source)
    }

    private fun getCover(path: String?): InputStream? {
        var coverPath = path

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
