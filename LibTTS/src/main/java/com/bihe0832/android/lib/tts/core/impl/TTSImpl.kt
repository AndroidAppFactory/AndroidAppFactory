package com.bihe0832.android.lib.tts.core.impl

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.TextUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.tts.core.TTSConfig
import com.bihe0832.android.lib.tts.core.TTSData
import com.bihe0832.android.lib.utils.os.BuildUtils
import java.io.File
import java.lang.ref.WeakReference
import java.lang.reflect.Field
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 * Description: Description
 */
@SuppressLint("StaticFieldLeak")
open class TTSImpl {

    companion object {
        const val TAG = "TTS"
    }

    private var mSpeech: TextToSpeech? = null
    private var mContext: WeakReference<Context>? = null
    private var mLocale: Locale = Locale.SIMPLIFIED_CHINESE
    private var mVolume = 100

    private var mNeedStopAfterSpeak = false
    private var isSpeakIng = false
    protected var enginePackageName: String? = null

    private val mMsgList by lazy {
        CopyOnWriteArrayList<TTSData>()
    }

    private var mTTSSpeakListener: TTSSpeakListener? = null
    private var mTTSInitListener: TTSInitListener? = null

    private val mTTSResultListener = object : TTSSpeakListener {

        override fun onUtteranceStart(utteranceId: String) {
            mTTSSpeakListener?.onUtteranceStart(utteranceId)
        }

        override fun onUtteranceDone(utteranceId: String) {
            mTTSSpeakListener?.onUtteranceDone(utteranceId)

            if (mNeedStopAfterSpeak) {
                onDestroy()
                mNeedStopAfterSpeak = false
            }
        }

        override fun onUtteranceError(utteranceId: String) {
            mTTSSpeakListener?.onUtteranceError(utteranceId)
        }

        override fun onUtteranceFailed(utteranceId: String, textSpeak: String) {
            mTTSSpeakListener?.onUtteranceFailed(utteranceId, textSpeak)
        }
    }

    fun initTTSImpl(
        context: Context,
        loc: Locale,
        engine: String?,
        initListener: TTSInitListener?,
        speakListener: TTSSpeakListener?,
    ) {
        mContext = WeakReference(context)
        mLocale = loc
        mTTSInitListener = initListener
        mTTSSpeakListener = speakListener
        enginePackageName = engine
        initTTSAndSpeak(null)
        mSpeech?.engines?.forEach {
            ZLog.e(TAG, "onInit: 引擎列表：" + it.label + " " + it.name)
            if (it.name == mSpeech!!.defaultEngine) {
                ZLog.e(TAG, "onInit: 默认引擎：" + it.label + " " + it.name)
            }
        }
    }

