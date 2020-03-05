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
    private var mLocale: Locale = Locale.CHINA
    private val mContext: Context? = null
    private var mTTSResultListener: TTSResultListener? = null

    private var mUtteranceId = 1
    private var mPitch = 0.4f
    private var mSpeechRate = 0.4f


    fun init(context: Context, loc: Locale, listener: TTSResultListener?) {
        mTTSResultListener = listener
        mLocale = loc
        mSpeech = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val supported = setLanguage(mLocale)
                if (supported != TextToSpeech.LANG_AVAILABLE && supported != TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                    Log.i(TAG, "onInit: 不支持当前语言")
                    mTTSResultListener?.onLangUnAvaiavble()
                } else {
                    Log.i(TAG, "onInit: 支持当前选择语言")
                    mTTSResultListener?.onLangAvaiavble()
                }
            } else {
                Log.i(TAG, "onInit: TTS引擎初始化失败")
                mTTSResultListener?.onLangError()
            }
        })

        setPitch(mPitch)
        setSpeechRate(mSpeechRate)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            mSpeech?.setOnUtteranceCompletedListener { utteranceId ->
                mTTSResultListener?.onUtteranceDone(utteranceId ?: "")
            }
        } else {
            mSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {
                    mTTSResultListener?.onUtteranceStart(utteranceId)
                }

                override fun onDone(utteranceId: String) {
                    mTTSResultListener?.onUtteranceDone(utteranceId)
                }

                override fun onError(utteranceId: String) {
                    mTTSResultListener?.onUtteranceError(utteranceId)
                }
            })
        }
    }


    interface TTSResultListener {
        fun onLangUnAvaiavble()

        fun onLangAvaiavble()

        fun onLangError()

        fun onUtteranceStart(utteranceId: String)

        fun onUtteranceDone(utteranceId: String)

        fun onUtteranceError(utteranceId: String)
    }

    fun setLanguage(loc: Locale): Int {
        return mSpeech?.setLanguage(loc) ?: TextToSpeech.ERROR
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
