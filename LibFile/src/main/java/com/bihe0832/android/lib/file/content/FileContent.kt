package com.bihe0832.android.lib.file.content

import android.content.Context
import android.net.Uri
import com.bihe0832.android.lib.file.FileUtils
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 */
object FileContent {

    fun getFileContent(contetxt: Context, uri: Uri, encoding: String): String {
        return getFileContent(contetxt, uri, encoding, false)
    }

    fun getFileContent(contetxt: Context, uri: Uri, encoding: String, isGzip: Boolean): String {
        var content = ""
        var inputStream: InputStream? = null
        try {
            inputStream = contetxt.getContentResolver().openInputStream(uri)
            content = getFileContent(inputStream, encoding, isGzip)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return content
    }

    fun getFileContent(filePath: String?, encoding: String): String {
        return getFileContent(filePath, encoding, false)
    }

    fun getFileContent(filePath: String?, encoding: String, isGzip: Boolean): String {
        var content = ""
        filePath?.let {
            if (FileUtils.checkFileExist(it)) {
                var inputStream: InputStream? = null
                try {
                    inputStream = FileInputStream(File(it))
                    content = getFileContent(inputStream, encoding, isGzip)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        inputStream?.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        return content
    }

    fun getFileContent(fileInputStream: InputStream?, encoding: String, isGzip: Boolean): String {
        var content = ""
        var fis: InputStream? = null
        try {
            fis = if (isGzip) {
                GZIPInputStream(fileInputStream)
            } else {
                fileInputStream
            }
            content = getFileContent(fis, encoding)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fis?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return content
    }

    fun getFileContent(fis: InputStream?, encoding: String): String {
        val sb = StringBuffer()
        fis.let {
            var br: BufferedReader? = null
            try {
                br = BufferedReader(InputStreamReader(fis, encoding))
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    sb.append(line + System.lineSeparator())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    br?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
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

    fun writeToFile(filePath: String, data: String, encoding: String, append: Boolean) {
        writeToFile(filePath, data.toByteArray(Charset.forName(encoding)), append)
    }

    fun writeToFile(filePath: String, data: ByteArray, append: Boolean) {
        var fileOutputStream: FileOutputStream? = null
        try {
            val file = File(filePath)
            if (!FileUtils.checkFileExist(filePath)) {
                file.createNewFile()
            }
            // 建立数据的输出通道
            fileOutputStream = FileOutputStream(file, append)
            fileOutputStream.write(data)
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

    fun mergeFile(firstFile: String, secondFile: String, resultFile: String) {
        try {
            val bufferSize = 4096
            var buffer = ByteArray(bufferSize)
            var bytesRead: Int
            FileInputStream(firstFile).use { firstInputStream ->
                FileOutputStream(resultFile).use { outputFileStream ->
                    while (firstInputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputFileStream.write(buffer, 0, bytesRead)
                    }
                }
            }

            buffer = ByteArray(bufferSize)
            FileInputStream(secondFile).use { secondInputStream ->
                FileOutputStream(resultFile, true).use { outputFileStream -> // 使用追加模式
                    while (secondInputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputFileStream.write(buffer, 0, bytesRead)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun parseFileToMap(filePath: String, split: Regex): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        if (FileUtils.checkFileExist(filePath)) {
            var bufferedReader: BufferedReader? = null
            try {
                bufferedReader = File(filePath).bufferedReader()
                while (true) {
                    val line = try {
                        bufferedReader.readLine()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                    if (line == null) {
                        break
                    }
                    try {
                        val (key, value) = line.split(split)
                        map[key] = value
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close()
                    } catch (ee: Exception) {
                        ee.printStackTrace()
                    }
                }
            }
        }
        return map
    }

    fun parseAssetFileToMap(
        context: Context, fileName: String, split: Regex
    ): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        var inputStream: InputStream? = null
        var bufferedReader: BufferedReader? = null
        try {
            inputStream = context.assets.open(fileName)
            bufferedReader = BufferedReader(InputStreamReader(inputStream))
            while (true) {
                val line = try {
                    bufferedReader.readLine()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                if (line == null) {
                    break
                }
                try {
                    val (key, value) = line.split(split)
                    map[key] = value
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()
                } catch (ee: Exception) {
                    ee.printStackTrace()
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (ee: Exception) {
                    ee.printStackTrace()
                }
            }
        }
        return map
    }
}
