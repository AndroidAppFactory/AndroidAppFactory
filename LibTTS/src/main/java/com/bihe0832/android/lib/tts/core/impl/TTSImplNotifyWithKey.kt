package com.bihe0832.android.lib.tts.core.impl

import android.content.Context
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.tts.core.TTSConfig
import com.bihe0832.android.lib.tts.core.TTSData
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * 带Key回调的TTS实现类
 *
 * 在TTSImpl基础上增加了Key透传功能，允许在播放时传入自定义Key，
 * 在回调时会将该Key透传回来，方便业务层识别是哪个语音播放完成。
 *
 * 使用场景：
 * - 需要区分不同语音播放的回调
 * - 需要在回调中携带业务数据
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/3/24.
 */
open class TTSImplNotifyWithKey : TTSImpl() {
    private val TAG = TTSImpl.TAG

    private var mTTSListener: TTSListener? = null

    // TTS 透传参数，回调会透传
    private val mTTSNotifyKeyMap = ConcurrentHashMap<String, String>()

    interface TTSListener {
        fun onStart(utteranceId: String, key: String)
        fun onError(utteranceId: String, key: String)
        fun onComplete(utteranceId: String, key: String)
    }

    fun initTTSImplWithKey(
        context: Context,
        loc: Locale,
        engine: String?,
        initListener: TTSInitListener?,
        listener: TTSListener?,
    ) {
        if (listener == null) {
            ZLog.e(TAG, "TTSImplNotifyWithKey init  TTSListener is null !!!")
        }
        mTTSListener = listener
        initTTSImpl(context, loc, engine, initListener, object : TTSSpeakListener {
            var lastStart = System.currentTimeMillis()
            override fun onUtteranceStart(utteranceId: String) {
                lastStart = System.currentTimeMillis()
                mTTSListener?.onStart(utteranceId, mTTSNotifyKeyMap.get(utteranceId) ?: "")
                ZLog.d(TAG, "TTS ${this.hashCode()} onStart $utteranceId : $lastStart")
            }

            override fun onUtteranceDone(utteranceId: String) {
                var end = System.currentTimeMillis()
                mTTSListener?.onComplete(utteranceId, mTTSNotifyKeyMap.get(utteranceId) ?: "")
                ZLog.d(TAG, "TTS ${this.hashCode()} onDone $utteranceId : $lastStart $end  ${end - lastStart}")
                mTTSNotifyKeyMap.remove(utteranceId)
            }

            override fun onUtteranceError(utteranceId: String) {
                var end = System.currentTimeMillis()
                ZLog.d(TAG, "TTS ${this.hashCode()} onError $utteranceId : $lastStart $end  ${end - lastStart}")
                mTTSListener?.onError(utteranceId, mTTSNotifyKeyMap.get(utteranceId) ?: "")
                mTTSNotifyKeyMap.remove(utteranceId)
            }

            override fun onUtteranceFailed(utteranceId: String, textSpeak: String) {
                ZLog.d(TAG, "TTS ${this.hashCode()} onError $utteranceId : $textSpeak")
                initTTSImplWithKey(context, loc, enginePackageName, initListener, mTTSListener)
                mTTSListener?.onError(utteranceId, mTTSNotifyKeyMap.get(utteranceId) ?: "")
                mTTSNotifyKeyMap.remove(utteranceId)
            }
        })
    }

    open fun speak(key: String, data: TTSData, type: Int) {
        ZLog.d(TAG, "speak $data")
        mTTSNotifyKeyMap[data.getUtteranceId()] = key
        super.speak(data, type)
    }

    final override fun speak(tempStr: TTSData, type: Int) {
        speak("", tempStr, type)
    }

    final fun speak(key: String, data: TTSData) {
        speak(key, data, TTSConfig.SPEEAK_TYPE_FLUSH)
    }

    final fun speak(data: TTSData) {
        speak("", data)
    }

}
