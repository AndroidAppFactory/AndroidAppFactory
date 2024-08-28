package com.k2fsa.sherpa.onnx

import com.bihe0832.android.lib.audio.AudioUtils
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

    fun byteArrayToSherpaArray(bytes: ByteArray): FloatArray {
        val result = FloatArray(bytes.size / 2)
        var index = 0
        for (i in bytes.indices step 2) {
            val asShort = (bytes[i + 1].toInt() shl 8) or (bytes[i].toInt() and 0xff)
            result[index++] = asShort.toFloat() / Byte.MAX_VALUE * 2.0F
        }
        return result
    }

    fun readWavAudioToSherpaArray(filePath: String?): FloatArray? {
        try {
            AudioUtils.readWavAudioData(filePath)?.let {
                return byteArrayToSherpaArray(it)
            }
        } catch (e: IOException) {
            e.printStackTrace()

        }
        return null
    }
}