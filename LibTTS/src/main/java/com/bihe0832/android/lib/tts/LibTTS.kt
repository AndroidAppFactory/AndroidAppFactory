package com.bihe0832.android.lib.tts

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.TextUtils
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.ConvertUtils
import java.io.File
import java.lang.reflect.Field
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap


/**
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 * Description: Description
 */
@SuppressLint("StaticFieldLeak")
object LibTTS {

    private const val TAG = "TTSHelper"

    private val CONFIG_KEY_PITCH = this.javaClass.name.toString() + "pitch"
    private val CONFIG_KEY_SPEECH_RATE = this.javaClass.name.toString() + "speech.rate"
    private val CONFIG_KEY_ENGINE = this.javaClass.name.toString() + "engine"

    const val SPEEAK_TYPE_SEQUENCE = 1
    const val SPEEAK_TYPE_NEXT = 2
    const val SPEEAK_TYPE_FLUSH = 3
    const val SPEEAK_TYPE_CLEAR = 4

    const val CONFIG_VALUE_ENGINE = "com.google.android.tts"
    private const val CONFIG_VALUE_PITCH = 0.4f
    private const val CONFIG_VALUE_SPEECH_RATE = 0.4f

    private var mUtteranceId = 1

    private var mSpeech: TextToSpeech? = null
    private var mContext: Context? = null
    private var mLocale: Locale? = null

    private val mMsgList by lazy {
        mutableListOf<String>()
    }

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
        if (mTTSInitListenerList.contains(listener)) {
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

        override fun onUtteranceFailed(utteranceId: String, textSpeak: String) {
            mTTSSpeakListenerList.forEach {
                it.onUtteranceFailed(utteranceId, textSpeak)
            }
        }
    }

    fun init(context: Context, loc: Locale, engine: String?, listener: TTSInitListener) {
        mContext = context
        mLocale = loc
        mTTSInitListenerList.add(listener)
        engine?.let {
            setEngine(it)
        }
        initTTS()
        mSpeech?.engines?.forEach {
            ZLog.e(TAG, "onInit: 引擎列表：" + it.label + " " + it.name)
            if (it.name == mSpeech!!.defaultEngine) {
                ZLog.e(TAG, "onInit: 默认引擎：" + it.label + " " + it.name)
            }
        }
    }

    fun init(context: Context, loc: Locale, listener: TTSInitListener) {
        init(context, loc, null, listener)
    }


