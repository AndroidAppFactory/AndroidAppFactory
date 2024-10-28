package com.bihe0832.android.lib.tts.core.impl

import android.content.Context
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.tts.core.TTSConfig
import com.bihe0832.android.lib.tts.core.TTSData
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020/3/24.
 * Description: Description
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

    override fun initTTSImpl(
        context: Context,
        loc: Locale,
        engine: String?,
        initListener: TTSInitListener?,
        speakListener: TTSSpeakListener?,
    ) {
        ZLog.e(TAG, "TTSImplNotifyWithKey init,  TTSSpeakListener can not used, please use TTSListener !!!")
        super.initTTSImpl(context, loc, engine, initListener, speakListener)
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
