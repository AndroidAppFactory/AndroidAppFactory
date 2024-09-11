package com.bihe0832.android.lib.audio.record

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.annotation.RequiresPermission
import com.bihe0832.android.lib.audio.AudioRecordConfig
import com.bihe0832.android.lib.audio.record.core.AudioChunk
import com.bihe0832.android.lib.audio.record.core.AudioDataRecorder
import com.bihe0832.android.lib.foreground.service.AAFForegroundServiceManager
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

    const val TAG = AudioRecordConfig.TAG

    private var mInterval = 0.1f
    private var mAudioDataRecorder: AudioDataRecorder? = null

    @get:Synchronized
    private val sceneWithListener = ConcurrentHashMap<String, AudioRecordItem>()

    internal class AudioRecordItem(val scene: String, val listener: AudioChunk.OnAudioChunkPulledListener) {
        var isRecord: Boolean = true
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun init(audioSource: Int, sampleRateInHz: Int, channelConfig: Int, audioFormat: Int, interval: Float) {
        mInterval = interval
        mAudioDataRecorder = AudioDataRecorder(AudioRecordConfig(
            audioSource, sampleRateInHz, channelConfig, audioFormat
        ), interval, object : AudioChunk.OnAudioChunkPulledListener {
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

    fun startRecord(
        activity: Activity?,
        scene: String,
        notifyContent: String,
        listener: AudioChunk.OnAudioChunkPulledListener?,
    ): Boolean {
        if (activity == null) {
            return startRecord(scene, listener)
        } else {
            return AAFForegroundServiceManager.sendToForegroundService(
                activity,
                Intent(),
                object : AAFForegroundServiceManager.ForegroundServiceAction {
                    override fun getScene(): String {
                        return scene
                    }

                    override fun getNotifyContent(): String {
                        return notifyContent
                    }

                    override fun onStartCommand(context: Context, intent: Intent, flags: Int, startId: Int) {
                        startRecord(scene, listener)
                    }
                })
        }
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
            mAudioDataRecorder?.startRecord()
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
            mAudioDataRecorder?.pauseRecord()
        }
    }

    /**
     * 继续
     */
    fun resumeRecord(scene: String) {
        ZLog.d(TAG, "resumeRecord recording:$scene")
        if (sceneWithListener[scene]?.isRecord == false && sceneWithListener.filter { it.value.isRecord }.isEmpty()) {
            sceneWithListener[scene]?.isRecord = true
            mAudioDataRecorder?.resumeRecord()
        }
    }

    /**
     * 停止
     */
    fun stopRecord(context: Context, scene: String) {
        ZLog.d(TAG, "stopRecord recording:$scene")
        sceneWithListener.remove(scene)
        if (sceneWithListener.isEmpty()) {
            mAudioDataRecorder?.stopRecord()
        }
        AAFForegroundServiceManager.deleteFromForegroundService(context, scene)
    }
}