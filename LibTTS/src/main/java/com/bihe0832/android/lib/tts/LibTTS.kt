package com.bihe0832.android.lib.tts

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
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
    private const val UNIT_CHANGE = 0.1f

    private var mSpeech: TextToSpeech? = null

    private val mMsgList = mutableListOf<String>()
    const val SPEEAK_TYPE_SEQUENCE = 1
    const val SPEEAK_TYPE_NEXT = 2
    const val SPEEAK_TYPE_FORCE = 3
    const val SPEEAK_TYPE_CLEAR = 4

    private var mUtteranceId = 1
    private var mPitch = 0.4f
    private var mSpeechRate = 0.4f

    private val mTTSSpeakListenerList = mutableListOf<TTSSpeakListener>()

    fun addTTSSpeakListener(listener: TTSSpeakListener){
        mTTSSpeakListenerList.add(listener)
    }

    fun removeTTSSpeakListener(listener: TTSSpeakListener){
        if(mTTSSpeakListenerList.contains(listener)){
            mTTSSpeakListenerList.remove(listener)
        }
    }

    private val mTTSResultListener = object : TTSSpeakListener{

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

    fun init(context: Context, listener :TTSInitListener?) {
        mSpeech = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "onInit: TTS引擎初始化成功")
                listener?.onInitSuccess()
            } else {
                Log.e(TAG, "onInit: TTS引擎初始化失败")
                listener?.onInitError()
            }
        })

        setPitch(mPitch)
        setSpeechRate(mSpeechRate)
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

        fun onInitSuccess()
    }

    interface TTSLanguageListener {
        fun onLangUnAvaiavble()

        fun onLangAvaiavble()
    }

    interface TTSSpeakListener {

        fun onUtteranceStart(utteranceId: String)

        fun onUtteranceDone(utteranceId: String)

        fun onUtteranceError(utteranceId: String)
    }

    fun setLanguage(loc: Locale, listener: TTSLanguageListener): Int {

        val supported = mSpeech?.setLanguage(loc)
        if (supported != TextToSpeech.LANG_AVAILABLE && supported != TextToSpeech.LANG_COUNTRY_AVAILABLE) {
            Log.i(TAG, "onInit: 不支持当前语言")
            listener.onLangUnAvaiavble()
        } else {
            Log.i(TAG, "onInit: 支持当前选择语言")
            listener.onLangAvaiavble()
        }
        return supported ?: TextToSpeech.ERROR
    }

    fun getLanguage(): Locale? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mSpeech?.language
        } else {
            mSpeech?.voice?.locale
        }
    }

    fun getAvailableLanguages(): Set<Locale>? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            null
        } else {
            mSpeech?.availableLanguages
        }
    }

    fun speak(tempStr: String, type: Int) {
        when (type) {
            SPEEAK_TYPE_SEQUENCE -> mMsgList.add(tempStr)
            SPEEAK_TYPE_NEXT -> mMsgList.add(0, tempStr)
            SPEEAK_TYPE_FORCE -> speak(tempStr)
            SPEEAK_TYPE_CLEAR -> {
                mMsgList.clear()
                speak(tempStr)
            }
        }
        if(mSpeech?.isSpeaking == false){
            startSpeak()
        }
    }

    fun isSpeak() : Boolean{
        return mSpeech?.isSpeaking?:false
    }

    fun hasMoreSpeak(): Boolean{
        return mMsgList.size > 0
    }

    fun startSpeak(){
        if(mMsgList.isNotEmpty()){
            var s = mMsgList[0]
            mMsgList.removeAt(0)
            speak(s)
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
        DecimalFormat("0.00").apply {
            roundingMode = RoundingMode.HALF_UP
        }.let {
            return ConvertUtils.parseFloat(it.format(mSpeechRate), mSpeechRate)
        }
    }

    fun setSpeechRate(speechRate: Float) {
        val result = mSpeech?.setSpeechRate(speechRate * 4)
        Log.i(TAG, "setSpeechRate: $result")
    }

    //增加语速
    fun increSpeechRate() {
        mSpeechRate += UNIT_CHANGE
        if (mSpeechRate > 1) {
            mSpeechRate = 1f
        }
        Log.i(TAG, "mSpeechRate: $mSpeechRate")
        setSpeechRate(mSpeechRate)
    }


    //减小语速
    fun decreSpeechRate() {
        mSpeechRate -= UNIT_CHANGE
        if (mSpeechRate <= 0) {
            mSpeechRate = 0.1f
        }
        Log.i(TAG, "mSpeechRate: $mSpeechRate")
        setSpeechRate(mSpeechRate)
    }


    fun getPitch(): Float {
        DecimalFormat("0.00").apply {
            roundingMode = RoundingMode.HALF_UP
        }.let {
            return ConvertUtils.parseFloat(it.format(mPitch), mPitch)
        }
    }

    fun setPitch(pitch: Float) {
        val result = mSpeech!!.setPitch(pitch * 2)
        Log.i(TAG, "setPitch: $result")
    }

    //升高音调
    fun increPitch() {
        mPitch += UNIT_CHANGE
        if (mPitch > 1) {
            mPitch = 1.0f
        }
        Log.i(TAG, "mPitch: $mPitch")
        setPitch(mPitch)
    }

    //减低音调
    fun decrePitch() {
        mPitch -= UNIT_CHANGE
        if (mPitch <= 0) {
            mPitch = 0.1f
        }
        Log.i(TAG, "mPitch: $mPitch")
        setPitch(mPitch)
    }


    fun onDestroy() {
        if (mSpeech != null) {
            mSpeech!!.stop()
            mSpeech!!.shutdown()
            mSpeech = null
        }
    }
}
