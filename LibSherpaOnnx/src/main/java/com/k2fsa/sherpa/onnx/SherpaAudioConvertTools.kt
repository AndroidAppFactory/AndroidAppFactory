package com.k2fsa.sherpa.onnx

import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/7/25.
 * Description:
 *
 */
object SherpaAudioConvertTools {

    fun shortArrayToSherpaArray(buffer: ShortArray, size: Int): FloatArray {
        val samples = FloatArray(size) { buffer[it] / Byte.MAX_VALUE * 2.0F }
        return samples
    }

    fun shortArrayToByteArray(shorts: ShortArray): ByteArray {
        val bytes = ByteArray(shorts.size * 2)
        for (i in 0 until shorts.size) {
            bytes[i * 2] = (shorts[i].toInt() and 0xFF).toByte()
            bytes[i * 2 + 1] = (shorts[i].toInt() shr 8).toByte()
        }
        return bytes
    }


    fun byteArrayToSherpaArray(bytes: ByteArray): FloatArray {
        val result = FloatArray(bytes.size / 2)
        var index = 0
        for (i in 0 until bytes.size step 2) {
            val asShort = (bytes[i + 1].toInt() shl 8) or (bytes[i].toInt() and 0xff)
            result[index++] = asShort.toFloat() / Byte.MAX_VALUE * 2.0F
        }
        return result
    }

    fun byteArrayToShortArray(bytes: ByteArray, size: Int): ShortArray {
        val shorts = ShortArray(size / 2)
        for (i in 0 until size step 2) {
            shorts[i / 2] = ((bytes[i].toInt() and 0xFF) or ((bytes[i + 1].toInt() and 0xFF) shl 8)).toShort()
        }
        return shorts
    }

    fun readWavAudioData(filePath: String?): ByteArray? {
        return try {
            val file = File(filePath)
            FileInputStream(file).use { inputStream ->
                // 跳过WAV文件头
                val headerSize = 44
                inputStream.skip(headerSize.toLong())
                // 读取音频数据
                val bytes = ByteArray(file.length().toInt() - headerSize)
                inputStream.read(bytes)
                bytes
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun readWavAudioToSherpaArray(filePath: String?): FloatArray? {
        try {
            readWavAudioData(filePath)?.let {
                return byteArrayToSherpaArray(it)
            }
        } catch (e: IOException) {
            e.printStackTrace()

        }
        return null
    }
}