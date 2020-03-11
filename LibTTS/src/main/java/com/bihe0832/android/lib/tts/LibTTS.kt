package com.bihe0832.android.lib.tts

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.utils.ConvertUtils
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap


/**
 * @author hardyshi code@bihe0832.com
 * Created on 2020-03-05.
 * Description: Description
 */
@SuppressLint("StaticFieldLeak")
object LibTTS {

    private const val TAG = "TTSHelper"
    private val CONFIG_KEY_PITCH = this.javaClass.name.toString() + "pitch"
    private val CONFIG_KEY_SPEECH_RATE = this.javaClass.name.toString() + "speech.rate"

    private const val CONFIG_VALUE_PITCH = 0.4f
    private const val CONFIG_VALUE_SPEECH_RATE = 0.4f

    private var mSpeech: TextToSpeech? = null

    private val mMsgList = mutableListOf<String>()
    const val SPEEAK_TYPE_SEQUENCE = 1
    const val SPEEAK_TYPE_NEXT = 2
    const val SPEEAK_TYPE_FLUSH = 3
    const val SPEEAK_TYPE_CLEAR = 4

    private var mUtteranceId = 1

    private val mTTSSpeakListenerList = mutableListOf<TTSSpeakListener>()
    private val mTTSInitListenerList = mutableListOf<TTSInitListener>()

    fun addTTSSpeakListener(listener: TTSSpeakListener) {
        mTTSSpeakListenerList.add(listener)
    }

    fun removeTTSSpeakListener(listener: TTSSpeakListener) {
        if (mTTSSpeakListenerList.contains(listener)) {
            mTTSSpeakListenerList.remove(listener)
        }
    }

    fun addTTSInitListener(listener: TTSInitListener) {
        if(mTTSInitListenerList.contains(listener)){
            return
        }
        mTTSInitListenerList.add(listener)
    }

    fun removeTTSInitListener(listener: TTSInitListener) {
        if (mTTSInitListenerList.contains(listener)) {
            mTTSInitListenerList.remove(listener)
        }
    }

    private val mTTSResultListener = object : TTSSpeakListener {

        override fun onUtteranceStart(utteranceId: String) {
            mTTSSpeakListenerList.forEach {
                it.onUtteranceStart(utteranceId)
            }
        }

        override fun onUtteranceDone(utteranceId: String) {
            mTTSSpeakListenerList.forEach {
                it.onUtteranceDone(utteranceId)
            }
        }

        override fun onUtteranceError(utteranceId: String) {
            mTTSSpeakListenerList.forEach {
                it.onUtteranceError(utteranceId)
            }
        }
    }

