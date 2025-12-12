package com.bihe0832.android.lib.tts.core.impl

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.TextUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.tts.core.TTSConfig
import com.bihe0832.android.lib.tts.core.TTSData
import com.bihe0832.android.lib.utils.os.BuildUtils
import java.io.File
import java.lang.ref.WeakReference
import java.lang.reflect.Field
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * TTS核心实现类
 *
 * 封装Android TextToSpeech API，提供：
 * - TTS引擎初始化和生命周期管理
 * - 语音播放队列管理
 * - 语音参数配置（语速、音调、音量）
 * - 语音保存到文件
 * - 播放状态回调
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 */
@SuppressLint("StaticFieldLeak")
open class TTSImpl {

    companion object {
        const val TAG = "TTS"
    }

    // 使用AAF框架的ThreadManager提供的单线程Handler，保证TTS操作的顺序性
    // 使用LOOPER_TYPE_NORMAL优先级，适合处理业务逻辑
    private val ttsHandler by lazy {
        Handler(ThreadManager.getInstance().getLooper(ThreadManager.LOOPER_TYPE_NORMAL))
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
                    // TTS初始化回调在主线程执行，需要将耗时操作移到子线程避免ANR
                    if (status == TextToSpeech.SUCCESS) {
                        ZLog.d(TAG, "onInit: TTS引擎初始化成功")
                        // 使用AAF框架的单线程Handler执行后续操作，既避免阻塞主线程，又保证操作顺序性
                        ttsHandler.post {
                            try {
                                setLanguage(mLocale!!)
                                ttsData?.let {
                                    if (speak(ttsData) == TextToSpeech.ERROR) {
                                        mTTSResultListener.onUtteranceFailed(
                                            ttsData.getUtteranceId(),
                                            ttsData.speakText
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                ZLog.e(TAG, "TTS初始化后处理异常: ${e.message}")
                                e.printStackTrace()
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

    /**
     * 判断当前是否正在播放语音
     *
     * @return true表示正在播放，false表示未播放
     */
    fun isSpeaking(): Boolean {
        return isSpeakIng
    }

    fun hasMoreSpeak(): Boolean {
        return mMsgList.isNotEmpty()
    }

    /**
     * 开始播放队列中的下一条语音
     * 使用线程安全的方式从队列中取出数据
     */
    @Synchronized
    fun startSpeak() {
        if (mMsgList.isNotEmpty()) {
            val data = mMsgList.removeFirstOrNull() ?: return
            speakWithTry(data)
        }
    }

    /**
     * 停止语音播放
     * 如果当前正在播放，则等待播放完成后停止
     */
    fun stopSpeak() {
        ZLog.d(TAG, "stopSpeak")
        if (isSpeaking()) {
            mNeedStopAfterSpeak = true
        } else {
            onDestroy()
        }
    }

    fun forceStop() {
        onDestroy()
    }

    /**
     * 播放语音
     *
     * @param tempStr TTS数据
     * @param type 播放类型：
     *   - SPEEAK_TYPE_SEQUENCE: 顺序播放，添加到队列末尾
     *   - SPEEAK_TYPE_NEXT: 插队播放，添加到队列头部
     *   - SPEEAK_TYPE_FLUSH: 立即播放，打断当前播放
     *   - SPEEAK_TYPE_CLEAR: 清空队列后播放
     */
    open fun speak(tempStr: TTSData, type: Int) {
        ZLog.d(TAG, "speak: ${mSpeech.hashCode()} ${isSpeaking()} ${mSpeech?.isSpeaking} $tempStr")
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
                if (!isSpeaking()) {
                    startSpeak()
                }
            }

            TTSConfig.SPEEAK_TYPE_NEXT -> {
                mMsgList.add(0, tempStr)
                if (!isSpeaking()) {
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

    /**
     * 尝试播放语音，如果失败则重新初始化TTS引擎
     */
    private fun speakWithTry(ttsData: TTSData) {
        ZLog.d(TAG, "speakWithTry ttsData: $ttsData ")
        if (!isTTSServiceOK(mSpeech)) {
            initTTSAndSpeak(ttsData)
        } else {
            val result = try {
                speak(ttsData) ?: TextToSpeech.ERROR
            } catch (e: Exception) {
                ZLog.e(TAG, "speakWithTry exception: ${e.message}")
                e.printStackTrace()
                TextToSpeech.ERROR
            }
            ZLog.d(TAG, "speakWithTry result: $result ")
            if (result == TextToSpeech.ERROR) {
                initTTSAndSpeak(ttsData)
            }
        }
    }

    /**
     * 检查TTS服务是否正常
     * 使用反射检查mServiceConnection字段是否为null
     *
     * @param tts TextToSpeech实例
     * @return true表示服务正常，false表示服务异常
     */
    fun isTTSServiceOK(tts: TextToSpeech?): Boolean {
        if (tts == null) {
            return false
        }
        
        return try {
            val fields: Array<Field> = tts.javaClass.declaredFields
            for (field in fields) {
                field.isAccessible = true
                if (field.name == "mServiceConnection" && 
                    field.type.name == "android.speech.tts.TextToSpeech\$Connection") {
                    if (field.get(tts) == null) {
                        ZLog.w(TAG, "TTS service connection is null")
                        return false
                    }
                }
            }
            true
        } catch (e: Exception) {
            ZLog.e(TAG, "Failed to check TTS service status: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * 执行实际的语音播放
     */
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
        isSpeakIng = (result == TextToSpeech.SUCCESS)
        ZLog.d(TAG, "real speak ttsData result $result ,ttsData: $ttsData ")
        return result
    }

    /**
     * 将语音保存到文件
     *
     * @param ttsData TTS数据
     * @param finalFileName 目标文件路径
     * @return TextToSpeech.SUCCESS 或 TextToSpeech.ERROR
     */
    fun save(ttsData: TTSData, finalFileName: String): Int {
        ZLog.d(TAG, "save ttsData: $ttsData")
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

    /**
     * 释放资源，停止TTS引擎
     */
    fun onDestroy() {
        ZLog.d(TAG, "onDestroy")
        
        // 清理Handler中的所有待执行任务
        ttsHandler.removeCallbacksAndMessages(null)
        
        mSpeech?.apply {
            stop()
            shutdown()
        }
        mSpeech = null
        mMsgList.clear()
        mNeedStopAfterSpeak = false
        isSpeakIng = false
    }
}
