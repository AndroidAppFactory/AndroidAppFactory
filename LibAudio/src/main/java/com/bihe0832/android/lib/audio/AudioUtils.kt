package com.bihe0832.android.lib.audio

import android.content.Context
import android.net.Uri
import com.bihe0832.android.lib.audio.wav.PcmToWav
import com.bihe0832.android.lib.file.FileUtils
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

const val TAG = "Audio"
const val WAV_LENGTH = 44

object AudioUtils {

    fun byteArrayToShortArray(bytes: ByteArray, size: Int): ShortArray {
        val shorts = ShortArray(size / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()[shorts]
        return shorts
    }

    fun shortArrayToByteArray(shorts: ShortArray): ByteArray {
        val bytes = ByteArray(shorts.size * 2)
        for (i in shorts.indices) {
            bytes[i * 2] = (shorts[i].toInt() and 0xFF).toByte()
            bytes[i * 2 + 1] = (shorts[i].toInt() shr 8).toByte()
        }
        return bytes
    }

    fun getAudioDuration(context: Context, resID: Int): Long {
        return AudioDurationTools.getDurationWithMediaPlayer(context, resID)
    }

    fun getAudioDuration(context: Context, uri: Uri): Long {
        return AudioDurationTools.getDurationWithMediaPlayer(context, uri)
    }

    fun getAudioDuration(path: String): Long {
        return AudioDurationTools.getDurationWithMediaPlayer(path)
    }

    fun readWavAudioData(filePath: String?): ByteArray? {
        var bytes: ByteArray? = null
        if (FileUtils.checkFileExist(filePath ?: "")) {
            val file = File(filePath!!)
            val length = file.length()
            if (length > WAV_LENGTH) {
                var inputStream: FileInputStream? = null
                try {
                    inputStream = FileInputStream(file)
                    // 跳过WAV文件头
                    val headerSize = WAV_LENGTH
                    inputStream.skip(headerSize.toLong())
                    // 读取音频数据
                    bytes = ByteArray(length.toInt() - headerSize)
                    inputStream.read(bytes)
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

        return bytes
    }

    fun convertPCMToWAV(
        sampleRateInHz: Int,
        channelConfig: Int,
        audioFormat: Int,
        data: ByteArray,
        targetFile: String,
    ) {
        PcmToWav(
            sampleRateInHz, channelConfig, audioFormat
        ).convertToFile(data, targetFile)
    }

    fun convertPCMToWAV(
        sampleRateInHz: Int,
        channelConfig: Int,
        audioFormat: Int,
        pcmFile: String,
        targetFile: String,
    ) {
        PcmToWav(
            sampleRateInHz, channelConfig, audioFormat
        ).convertToFile(pcmFile, targetFile)
    }


    fun isOverSilence(shorts: ShortArray, max: Short): Boolean {
        for (sh in shorts) {
            if (sh > max || sh < -max) {
                return true
            }
        }
        return false
    }
}