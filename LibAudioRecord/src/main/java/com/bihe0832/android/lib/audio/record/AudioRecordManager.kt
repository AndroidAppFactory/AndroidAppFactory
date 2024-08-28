package com.bihe0832.android.lib.audio.record

import android.Manifest
import android.text.TextUtils
import androidx.annotation.RequiresPermission
import com.bihe0832.android.lib.audio.AudioRecordConfig
import com.bihe0832.android.lib.audio.record.common.AudioChunk
import com.bihe0832.android.lib.audio.record.common.AudioDataRecorder
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import java.util.concurrent.ConcurrentHashMap

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/7/18.
 * Description:
 *
 */
object AudioRecordManager {

     const val TAG = "AudioRecordManager"

    private var mInterval = 0.1f
    private lateinit var mAudioDataRecorder: AudioDataRecorder

    @get:Synchronized
    private val sceneWithListener = ConcurrentHashMap<String, AudioRecordItem>()

    internal class AudioRecordItem(val scene: String, val listener: AudioChunk.OnAudioChunkPulledListener) {
        var isRecord: Boolean = true
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun init(audioSource: Int, sampleRateInHz: Int, channelConfig: Int, audioFormat: Int, interval: Float) {
        mInterval = interval
        mAudioDataRecorder = AudioDataRecorder(
            AudioRecordConfig(
                audioSource,
                sampleRateInHz,
                channelConfig,
                audioFormat
            ),
            interval,
            object : AudioChunk.OnAudioChunkPulledListener {
                override fun onAudioChunkPulled(
                    audioRecordConfig: AudioRecordConfig,
                    audioChunk: AudioChunk?,
                    dataLength: Int,
                ) {
                    sceneWithListener.forEach {
                        if (it.value.isRecord) {
                            ThreadManager.getInstance().start {
                                it.value.listener.onAudioChunkPulled(audioRecordConfig, audioChunk, dataLength)
                            }
                        }
                    }
                }
            })
    }

    /**
     * 开始，请确保当前app有 RECORD_AUDIO 权限
     */
    fun startRecord(scene: String, listener: AudioChunk.OnAudioChunkPulledListener?): Boolean {
        ZLog.d(TAG, "startRecord recording:$scene")
        if (TextUtils.isEmpty(scene) || listener == null) {
            return false
        }
        return if (sceneWithListener.containsKey(scene)) {
            ZLog.d(TAG, "startRecord record failed, start before:$scene")
            false
        } else {
            ZLog.d(TAG, "startRecord record success:$scene")
            sceneWithListener[scene] = AudioRecordItem(scene, listener)
            mAudioDataRecorder.startRecord()
            true
        }
    }

    /**
     * 暂停
     */
    fun pauseRecord(scene: String) {
        ZLog.d(TAG, "pauseRecord recording:$scene")
        sceneWithListener[scene]?.isRecord = false
        if (sceneWithListener.filter { it.value.isRecord }.isEmpty()) {
            mAudioDataRecorder.pauseRecord()
        }
    }

    /**
     * 继续
     */
    fun resumeRecord(scene: String) {
        ZLog.d(TAG, "resumeRecord recording:$scene")
        if (sceneWithListener[scene]?.isRecord == false && sceneWithListener.filter { it.value.isRecord }.isEmpty()) {
            sceneWithListener[scene]?.isRecord = true
            mAudioDataRecorder.resumeRecord()
        }
    }

    /**
     * 停止
     */
    fun stopRecord(scene: String) {
        ZLog.d(TAG, "stopRecord recording:$scene")
        sceneWithListener.remove(scene)
        if (sceneWithListener.isEmpty()) {
            mAudioDataRecorder.stopRecord()
        }
    }
}