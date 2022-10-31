package com.bihe0832.android.lib.media.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import java.io.IOException

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2022/10/28.
 * Description: Description
 *
 */
object AudioTools {

    fun getAudioDuration(path: String): Long {
        var duration = 0
        if (FileUtils.checkFileExist(path)) {
            val player = MediaPlayer()
            try {
                player.setDataSource(path)
                player.prepare()
                duration = player.duration
                player.release() //记得释放资源
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return duration.toLong()
    }

    fun getAudioDuration(context: Context, uri: Uri): Long {
        var duration = 0
        val file = ZixieFileProvider.uriToFile(context, uri)
        if (FileUtils.checkFileExist(file.absolutePath)) {
            val player = MediaPlayer()
            try {
                player.setDataSource(file.absolutePath)
                player.prepare()
                duration = player.duration
                player.release() //记得释放资源
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return duration.toLong()
    }

    fun getAudioDuration(context: Context, resID: Int): Long {
        var duration = 0
        val uri = Uri.parse("android.resource://" + context.packageName.toString() + "/" + resID)
        val player = MediaPlayer()
        try {
            player.setDataSource(context, uri)
            player.prepare()
            duration = player.duration
            player.release() //记得释放资源
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return duration.toLong()
    }
}