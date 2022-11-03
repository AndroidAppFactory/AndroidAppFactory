package com.bihe0832.lib.audio.player.block

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.annotation.NonNull
import com.bihe0832.android.lib.block.task.BaseAAFBlockTask
import com.bihe0832.android.lib.block.task.BlockTaskManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.media.audio.AudioTools
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.lib.audio.player.AudioItem
import com.bihe0832.lib.audio.player.AudioPlayListener
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2022/10/29.
 * Description: Description
 *
 */
class AudioPLayerManager : BlockTaskManager() {

    val PRIORITY_DEFAULT = 1
    private val TAG = "AudioManager"
    private val mAudioInfoMap = ConcurrentHashMap<Int, AudioItem>()

    private val mSoundPool by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //设置描述音频流信息的属性
            val abs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(abs)
                .build()
        } else {
            SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        }
    }

    init {
        mSoundPool.setOnLoadCompleteListener { _, sampleId, status ->

            try {
                mAudioInfoMap.get(sampleId)?.let {
                    ZLog.d(TAG, "load Complete, add task ：sampleId $sampleId, status: $status")
                    it.playListener?.onLoadComplete(sampleId, status)
                    add(BlockAudioTask(mSoundPool, it, {
                        mAudioInfoMap.remove(sampleId)
                        ZLog.d(TAG, "play Complete：${mAudioInfoMap.size}")
                    }, "").apply {
                        sequence = sampleId.toLong()
                        priority = it.priority
                    })
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    class BlockAudioTask(
        private val mPlay: SoundPool,
        private val mAudioItem: AudioItem,
        private val finishedAction: () -> Unit,
        name: String
    ) : BaseAAFBlockTask(name) {

        override fun doTask() {
            try {
                if (mAudioItem != null && mAudioItem.duration > 0) {
                    mAudioItem.playListener?.onPlayStart()
                    ZLog.d("AudioManager", "play start：$mAudioItem")
                    try {
                        mPlay.play(mAudioItem.soundid, 1f, 1f, 0, 0, 1.0f)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    ThreadManager.getInstance().start({
                        unLockBlock(0, "success")
                    }, mAudioItem.duration)
                } else {
                    unLockBlock(-1, "bad audio data")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                unLockBlock(-2, "audio play throw exception:${e}")
            }
        }


        fun unLockBlock(errorCode: Int, msg: String) {
            mAudioItem.playListener?.onPlayFinished(errorCode, msg)
            super.unLockBlock()
            ZLog.d("AudioManager", "play finish：$mAudioItem")
        }

        override fun finishTask() {
            super.finishTask()
            finishedAction()
        }
    }

    fun play(@NonNull context: Context, @NonNull resId: Int): Int {
        return play(context, resId, null)
    }

    fun play(context: Context, resId: Int, listener: AudioPlayListener?): Int {
        return play(context, resId, 0, listener)
    }

    fun play(context: Context, resId: Int, priority: Int): Int {
        return play(context, resId, priority, null)
    }


    fun play(context: Context, resId: Int, priority: Int, listener: AudioPlayListener?): Int {
        ZLog.d(TAG, "load start")
        listener?.onLoad()
        val soundid = mSoundPool.load(context, resId, PRIORITY_DEFAULT)
        val duration = AudioTools.getAudioDuration(context, resId)
        mAudioInfoMap[soundid] = AudioItem(soundid, duration).apply {
            listener?.let {
                playListener = it
            }
            this.priority = priority
        }
        return soundid
    }


    fun play(path: String): Int {
        return play(path, 0)
    }

    fun play(path: String, priority: Int): Int {
        return play(path, priority, null)
    }

    fun play(path: String, listener: AudioPlayListener?): Int {
        return play(path, 0, listener)
    }

    fun play(path: String, priority: Int, listener: AudioPlayListener?): Int {
        ZLog.d(TAG, "load start")
        listener?.onLoad()
        val soundid = mSoundPool.load(path, PRIORITY_DEFAULT)
        val duration = AudioTools.getAudioDuration(path)
        mAudioInfoMap[soundid] = AudioItem(soundid, duration).apply {
            listener?.let {
                playListener = it
            }
            this.priority = priority
        }
        return soundid
    }

    fun stopAll(stopCurrent: Boolean) {
        mAudioInfoMap.clear()
        super.clearAll()
        if (stopCurrent) {
            (currentTask as? BlockAudioTask)?.unLockBlock(-3, "pause")
        }
    }

    fun pause() {
        mSoundPool.autoPause()
    }

    fun resume() {
        mSoundPool.autoResume()
    }
}