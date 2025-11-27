package com.bihe0832.android.lib.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import com.bihe0832.android.lib.audio.wav.WaveFileReader
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog

/**
 * 音频时长获取工具类
 *
 * 提供多种方式获取音频文件的时长信息：
 * 1. 通过 WAV 文件头解析获取时长
 * 2. 通过 MediaMetadataRetriever 获取时长
 * 3. 通过 MediaPlayer 获取时长
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/10/28.
 * Description: 音频时长获取工具类，支持多种音频格式和获取方式
 */
object AudioDurationTools {

    /**
     * 通过解析 WAV 文件头获取音频时长
     *
     * 该方法直接读取 WAV 文件头信息来计算音频时长，速度快且不需要加载整个音频文件。
     * 仅适用于 WAV 格式的音频文件。
     *
     * @param filePath WAV 文件的完整路径
     * @return 音频时长（毫秒），如果文件不存在或解析失败则返回 0
     */
    fun getDurationByWavFile(filePath: String): Int {
        if (!FileUtils.checkFileExist(filePath)) {
            ZLog.e(TAG, "getDurationByWavFile: file not exist: $filePath")
            return 0
        }
        return try {
            val wavFileReader = WaveFileReader(filePath)
            wavFileReader.duration
        } catch (e: Exception) {
            ZLog.e(TAG, "getDurationByWavFile error: ${e.message}")
            0
        }
    }

    /**
     * 使用 MediaMetadataRetriever 获取音频时长
     *
     * 该方法通过读取音频文件的元数据来获取时长信息，支持多种音频格式（MP3、AAC、WAV 等）。
     * 相比 MediaPlayer，该方法不需要准备播放器，性能更好。
     *
     * @param filePath 音频文件的完整路径
     * @return 音频时长（毫秒），如果获取失败则返回 0
     */
    fun getDurationWithMediaMetadataRetriever(filePath: String): Long {
        if (!FileUtils.checkFileExist(filePath)) {
            ZLog.e(TAG, "getDurationWithMediaMetadataRetriever: file not exist: $filePath")
            return 0L
        }
        var duration = 0L
        var mmr: MediaMetadataRetriever? = null
        try {
            mmr = MediaMetadataRetriever()
            mmr.setDataSource(filePath)
            val tempText = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = tempText?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            ZLog.e(TAG, "getDurationWithMediaMetadataRetriever error: ${e.message}")
        } finally {
            try {
                mmr?.release()
            } catch (e: Exception) {
                ZLog.e(TAG, "MediaMetadataRetriever release error: ${e.message}")
            }
        }
        return duration
    }

    /**
     * 使用 MediaPlayer 获取音频时长（内部方法）
     *
     * 该方法会准备 MediaPlayer 并获取音频时长，使用完毕后会自动释放资源。
     *
     * @param player 已设置数据源的 MediaPlayer 实例
     * @return 音频时长（毫秒），如果获取失败则返回 0
     */
    private fun getDurationWithMediaPlayer(player: MediaPlayer): Long {
        var duration = 0L
        try {
            // 准备播放器（同步方法）
            player.prepare()
            duration = player.duration.toLong()
        } catch (e: Exception) {
            ZLog.e(TAG, "getDurationWithMediaPlayer error: ${e.message}")
        } finally {
            try {
                player.release()
            } catch (e: Exception) {
                ZLog.e(TAG, "MediaPlayer release error: ${e.message}")
            }
        }
        return duration
    }

    /**
     * 使用 MediaPlayer 从文件路径获取音频时长
     *
     * 该方法会创建 MediaPlayer 实例并准备播放器来获取时长信息。
     * 支持多种音频格式，但性能相对 MediaMetadataRetriever 较低。
     *
     * @param filePath 音频文件的完整路径
     * @return 音频时长（毫秒），如果文件不存在或获取失败则返回 0
     */
    fun getDurationWithMediaPlayer(filePath: String): Long {
        if (!FileUtils.checkFileExist(filePath)) {
            ZLog.e(TAG, "getDurationWithMediaPlayer: file not exist: $filePath")
            return 0L
        }
        return try {
            val player = MediaPlayer()
            player.setDataSource(filePath)
            getDurationWithMediaPlayer(player)
        } catch (e: Exception) {
            ZLog.e(TAG, "getDurationWithMediaPlayer error: ${e.message}")
            0L
        }
    }

    /**
     * 使用 MediaPlayer 从 Uri 获取音频时长
     *
     * 该方法支持从 Content Uri 或其他 Uri 类型获取音频时长。
     * 适用于从 ContentProvider、网络或其他来源获取的音频。
     *
     * @param context 上下文对象
     * @param uri 音频资源的 Uri
     * @return 音频时长（毫秒），如果获取失败则返回 0
     */
    fun getDurationWithMediaPlayer(context: Context, uri: Uri): Long {
        return try {
            val player = MediaPlayer()
            player.setDataSource(context, uri)
            getDurationWithMediaPlayer(player)
        } catch (e: Exception) {
            ZLog.e(TAG, "getDurationWithMediaPlayer(uri) error: ${e.message}")
            0L
        }
    }

    /**
     * 使用 MediaPlayer 从资源 ID 获取音频时长
     *
     * 该方法用于获取应用资源文件（res/raw 目录）中音频的时长。
     *
     * @param context 上下文对象
     * @param resID 音频资源 ID（R.raw.xxx）
     * @return 音频时长（毫秒），如果获取失败则返回 0
     */
    fun getDurationWithMediaPlayer(context: Context, resID: Int): Long {
        return try {
            val uri = Uri.parse("android.resource://${context.packageName}/$resID")
            getDurationWithMediaPlayer(context, uri)
        } catch (e: Exception) {
            ZLog.e(TAG, "getDurationWithMediaPlayer(resID) error: ${e.message}")
            0L
        }
    }
}