    private fun initTTS() {
        if (mContext != null && mLocale != null) {
            var enginePackageName = Config.readConfig(CONFIG_KEY_ENGINE, CONFIG_VALUE_ENGINE).let {
                if (it.isEmpty()) {
                    null
                } else {
                    it
                }
            }
            try {
                mSpeech?.shutdown()
            } catch (e: Exception) {
                e.printStackTrace();
            }

            mSpeech = TextToSpeech(mContext, TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ZLog.d(TAG, "onInit: TTS引擎初始化成功")
                    setLanguage(mLocale!!)
                } else {
                    ZLog.e(TAG, "onInit: TTS引擎初始化失败")
                    mTTSInitListenerList.forEach {
                        it.onInitError()
                    }
                }
            }, enginePackageName)
            setPitch(Config.readConfig(CONFIG_KEY_PITCH, getDefaultPitch()))
            setSpeechRate(Config.readConfig(CONFIG_KEY_SPEECH_RATE, getDefaultSpeechRate()))
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
        } else {
            ZLog.e(TAG, "onInit: TTS引擎初始化失败，参数失败")
            mTTSInitListenerList.forEach {
                it.onInitError()
            }
        }
    }

    interface TTSInitListener {

        fun onInitError()

        fun onTTSError()

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
        val supported = mSpeech?.setLanguage(loc)
        if (supported != TextToSpeech.LANG_AVAILABLE && supported != TextToSpeech.LANG_COUNTRY_AVAILABLE) {
            ZLog.i(TAG, "onInit: 不支持当前语言")
            mTTSInitListenerList.forEach {
                it.onLangUnAvailable()
            }
        } else {
            mTTSInitListenerList.forEach {
                it.onLangAvailable()
            }
            ZLog.i(TAG, "onInit: 支持当前选择语言")
            startSpeak()
        }
        return supported ?: TextToSpeech.ERROR
    }

    fun isTTSServiceOK(tts: TextToSpeech?): Boolean {
        var isBindConnection = true
        if (tts == null) {
            return false
        }
        val fields: Array<Field> = tts.javaClass.getDeclaredFields()
        for (j in fields.indices) {
            fields[j].setAccessible(true)
            if (TextUtils.equals("mServiceConnection", fields[j].getName()) && TextUtils.equals("android.speech.tts.TextToSpeech\$Connection", fields[j].getType().getName())) {
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

    fun isSpeak(): Boolean {
        return mSpeech?.isSpeaking ?: false
    }

    fun hasMoreSpeak(): Boolean {
        return mMsgList.size > 0
    }

    fun startSpeak() {
        if (!isTTSServiceOK(mSpeech)) {
            ZLog.e(TAG, "TTS引擎异常，重新再次初始化")
            initTTS()
        } else {
            if (mMsgList.isNotEmpty()) {
                var s = mMsgList[0]
                mMsgList.removeAt(0)
                speak(s)
            }
        }
    }

    fun stopSpeak() {
        mSpeech?.stop()
        mSpeech?.shutdown()
        mMsgList.clear()
    }

    fun speak(tempStr: String, type: Int) {
        when (type) {
            SPEEAK_TYPE_SEQUENCE -> {
                mMsgList.add(tempStr)
                if (mSpeech?.isSpeaking == false) {
                    startSpeak()
                }
            }
            SPEEAK_TYPE_NEXT -> {
                mMsgList.add(0, tempStr)
                if (mSpeech?.isSpeaking == false) {
                    startSpeak()
                }
            }
            SPEEAK_TYPE_FLUSH -> {
                mMsgList.add(0, tempStr)
                startSpeak()
            }
            SPEEAK_TYPE_CLEAR -> {
                mMsgList.clear()
                mMsgList.add(0, tempStr)
                startSpeak()
            }
        }
    }


    private fun speak(tempStr: String) {
        mUtteranceId++
        ZLog.e(TAG, "mUtteranceId: $mUtteranceId $tempStr")
        var result = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mSpeech?.speak(tempStr, TextToSpeech.QUEUE_FLUSH, null)
        } else {
            mSpeech?.speak(tempStr, TextToSpeech.QUEUE_FLUSH, null, mUtteranceId.toString())
        }

        if (result == TextToSpeech.ERROR) {
            mTTSResultListener.onUtteranceFailed(mUtteranceId.toString(), tempStr)
            initTTS()
        }
    }

    fun save(tempStr: String, finalFileName: String): Int {
        if (!isTTSServiceOK(mSpeech)) {
            ZLog.e(TAG, "TTS引擎异常，重新再次初始化")
            initTTS()
        }
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
        ZLog.i(TAG, "saveAudioFile: $finalFileName 文件保存结果： $result")
        return result
    }


    fun getSpeechRate(): Float {
        val speechRate = Config.readConfig(CONFIG_KEY_SPEECH_RATE, getDefaultSpeechRate())
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
        } else if (tempmSpeechRate <= 0) {
            tempmSpeechRate = 0.1f
        }
        val result = mSpeech?.setSpeechRate(speechRate * 4)
        val result1 = Config.writeConfig(CONFIG_KEY_SPEECH_RATE, tempmSpeechRate)

        ZLog.i(TAG, "setSpeechRate: $speechRate $tempmSpeechRate $result $result1")
    }


    fun getPitch(): Float {
        val pitch = Config.readConfig(CONFIG_KEY_PITCH, getDefaultPitch())

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
        ZLog.i(TAG, "setPitch: $pitch $tempPitch $result $result1")
    }

    private fun setEngine(tempEngine: String) {
        Config.writeConfig(CONFIG_KEY_ENGINE, tempEngine)
//        APKUtils.getInstalledPackage(mContext, tempEngine).let { packageInfo ->
//            if (null == packageInfo || TextUtils.isEmpty(packageInfo?.packageName)) {
//                APKUtils.getInstalledPackage(mContext, CONFIG_VALUE_ENGINE).let { androidTTS ->
//                    if (null == androidTTS || TextUtils.isEmpty(androidTTS?.packageName)) {
//                        Config.writeConfig(CONFIG_KEY_ENGINE, "")
//                    } else {
//                        val result = Config.writeConfig(CONFIG_KEY_ENGINE, androidTTS?.packageName)
//                        ZLog.i(TAG, "setEngine: ${androidTTS?.packageName} ; result $result")
//                    }
//                }
//            } else {
//                val result = Config.writeConfig(CONFIG_KEY_ENGINE, packageInfo?.packageName)
//                ZLog.i(TAG, "setEngine: ${packageInfo?.packageName} ; result $result")
//            }
//        }
    }

    fun getEngines(): List<TextToSpeech.EngineInfo>? {
        return mSpeech?.engines
    }

    fun getDefaultEngine(): String? {
        return mSpeech?.defaultEngine
    }

    fun onDestroy() {
        if (mSpeech != null) {
            mSpeech!!.stop()
            mSpeech!!.shutdown()
            mSpeech = null
        }
    }
}
