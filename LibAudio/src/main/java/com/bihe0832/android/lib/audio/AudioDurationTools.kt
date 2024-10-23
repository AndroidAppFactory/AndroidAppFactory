package com.bihe0832.android.lib.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import com.bihe0832.android.lib.audio.wav.WaveFileReader
import com.bihe0832.android.lib.file.FileUtils

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/10/28.
 * Description: Description
 *
 */
object AudioDurationTools {

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

    private fun getDurationWithMediaPlayer(player: MediaPlayer): Long {
        var duration = 0L
        try {
            player.prepare()
            duration = player.duration.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                player.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
}