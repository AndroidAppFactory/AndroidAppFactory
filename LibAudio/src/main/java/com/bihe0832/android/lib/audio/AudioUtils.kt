package com.bihe0832.android.lib.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import com.bihe0832.android.lib.audio.wav.PcmToWav
import com.bihe0832.android.lib.audio.wav.WaveFileReader
import com.bihe0832.android.lib.file.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

val TAG = "Audio"
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

    fun getDurationWithMediaPlayer(player: MediaPlayer): Long {
        var duration = 0L
        try {
            player.prepare()
            duration = player.duration.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            player.release()
        }
        return duration
    }

    fun getDurationWithMediaPlayer(filePath: String): Long {
        val player = MediaPlayer()
        player.setDataSource(filePath)
        return getDurationWithMediaPlayer(player)
    }

    fun getDurationWithMediaPlayer(context: Context, uri: Uri): Long {
        val player = MediaPlayer()
        player.setDataSource(context, uri)
        return getDurationWithMediaPlayer(player)
    }

    fun getDurationWithMediaPlayer(context: Context, resID: Int): Long {
        val uri = Uri.parse("android.resource://" + context.packageName.toString() + "/" + resID)
        return getDurationWithMediaPlayer(context, uri)
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

    fun readWavAudioData(filePath: String?): ByteArray? {
        var bytes: ByteArray? = null
        val file = File(filePath)
        val WAV_LENGTH = 44
        if (file.exists() && file.length() > WAV_LENGTH) {

            var inputStream: FileInputStream? = null
            try {
                inputStream = FileInputStream(file)
                // 跳过WAV文件头
                val headerSize = WAV_LENGTH
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


    fun isOverSilence(shorts: ShortArray, max: Short): Boolean {
        for (sh in shorts) {
            if (sh > max || sh < -max) {
                return true
            }
        }
        return false
    }
}