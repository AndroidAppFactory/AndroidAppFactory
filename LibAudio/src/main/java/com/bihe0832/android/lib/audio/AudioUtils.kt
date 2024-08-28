package com.bihe0832.android.lib.audio

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import com.bihe0832.android.lib.audio.wav.PcmToWav
import com.bihe0832.android.lib.audio.wav.WaveFileReader
import com.bihe0832.android.lib.file.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder


object AudioUtils {
    val TAG = "Audio"

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

    fun getDurationByWavFile(filePath: String): Int {
        if (!FileUtils.checkFileExist(filePath)) {
            return -1
        }
        val wavFileReader = WaveFileReader(filePath)
        return wavFileReader.duration
    }

    fun getDurationWithMediaMetadataRetriever(filePath: String): Long {
        var duration = 0L
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(filePath)
            val tempText = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val tempLong = tempText?.toLongOrNull() ?: 0L
            duration = tempLong
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return duration
    }

    fun getDurationWithMediaPlayer(filePath: String): Long {
        var duration = 0L
        val player = MediaPlayer()
        try {
            player.setDataSource(filePath)
            player.prepare()
            duration = player.duration.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            player.release()
        }
        return duration
    }

    fun readWavAudioData(filePath: String?): ByteArray? {
        var bytes: ByteArray? = null
        if (FileUtils.checkFileExist(filePath ?: "")) {
            val file = File(filePath)
            var inputStream: FileInputStream? = null
            try {
                inputStream = FileInputStream(file)
                // 跳过WAV文件头
                val headerSize = 44
                inputStream.skip(headerSize.toLong())
                // 读取音频数据
                bytes = ByteArray(file.length().toInt() - headerSize)
                inputStream.read(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
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
}