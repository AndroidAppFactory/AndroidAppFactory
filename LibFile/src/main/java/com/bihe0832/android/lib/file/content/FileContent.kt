package com.bihe0832.android.lib.file.content

import com.bihe0832.android.lib.file.FileUtils
import java.io.*
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 */
object FileContent {

    fun getFileContent(filePath: String?, encoding: String): String {
        return getFileContent(filePath, encoding, false)
    }

    fun getFileContent(filePath: String?, encoding: String, isGzip: Boolean): String {
        val sb = StringBuffer()
        filePath?.let { it ->
            if (FileUtils.checkFileExist(it)) {
                var fis: InputStream? = null
                var br: BufferedReader? = null
                try {
                    fis = if (isGzip) {
                        GZIPInputStream(FileInputStream(File(it)))
                    } else {
                        FileInputStream(File(it))
                    }

                    br = BufferedReader(InputStreamReader(fis, encoding))
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        sb.append(line + System.lineSeparator())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        fis?.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        br?.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return sb.toString()
    }

    fun getFileBytes(filePath: String?): ByteArray? {
        filePath?.let { it ->
            if (FileUtils.checkFileExist(it)) {
                var buf: BufferedInputStream? = null
                try {
                    val file = File(filePath)
                    val size: Int = file.length().toInt()
                    val bytes = ByteArray(size)
                    buf = BufferedInputStream(FileInputStream(file))
                    buf.read(bytes, 0, bytes.size)
                    return bytes
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        buf?.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return null

    }

    fun isBinaryFile(filePath: String): Boolean {
        getFileBytes(filePath)?.let {
            for (b in it) {
                if (b < 0x09) {
                    return true
                }
            }
        }
        return false
    }

    fun writeToFile(filePath: String, data: String, append: Boolean) {
        var fileOutputStream: FileOutputStream? = null
        try {
            val file = File(filePath)
            if (!FileUtils.checkFileExist(filePath)) {
                file.createNewFile()
            }

            //建立数据的输出通道
            fileOutputStream = FileOutputStream(file, append)
            fileOutputStream.write(data.toByteArray(Charset.forName("ISO-8859-1")))
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}