    private fun initTTSAndSpeak(ttsData: TTSData?) {
        if (mContext?.get() != null) {
            mSpeech = TextToSpeech(
                mContext?.get(),
                { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        ZLog.d(TAG, "onInit: TTS引擎初始化成功")
                        setLanguage(mLocale!!)
                        ttsData?.let {
                            if (speak(ttsData) == TextToSpeech.ERROR) {
                                mTTSResultListener.onUtteranceFailed(
                                    ttsData.getUtteranceId(),
                                    ttsData.speakText
                                )
                            }
                        }
                    } else {
                        ZLog.e(TAG, "onInit: TTS引擎初始化失败")
                        mTTSInitListener?.onInitError()
                        if (null != ttsData) {
                            mTTSResultListener.onUtteranceFailed(
                                ttsData.getUtteranceId(),
                                ttsData.speakText
                            )
                        }
                    }
                },
                enginePackageName,
            )
            if (BuildUtils.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                mSpeech?.setOnUtteranceCompletedListener { utteranceId ->
                    mTTSResultListener.onUtteranceDone(utteranceId ?: "")
                    isSpeakIng = false
                    startSpeak()
                }
            } else {
                mSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String) {
                        mTTSResultListener.onUtteranceStart(utteranceId)
                    }

                    override fun onDone(utteranceId: String) {
                        mTTSResultListener.onUtteranceDone(utteranceId)
                        isSpeakIng = false
                        startSpeak()
                    }

                    override fun onError(utteranceId: String) {
                        mTTSResultListener.onUtteranceError(utteranceId)
                        isSpeakIng = false
                    }
                })
            }
        } else {
            ZLog.e(TAG, "onInit: TTS引擎初始化失败，参数失败，请先初始化后重试")
            isSpeakIng = false
            mTTSInitListener?.onInitError()
            if (null != ttsData) {
                mTTSResultListener.onUtteranceFailed(ttsData.getUtteranceId(), ttsData.speakText)
            }
        }
    }

    interface TTSInitListener {

        fun onInitError()

        fun onLangUnAvailable()

        fun onLangAvailable()
    }

    interface TTSSpeakListener {

        fun onUtteranceStart(utteranceId: String)

        fun onUtteranceDone(utteranceId: String)

        fun onUtteranceError(utteranceId: String)

        fun onUtteranceFailed(utteranceId: String, textSpeak: String)
    }

    private fun setLanguage(loc: Locale): Int {
        var supported = TextToSpeech.ERROR
        try {
            supported = mSpeech?.setLanguage(loc) ?: TextToSpeech.ERROR
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (supported != TextToSpeech.LANG_AVAILABLE && supported != TextToSpeech.LANG_COUNTRY_AVAILABLE) {
            ZLog.i(TAG, "onInit: 不支持当前语言")
            mTTSInitListener?.onLangUnAvailable()
        } else {
            mTTSInitListener?.onLangAvailable()
            ZLog.i(TAG, "onInit: 支持当前选择语言")
            startSpeak()
        }
        return supported
    }

    fun isSpeak(): Boolean {
        return isSpeakIng
    }

    fun hasMoreSpeak(): Boolean {
        return mMsgList.isNotEmpty()
    }

    @Synchronized
    fun startSpeak() {
        if (mMsgList.isNotEmpty()) {
            var s = mMsgList[0]
            mMsgList.removeAt(0)
            speakWithTry(s)
        }
    }

    fun stopSpeak() {
        ZLog.e(TAG, "stopSpeak")
        if (isSpeak()) {
            mNeedStopAfterSpeak = true
        } else {
            onDestroy()
        }
    }

    fun forceStop() {
        onDestroy()
    }

    open fun speak(tempStr: TTSData, type: Int) {
        ZLog.e(TAG, "speak: ${mSpeech.hashCode()} ${isSpeak()} ${mSpeech?.isSpeaking} $tempStr")
        if (!tempStr.getSpeakBundle().containsKey(TextToSpeech.Engine.KEY_PARAM_VOLUME)) {
            tempStr.addSpeakParams(
                Bundle().apply {
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, getVoiceVolumeFloat())
                },
            )
        }

        if (TextUtils.isEmpty(tempStr.speakText)) {
            return
        }
        when (type) {
            TTSConfig.SPEEAK_TYPE_SEQUENCE -> {
                mMsgList.add(tempStr)
                if (!isSpeak()) {
                    startSpeak()
                }
            }

            TTSConfig.SPEEAK_TYPE_NEXT -> {
                mMsgList.add(0, tempStr)
                if (!isSpeak()) {
                    startSpeak()
                }
            }

            TTSConfig.SPEEAK_TYPE_FLUSH -> {
                mMsgList.add(0, tempStr)
                startSpeak()
            }

            TTSConfig.SPEEAK_TYPE_CLEAR -> {
                mMsgList.clear()
                mMsgList.add(0, tempStr)
                startSpeak()
            }
        }
    }

    private fun speakWithTry(ttsData: TTSData) {
        ZLog.e(TAG, "speakWithTry ttsData: $ttsData ")
        if (!isTTSServiceOK(mSpeech)) {
            initTTSAndSpeak(ttsData)
        } else {
            var result = TextToSpeech.ERROR
            try {
                result = speak(ttsData) ?: TextToSpeech.ERROR
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ZLog.d(TAG, "speakWithTry result: $result ")
            if (result == TextToSpeech.ERROR) {
                initTTSAndSpeak(ttsData)
            }
        }
    }

    fun isTTSServiceOK(tts: TextToSpeech?): Boolean {
        var isBindConnection = true
        if (tts == null) {
            return false
        }
        val fields: Array<Field> = tts.javaClass.getDeclaredFields()
        for (j in fields.indices) {
            fields[j].setAccessible(true)
            if (TextUtils.equals(
                    "mServiceConnection",
                    fields[j].getName(),
                ) && TextUtils.equals(
                    "android.speech.tts.TextToSpeech\$Connection",
                    fields[j].getType().getName()
                )
            ) {
                try {
                    if (fields[j].get(tts) == null) {
                        isBindConnection = false
                        ZLog.e(TAG, "******* TTS -> mServiceConnection == null*******")
                    }
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        return isBindConnection
    }

    private fun speak(ttsData: TTSData): Int? {
        val result = if (BuildUtils.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mSpeech?.speak(ttsData.speakText, TextToSpeech.QUEUE_FLUSH, ttsData.getSpeakMap())
        } else {
            mSpeech?.speak(
                ttsData.speakText,
                TextToSpeech.QUEUE_FLUSH,
                ttsData.getSpeakBundle(),
                ttsData.getUtteranceId(),
            )
        }
        isSpeakIng = (TextToSpeech.SUCCESS == result)
        ZLog.e(TAG, "real speak ttsData result $result ,ttsData: $ttsData ")
        return result
    }

    fun save(ttsData: TTSData, finalFileName: String): Int {
        ZLog.e(TAG, "ttsData: $ttsData")
        val result = if (BuildUtils.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mSpeech?.speak(ttsData.speakText, TextToSpeech.QUEUE_FLUSH, ttsData.getSpeakMap())
                ?: TextToSpeech.ERROR
        } else {
            mSpeech?.synthesizeToFile(
                ttsData.speakText,
                ttsData.getSpeakBundle(),
                File(finalFileName),
                ttsData.speakText,
            ) ?: TextToSpeech.ERROR
        }
        ZLog.i(TAG, "saveAudioFile: $finalFileName 文件保存结果： $result")
        return result
    }


    open fun setSpeechRate(speechRate: Float) {
        val tempSpeechRate = if (speechRate < 0) {
            0f
        } else {
            speechRate
        }
        val result = mSpeech?.setSpeechRate(tempSpeechRate)
        ZLog.i(TAG, "setSpeechRate:  $tempSpeechRate $result")
    }


    open fun setPitch(pitch: Float) {
        val tempPitch = if (pitch < 0) {
            0f
        } else {
            pitch
        }

        val result = mSpeech?.setPitch(tempPitch)
        ZLog.i(TAG, "setPitch: $pitch $tempPitch $result")
    }

    fun getEngines(): List<TextToSpeech.EngineInfo>? {
        return mSpeech?.engines
    }

    fun getDefaultEngine(): String? {
        return mSpeech?.defaultEngine
    }

    open fun setVoiceVolume(paramVolume: Int): Int {
        val volume = if (paramVolume in 0..100) {
            paramVolume
        } else if (paramVolume < 0) {
            0
        } else {
            100
        }
        mVolume = volume
        ZLog.i(TAG, "setVoiceVolume ${this.hashCode()}: $paramVolume $volume")
        return volume
    }

    fun getVoiceVolumeFloat(): Float {
        return mVolume / 100f
    }

    fun getVoiceVolumeInt(): Int {
        return mVolume
    }

    fun onDestroy() {
        ZLog.e(TAG, "onDestroy")

        if (mSpeech != null) {
            mSpeech!!.stop()
            mSpeech!!.shutdown()
            mSpeech = null
        }
        mNeedStopAfterSpeak = false
    }
}
