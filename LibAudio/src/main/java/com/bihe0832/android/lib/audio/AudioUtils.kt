package com.bihe0832.android.lib.audio

import android.content.Context
import android.net.Uri
import com.bihe0832.android.lib.audio.wav.PcmToWav
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** 音频处理相关的日志 TAG */
const val TAG = "Audio"

/** WAV 文件头的固定长度（44 字节） */
const val WAV_LENGTH = 44

/**
 * 音频处理工具类
 *
 * 提供音频数据处理的各种工具方法，包括：
 * 1. 字节数组与 Short 数组的相互转换
 * 2. 音频时长获取
 * 3. WAV 文件数据读取
 * 4. PCM 与 WAV 格式转换
 * 5. 音频静音检测
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/10/28.
 * Description: 音频处理工具类，提供常用的音频数据处理功能
 */
object AudioUtils {

    /**
     * 将字节数组转换为 Short 数组
     *
     * 使用小端序（Little Endian）进行转换，适用于 PCM 音频数据处理。
     * 每 2 个字节转换为 1 个 Short 值。
     *
     * @param bytes 要转换的字节数组
     * @param size 要转换的字节数，必须是 2 的倍数
     * @return 转换后的 Short 数组
     * @throws IllegalArgumentException 如果 size 不是 2 的倍数或超出 bytes 长度
     */
    fun byteArrayToShortArray(bytes: ByteArray, size: Int): ShortArray {
        if (size % 2 != 0) {
            ZLog.e(TAG, "byteArrayToShortArray: size must be even, but got $size")
            throw IllegalArgumentException("Size must be even number")
        }
        if (size > bytes.size) {
            ZLog.e(TAG, "byteArrayToShortArray: size($size) exceeds bytes length(${bytes.size})")
            throw IllegalArgumentException("Size exceeds bytes length")
        }
        val shorts = ShortArray(size / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()[shorts]
        return shorts
    }

    /**
     * 将 Short 数组转换为字节数组
     *
     * 使用小端序（Little Endian）进行转换，适用于 PCM 音频数据处理。
     * 每 1 个 Short 值转换为 2 个字节。
     *
     * @param shorts 要转换的 Short 数组
     * @return 转换后的字节数组，长度为 shorts.size * 2
     */
    fun shortArrayToByteArray(shorts: ShortArray): ByteArray {
        val bytes = ByteArray(shorts.size * 2)
        for (i in shorts.indices) {
            // 低字节：取低 8 位
            bytes[i * 2] = (shorts[i].toInt() and 0xFF).toByte()
            // 高字节：取高 8 位
            bytes[i * 2 + 1] = (shorts[i].toInt() shr 8).toByte()
        }
        return bytes
    }

    /**
     * 从资源 ID 获取音频时长
     *
     * @param context 上下文对象
     * @param resID 音频资源 ID（R.raw.xxx）
     * @return 音频时长（毫秒）
     * @see AudioDurationTools.getDurationWithMediaPlayer
     */
    fun getAudioDuration(context: Context, resID: Int): Long {
        return AudioDurationTools.getDurationWithMediaPlayer(context, resID)
    }

    /**
     * 从 Uri 获取音频时长
     *
     * @param context 上下文对象
     * @param uri 音频资源的 Uri
     * @return 音频时长（毫秒）
     * @see AudioDurationTools.getDurationWithMediaPlayer
     */
    fun getAudioDuration(context: Context, uri: Uri): Long {
        return AudioDurationTools.getDurationWithMediaPlayer(context, uri)
    }

    /**
     * 从文件路径获取音频时长
     *
     * @param path 音频文件的完整路径
     * @return 音频时长（毫秒）
     * @see AudioDurationTools.getDurationWithMediaPlayer
     */
    fun getAudioDuration(path: String): Long {
        return AudioDurationTools.getDurationWithMediaPlayer(path)
    }

    /**
     * 读取 WAV 文件的音频数据（不包含文件头）
     *
     * 该方法会跳过 WAV 文件的 44 字节文件头，只读取纯粹的 PCM 音频数据。
     *
     * @param filePath WAV 文件的完整路径
     * @return 音频数据的字节数组，如果文件不存在或读取失败则返回 null
     */
    fun readWavAudioData(filePath: String?): ByteArray? {
        if (filePath.isNullOrBlank()) {
            ZLog.e(TAG, "readWavAudioData: filePath is null or blank")
            return null
        }
        if (!FileUtils.checkFileExist(filePath)) {
            ZLog.e(TAG, "readWavAudioData: file not exist: $filePath")
            return null
        }

        var bytes: ByteArray? = null
        val file = File(filePath)
        val length = file.length()

        if (length <= WAV_LENGTH) {
            ZLog.e(TAG, "readWavAudioData: file too small, length=$length")
            return null
        }

        var inputStream: FileInputStream? = null
        try {
            inputStream = FileInputStream(file)
            // 跳过 WAV 文件头（44 字节）
            val skipped = inputStream.skip(WAV_LENGTH.toLong())
            if (skipped != WAV_LENGTH.toLong()) {
                ZLog.e(TAG, "readWavAudioData: skip header failed, skipped=$skipped")
                return null
            }
            // 读取音频数据
            bytes = ByteArray(length.toInt() - WAV_LENGTH)
            val readCount = inputStream.read(bytes)
            if (readCount != bytes.size) {
                ZLog.e(TAG, "readWavAudioData: read incomplete, expected=${bytes.size}, actual=$readCount")
            }
        } catch (e: Exception) {
            ZLog.e(TAG, "readWavAudioData error: ${e.message}")
            bytes = null
        } finally {
            try {
                inputStream?.close()
            } catch (e: Exception) {
                ZLog.e(TAG, "readWavAudioData close stream error: ${e.message}")
            }
        }

        return bytes
    }

    /**
     * 将 PCM 数据转换为 WAV 文件（推荐使用，直接传入声道数）
     *
     * 该方法会在 PCM 数据前面添加 WAV 文件头，生成完整的 WAV 文件。
     *
     * @param sampleRateInHz 采样率（Hz），常用值：8000、16000、44100 等
     * @param channels 声道数：1=单声道，2=立体声
     * @param audioFormat 音频格式，参见 [android.media.AudioFormat.ENCODING_PCM_16BIT] 等
     * @param data PCM 音频数据的字节数组
     * @param targetFile 目标 WAV 文件的完整路径
     */

    fun convertPCMToWAV(
        sampleRateInHz: Int,
        channels: Int,
        audioFormat: Int,
        data: ByteArray,
        targetFile: String,
    ) {
        if (data.isEmpty()) {
            ZLog.e(TAG, "convertPCMToWAV: data is empty")
            return
        }
        // 打印转换参数，便于调试
        val bitsPerSample = if (audioFormat == android.media.AudioFormat.ENCODING_PCM_16BIT) 16 else 8
        val byteRate = bitsPerSample * sampleRateInHz * channels / 8
        ZLog.d(TAG, "convertPCMToWAV: sampleRate=$sampleRateInHz, channels=$channels, " +
                "bitsPerSample=$bitsPerSample, byteRate=$byteRate, dataLength=${data.size}")
        
        // 创建配置对象
        val config = AudioRecordConfig(
            AudioRecordConfig.DEFAULT_AUDIO_SOURCE,
            sampleRateInHz,
            channels,
            audioFormat
        )
        PcmToWav(config).convertToFile(data, targetFile)
    }

    /**
     * 将 PCM 文件转换为 WAV 文件（推荐使用，直接传入声道数）
     *
     * 该方法会读取 PCM 文件，在数据前面添加 WAV 文件头，生成完整的 WAV 文件。
     *
     * @param sampleRateInHz 采样率（Hz），常用值：8000、16000、44100 等
     * @param channels 声道数：1=单声道，2=立体声
     * @param audioFormat 音频格式，参见 [android.media.AudioFormat.ENCODING_PCM_16BIT] 等
     * @param pcmFile 源 PCM 文件的完整路径
     * @param targetFile 目标 WAV 文件的完整路径
     */

    fun convertPCMToWAV(
        sampleRateInHz: Int,
        channels: Int,
        audioFormat: Int,
        pcmFile: String,
        targetFile: String,
    ) {
        if (!FileUtils.checkFileExist(pcmFile)) {
            ZLog.e(TAG, "convertPCMToWAV: pcm file not exist: $pcmFile")
            return
        }
        // 创建配置对象
        val config = AudioRecordConfig(
            AudioRecordConfig.DEFAULT_AUDIO_SOURCE,
            sampleRateInHz,
            channels,
            audioFormat
        )
        PcmToWav(config).convertToFile(pcmFile, targetFile)
    }


    /**
     * 检测音频数据是否超过静音阈值
     *
     * 该方法用于判断音频数据中是否包含有效声音（非静音）。
     * 只要有任何一个采样点的绝对值超过阈值，就认为是有效声音。
     *
     * @param shorts 音频数据的 Short 数组（PCM 16bit 格式）
     * @param max 静音阈值，超过该值则认为是有效声音
     * @return true 表示超过静音阈值（有效声音），false 表示未超过（静音）
     */
    fun isOverSilence(shorts: ShortArray, max: Short): Boolean {
        for (sh in shorts) {
            // 检查绝对值是否超过阈值
            if (sh > max || sh < -max) {
                return true
            }
        }
        return false
    }
}