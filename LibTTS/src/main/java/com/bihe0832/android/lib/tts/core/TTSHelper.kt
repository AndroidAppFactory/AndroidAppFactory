package com.bihe0832.android.lib.tts.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.TextUtils
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.os.BuildUtils
import java.io.File
import java.lang.reflect.Field
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 * Description: Description
 */
@SuppressLint("StaticFieldLeak")
class TTSHelper {

    private val TAG = "TTSHelper"

    val CONFIG_VALUE_ENGINE = "com.google.android.tts"
    private val CONFIG_VALUE_PITCH = 0.4f
    private val CONFIG_VALUE_SPEECH_RATE = 0.4f

    private var mSpeech: TextToSpeech? = null
    private var mContext: Context? = null
    private var mLocale: Locale = Locale.CHINA
    private var mNeedStopAfterSpeak = false
    private var isSpeakIng = false
    private var mScene = ""

    private val mMsgList by lazy {
        CopyOnWriteArrayList<TTSData>()
    }

    private val mTTSSpeakListenerList = CopyOnWriteArrayList<TTSSpeakListener>()
    private val mTTSInitListenerList = CopyOnWriteArrayList<TTSInitListener>()

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

            if (mNeedStopAfterSpeak) {
                onDestroy()
                mNeedStopAfterSpeak = false
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

    fun init(context: Context, scene: String, loc: Locale, listener: TTSInitListener? = null) {
        init(context, scene, loc, null, listener)
    }

    fun init(context: Context, scene: String, loc: Locale, engine: String?, listener: TTSInitListener?) {
        mContext = context
        mLocale = loc
        mScene = scene
        listener?.let {
            mTTSInitListenerList.add(listener)
        }
        engine?.let {
            setEngine(it)
        }
        initTTSAndSpeak(scene, null)
        mSpeech?.engines?.forEach {
            ZLog.e(TAG, "onInit: 引擎列表：" + it.label + " " + it.name)
            if (it.name == mSpeech!!.defaultEngine) {
                ZLog.e(TAG, "onInit: 默认引擎：" + it.label + " " + it.name)
            }
        }
    }

    private fun initTTSAndSpeak(scene: String, ttsData: TTSData?) {
        if (mContext != null && mLocale != null) {
            val enginePackageName = Config.readConfig(TTSConfig.CONFIG_KEY_ENGINE + scene, CONFIG_VALUE_ENGINE).let {
                if (it.isEmpty()) {
                    null
                } else {
                    it
                }
            }
            mSpeech = TextToSpeech(
                mContext,
                TextToSpeech.OnInitListener { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        ZLog.d(TAG, "onInit: TTS引擎初始化成功")
//                    lastInitTime = System.currentTimeMillis()
                        initConfig(scene)
                        setLanguage(mLocale!!)
                        ttsData?.let {
                            if (speak(ttsData) == TextToSpeech.ERROR) {
                                mTTSResultListener.onUtteranceFailed(ttsData.getUtteranceId(), ttsData.speakText)
                            }
                        }
                    } else {
                        ZLog.e(TAG, "onInit: TTS引擎初始化失败")
                        mTTSInitListenerList.forEach {
                            it.onInitError()
                        }
                        if (null != ttsData) {
                            mTTSResultListener.onUtteranceFailed(ttsData.getUtteranceId(), ttsData.speakText)
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
            ZLog.e(TAG, "onInit: TTS引擎初始化失败，参数失败")
            isSpeakIng = false
            mTTSInitListenerList.forEach {
                it.onInitError()
            }
            if (null != ttsData) {
                mTTSResultListener.onUtteranceFailed(ttsData.getUtteranceId(), ttsData.speakText)
            }
        }
    }

    private fun initConfig(scene: String) {
        setPitch(Config.readConfig(TTSConfig.CONFIG_KEY_PITCH + scene, getDefaultPitch()))
        setSpeechRate(Config.readConfig(TTSConfig.CONFIG_KEY_SPEECH_RATE + scene, getDefaultSpeechRate()))
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
        var supported = TextToSpeech.ERROR
        try {
            supported = mSpeech?.setLanguage(loc) ?: TextToSpeech.ERROR
        } catch (e: Exception) {
            e.printStackTrace()
        }

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

    fun speak(tempStr: TTSData, type: Int) {
        ZLog.e(TAG, "speak:$tempStr $mSpeech ${isSpeak()} ${mSpeech?.isSpeaking}")
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
            initTTSAndSpeak(mScene, ttsData)
        } else {
            var result = TextToSpeech.ERROR
            try {
                result = speak(ttsData) ?: TextToSpeech.ERROR
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ZLog.e(TAG, "speakWithTry result: $result ")
            if (result == TextToSpeech.ERROR) {
                initTTSAndSpeak(mScene, ttsData)
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
                    ) && TextUtils.equals("android.speech.tts.TextToSpeech\$Connection", fields[j].getType().getName())
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
        ZLog.e(TAG, "speak ttsData result $result ,ttsData: $ttsData ")
        return result
    }

    fun save(ttsData: TTSData, finalFileName: String): Int? {
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
            )
                ?: TextToSpeech.ERROR
        }
        ZLog.i(TAG, "saveAudioFile: $finalFileName 文件保存结果： $result")
        return result
    }

    fun getSpeechRate(): Float {
        val speechRate = Config.readConfig(TTSConfig.CONFIG_KEY_SPEECH_RATE + mScene, getDefaultSpeechRate())
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
        if (tempmSpeechRate < 0) {
            tempmSpeechRate = 0.1f
        }
        val result = mSpeech?.setSpeechRate(speechRate * 4)
        val result1 = Config.writeConfig(TTSConfig.CONFIG_KEY_SPEECH_RATE + mScene, tempmSpeechRate)

        ZLog.i(TAG, "setSpeechRate: $speechRate $tempmSpeechRate $result $result1")
    }

    fun getPitch(): Float {
        val pitch = Config.readConfig(TTSConfig.CONFIG_KEY_PITCH + mScene, getDefaultPitch())

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

        if (tempPitch < 0) {
            tempPitch = 0.1f
        }
        val result = mSpeech?.setPitch(tempPitch * 2)
        val result1 = Config.writeConfig(TTSConfig.CONFIG_KEY_PITCH + mScene, tempPitch)
        ZLog.i(TAG, "setPitch: $pitch $tempPitch $result $result1")
    }

    private fun setEngine(tempEngine: String) {
        Config.writeConfig(TTSConfig.CONFIG_KEY_ENGINE + mScene, tempEngine)
//        APKUtils.getInstalledPackage(mContext, tempEngine).let { packageInfo ->
//            if (null == packageInfo || TextUtils.isEmpty(packageInfo?.packageName)) {
//                APKUtils.getInstalledPackage(mContext, CONFIG_VALUE_ENGINE).let { androidTTS ->
//                    if (null == androidTTS || TextUtils.isEmpty(androidTTS?.packageName)) {
//                        Config.writeConfig(CONFIG_KEY_ENGINE+ mScene, "")
//                    } else {
//                        val result = Config.writeConfig(CONFIG_KEY_ENGINE+ mScene, androidTTS?.packageName)
//                        ZLog.i(TAG, "setEngine: ${androidTTS?.packageName} ; result $result")
//                    }
//                }
//            } else {
//                val result = Config.writeConfig(CONFIG_KEY_ENGINE+ mScene, packageInfo?.packageName)
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
        ZLog.e(TAG, "onDestroy")

        if (mSpeech != null) {
            mSpeech!!.stop()
            mSpeech!!.shutdown()
            mSpeech = null
        }
        mNeedStopAfterSpeak = false
    }
}
