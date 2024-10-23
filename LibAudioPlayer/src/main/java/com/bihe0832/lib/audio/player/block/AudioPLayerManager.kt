package com.bihe0832.lib.audio.player.block

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.annotation.NonNull
import com.bihe0832.android.lib.audio.AudioUtils
import com.bihe0832.android.lib.block.task.BaseAAFBlockTask
import com.bihe0832.android.lib.block.task.priority.PriorityBlockTaskManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.lib.audio.player.AudioItem
import com.bihe0832.lib.audio.player.AudioPlayListener
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/10/29.
 * Description: Description
 *
 */
class AudioPLayerManager : PriorityBlockTaskManager() {

    val PRIORITY_DEFAULT = 1
    private val TAG = "AudioManager"
    private val mAudioInfoMap = ConcurrentHashMap<Int, AudioItem>()

    private var mSoundPool: SoundPool? = null

    private fun createSoundPool() {
        mSoundPool = if (BuildUtils.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //设置描述音频流信息的属性
            val abs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            SoundPool.Builder().setMaxStreams(5).setAudioAttributes(abs).build()
        } else {
            SoundPool(5, AudioManager.STREAM_MUSIC, 0)
        }
        mSoundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            try {
                mAudioInfoMap.get(sampleId)?.let {
                    ZLog.d(TAG, "load Complete, add task ：sampleId $sampleId, status: $status")
                    it.playListener?.onLoadComplete(sampleId, status)
                    if (mSoundPool != null) {
                        add(BlockAudioTask(mSoundPool!!, it, {
                            mAudioInfoMap.remove(sampleId)
                            ZLog.d(TAG, "play Complete：${mAudioInfoMap.size}")
                        }, "").apply {
                            sequence = sampleId.toLong()
                            priority = it.priority
                        })
                    } else {
                        it.playListener?.onPlayFinished(-1, "")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        createSoundPool()
        setDelayTime(3000L)
    }

    class BlockAudioTask(
        private val mPlay: SoundPool,
        private val mAudioItem: AudioItem,
        private val innerFinishedAction: () -> Unit,
        name: String,
    ) : BaseAAFBlockTask(name) {

        private var errorCode = 0
        private var msg = ""

        override fun doTask() {
            try {
                if (mAudioItem != null && mAudioItem.sourceDuration > 0) {
                    mAudioItem.playListener?.onPlayStart()
                    ZLog.d(TAG, "play start：$mAudioItem")
                    try {
                        mPlay.play(
                            mAudioItem.soundid,
                            mAudioItem.leftVolume,
                            mAudioItem.rightVolume,
                            mAudioItem.priority,
                            0,
                            mAudioItem.rate
                        ).let {
                            if (it == 0) {
                                ZLog.e(TAG, "play error !!! $mAudioItem")
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    ThreadManager.getInstance().start({
                        unLockBlock()
                    }, ceil((mAudioItem.sourceDuration / mAudioItem.rate).toDouble()).toLong())
                } else {
                    errorCode = -1
                    msg = "bad audio data"
                    unLockBlock()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorCode = -2
                msg = "audio play throw exception:${e}"
                unLockBlock()
            }
        }

        override fun finishTask() {
            super.finishTask()
            ZLog.d(TAG, "play finish：$mAudioItem")
            innerFinishedAction()
            try {
                mAudioItem.playListener?.onPlayFinished(errorCode, msg)
                mPlay.unload(mAudioItem.soundid).let {
                    if (!it) {
                        ZLog.e(TAG, "play finish and unload error: $it,$mAudioItem")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun forceStop() {
            mPlay.autoPause()
            errorCode = -3
            msg = "force stop"
            unLockBlock()
        }
    }

    fun play(@NonNull context: Context, @NonNull resId: Int): Int {
        return play(context, resId, 1F, null)
    }

    fun play(@NonNull context: Context, leftVolume: Float, resId: Int): Int {
        return play(context, resId, leftVolume, null)
    }

    fun play(@NonNull context: Context, leftVolume: Float, resId: Int, priority: Int): Int {
        return play(context, resId, 1F, leftVolume, leftVolume, priority, null)
    }

    fun play(context: Context, resId: Int, leftVolume: Float, listener: AudioPlayListener?): Int {
        return play(context, resId, 1F, leftVolume, leftVolume, 0, listener)
    }

    fun play(context: Context, resId: Int, priority: Int): Int {
        return play(context, resId, 1F, 1F, 1F, priority, null)
    }

    fun play(
        context: Context,
        resId: Int,
        rate: Float,
        leftVolume: Float,
        rightVolume: Float,
        priority: Int,
        listener: AudioPlayListener?,
    ): Int {
        ZLog.d(TAG, "load start")
        listener?.onLoad()
        if (mSoundPool == null) {
            createSoundPool()
        }
        val duration = AudioUtils.getAudioDuration(context, resId)
        val soundid = mSoundPool!!.load(context, resId, PRIORITY_DEFAULT)
        mAudioInfoMap[soundid] = AudioItem(soundid, duration).apply {
            listener?.let {
                playListener = it
            }
            this.priority = priority
            this.rate = rate
            this.leftVolume = leftVolume
            this.rightVolume = rightVolume
        }
        return soundid
    }


    fun play(path: String): Int {
        return play(path, 1F, 0)
    }

    fun play(path: String, leftVolume: Float, priority: Int): Int {
        return play(path, 1.0F, leftVolume, leftVolume, priority, null)
    }

    fun play(path: String, leftVolume: Float, listener: AudioPlayListener?): Int {
        return play(path, 1.0F, leftVolume, leftVolume, 0, listener)
    }

    fun play(
        path: String,
        rate: Float,
        leftVolume: Float,
        rightVolume: Float,
        priority: Int,
        listener: AudioPlayListener?,
    ): Int {
        ZLog.d(TAG, "load start")
        listener?.onLoad()
        if (mSoundPool == null) {
            createSoundPool()
        }
        val duration = AudioUtils.getAudioDuration(path)
        return if (mSoundPool != null) {
            val soundid = mSoundPool!!.load(path, PRIORITY_DEFAULT)
            mAudioInfoMap[soundid] = AudioItem(soundid, duration).apply {
                listener?.let {
                    playListener = it
                }
                this.priority = priority
                this.leftVolume = leftVolume
                this.rightVolume = rightVolume
                this.rate = rate
            }
            soundid
        } else {
            -1
        }
    }

    override fun taskQueueIsEmptyAfterDelay() {
        try {
            super.taskQueueIsEmptyAfterDelay()
            mSoundPool?.apply {
                release()
                mSoundPool = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAll(stopCurrent: Boolean) {
        mAudioInfoMap.clear()
        super.clearAll()
        if (stopCurrent) {
            (currentTask as? BlockAudioTask)?.forceStop()
        }
    }

    fun finishCurrent(): Boolean {
        pause()
        (getCurrentTask() as? BlockAudioTask).let {
            if (null == it) {
                return false
            } else {
                it.forceStop()
                return true
            }
        }
    }

    fun pause() {
        mSoundPool?.autoPause()
    }

    fun resume() {
        mSoundPool?.autoResume()
    }
}