    fun init(context: Context, loc: Locale, listener: TTSInitListener) {
        mTTSInitListenerList.add(listener)
        mSpeech = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "onInit: TTS引擎初始化成功")
                setLanguage(loc)
            } else {
                Log.e(TAG, "onInit: TTS引擎初始化失败")
                mTTSInitListenerList.forEach {
                    it.onInitError()
                }
            }
        })

        setPitch(Config.readConfig(CONFIG_KEY_PITCH, CONFIG_VALUE_PITCH))
        setSpeechRate(Config.readConfig(CONFIG_KEY_SPEECH_RATE, CONFIG_VALUE_SPEECH_RATE))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            mSpeech?.setOnUtteranceCompletedListener { utteranceId ->
                mTTSResultListener.onUtteranceDone(utteranceId ?: "")
            }
        } else {
            mSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {
                    mTTSResultListener.onUtteranceStart(utteranceId)
                }

                override fun onDone(utteranceId: String) {
                    mTTSResultListener.onUtteranceDone(utteranceId)
                    startSpeak()
                }

                override fun onError(utteranceId: String) {
                    mTTSResultListener.onUtteranceError(utteranceId)
                }
            })
        }

    }


    interface TTSInitListener {

        fun onInitError()

        fun onLangUnAvaiavble()

        fun onLangAvaiavble()
    }

    interface TTSSpeakListener {

        fun onUtteranceStart(utteranceId: String)

        fun onUtteranceDone(utteranceId: String)

        fun onUtteranceError(utteranceId: String)
    }

    fun setLanguage(loc: Locale, listener: TTSInitListener): Int {
        mTTSInitListenerList.add(listener)
        return setLanguage(loc)
    }

    private fun setLanguage(loc: Locale): Int {
        val supported = mSpeech?.setLanguage(loc)
        if (supported != TextToSpeech.LANG_AVAILABLE && supported != TextToSpeech.LANG_COUNTRY_AVAILABLE) {
            Log.i(TAG, "onInit: 不支持当前语言")
            mTTSInitListenerList.forEach {
                it.onLangUnAvaiavble()
            }
        } else {
            mTTSInitListenerList.forEach {
                it.onLangAvaiavble()
            }
            Log.i(TAG, "onInit: 支持当前选择语言")
        }
        return supported ?: TextToSpeech.ERROR
    }

    fun getLanguage(): String {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mSpeech?.language?.language ?: ""
        } else {
            mSpeech?.voice?.locale?.language ?: ""
        }
    }

    fun isLanguageAvailable(laca: Locale): Int {
        return mSpeech?.isLanguageAvailable(laca) ?: TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun getAvailableLanguages(): Set<Locale>? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            null
        } else {
            mSpeech?.availableLanguages
        }
    }



    fun isSpeak(): Boolean {
        return mSpeech?.isSpeaking ?: false
    }

    fun hasMoreSpeak(): Boolean {
        return mMsgList.size > 0
    }

    fun startSpeak() {
        if (mMsgList.isNotEmpty()) {
            var s = mMsgList[0]
            mMsgList.removeAt(0)
            speak(s)
        }
    }

    fun stopSpeak() {
        mSpeech?.stop()
        mMsgList.clear()
    }

    fun speak(tempStr: String, type: Int) {
        when (type) {
            SPEEAK_TYPE_SEQUENCE -> mMsgList.add(tempStr)
            SPEEAK_TYPE_NEXT -> mMsgList.add(0, tempStr)
            SPEEAK_TYPE_FLUSH -> speak(tempStr)
            SPEEAK_TYPE_CLEAR -> {
                mMsgList.clear()
                speak(tempStr)
            }
        }
        if (mSpeech?.isSpeaking == false) {
            startSpeak()
        }
    }


    fun speak(tempStr: String) {
        mUtteranceId++
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mSpeech?.speak(tempStr, TextToSpeech.QUEUE_FLUSH, null)
        } else {
            mSpeech?.speak(tempStr, TextToSpeech.QUEUE_FLUSH, null, mUtteranceId.toString())
        }
    }

    fun save(tempStr: String, finalFileName: String): Int {
        mUtteranceId++
        var result = TextToSpeech.ERROR
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            HashMap<String, String>().apply {
                put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mUtteranceId.toString())
            }.let {
                result = mSpeech?.speak(tempStr, TextToSpeech.QUEUE_FLUSH, it) ?: TextToSpeech.ERROR
            }
        } else {
            val bundle = Bundle()
            bundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mUtteranceId.toString())
            result = mSpeech?.synthesizeToFile(tempStr, bundle, File(finalFileName), mUtteranceId.toString())
                    ?: TextToSpeech.ERROR

        }
        Log.i(TAG, "saveAudioFile: $finalFileName 文件保存结果： $result")
        return result
    }


    fun getSpeechRate(): Float {
        val speechRate = Config.readConfig(CONFIG_KEY_SPEECH_RATE, CONFIG_VALUE_SPEECH_RATE)
        DecimalFormat("0.00").apply {
            roundingMode = RoundingMode.HALF_UP
        }.let {
            return ConvertUtils.parseFloat(it.format(speechRate), speechRate)
        }
    }

    fun getDefaultSpeechRate(): Float {
        return CONFIG_VALUE_SPEECH_RATE
    }

    fun setSpeechRate(speechRate: Float) {
        var tempmSpeechRate = speechRate
        if (tempmSpeechRate > 1) {
            tempmSpeechRate = 1f
        } else if (tempmSpeechRate <=0) {
            tempmSpeechRate = 0.1f
        }
        val result = mSpeech?.setSpeechRate(speechRate * 4)
        val result1 = Config.writeConfig(CONFIG_KEY_SPEECH_RATE, tempmSpeechRate)

        Log.i(TAG, "setSpeechRate: $speechRate $tempmSpeechRate $result $result1")
    }


    fun getPitch(): Float {
        val pitch = Config.readConfig(CONFIG_KEY_PITCH, CONFIG_VALUE_PITCH)

        DecimalFormat("0.00").apply {
            roundingMode = RoundingMode.HALF_UP
        }.let {
            return ConvertUtils.parseFloat(it.format(pitch), pitch)
        }
    }

    fun getDefaultPitch(): Float {
        return CONFIG_VALUE_PITCH
    }

    fun setPitch(pitch: Float) {
        var tempPitch = pitch

        if (tempPitch > 1) {
            tempPitch = 1f
        } else if (tempPitch <= 0) {
            tempPitch = 0.1f
        }
        val result = mSpeech?.setPitch(tempPitch * 2)
        val result1 = Config.writeConfig(CONFIG_KEY_PITCH, tempPitch)
        Log.i(TAG, "setPitch: $pitch $tempPitch $result $result1")
    }

    fun onDestroy() {
        if (mSpeech != null) {
            mSpeech!!.stop()
            mSpeech!!.shutdown()
            mSpeech = null
        }
    }
}
