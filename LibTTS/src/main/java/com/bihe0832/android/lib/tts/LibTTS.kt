package com.bihe0832.android.lib.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import com.bihe0832.android.lib.tts.core.TTSData
import com.bihe0832.android.lib.tts.core.impl.TTSImpl
import com.bihe0832.android.lib.tts.core.impl.TTSImplNotifyWithKey
import com.bihe0832.android.lib.tts.core.impl.TTSImplWithConfig
import java.util.Locale

/**
 * TTS语音合成库
 *
 * 提供基于Android TextToSpeech的语音合成功能，支持：
 * - 多种播放模式（顺序、插队、清空、立即播放）
 * - 语音参数配置（语速、音调、音量）
 * - 配置持久化
 * - TTS引擎选择
 * - 语音保存到文件
 *
 * 使用示例：
 * ```kotlin
 * // 初始化
 * LibTTS.init(context, Locale.SIMPLIFIED_CHINESE, "", null)
 *
 * // 播放语音
 * val ttsData = TTSData("你好，世界")
 * LibTTS.speak(ttsData, TTSConfig.SPEEAK_TYPE_SEQUENCE)
 * ```
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 */
object LibTTS {
    private val ttsImpl by lazy {
        TTSImplWithConfig("")
    }

    fun addTTSSpeakListener(listener: TTSImplNotifyWithKey.TTSListener) {
        ttsImpl.addTTSListener(listener)
    }

    fun removeTTSSpeakListener(listener: TTSImplNotifyWithKey.TTSListener) {
        ttsImpl.removeTTSListener(listener)
    }

    fun addTTSInitListener(listener: TTSImpl.TTSInitListener) {
        ttsImpl.addTTSInitListener(listener)
    }

    fun removeTTSInitListener(listener: TTSImpl.TTSInitListener) {
        ttsImpl.removeTTSInitListener(listener)
    }

    /**
     * 初始化TTS引擎
     *
     * @param context 上下文，内部会自动使用ApplicationContext避免内存泄漏
     * @param loc 语言环境，如 Locale.SIMPLIFIED_CHINESE
     * @param engine TTS引擎包名，传空字符串使用系统默认引擎
     * @param listener 初始化监听器，可为null
     */
    fun init(context: Context, loc: Locale, engine: String, listener: TTSImpl.TTSInitListener?) {
        listener?.let {
            ttsImpl.addTTSInitListener(listener)
        }
        // 使用 ApplicationContext 避免内存泄漏
        ttsImpl.initTTSImplWithConfig(context.applicationContext, loc, engine)
    }

    /**
     * 判断当前是否正在播放语音
     *
     * @return true表示正在播放，false表示未播放
     */
    fun isSpeaking(): Boolean {
        return ttsImpl.isSpeaking()
    }

    /**
     * 判断是否还有待播放的语音
     *
     * @return true表示有待播放语音，false表示队列为空
     */
    fun hasMoreSpeak(): Boolean {
        return ttsImpl.hasMoreSpeak()
    }

    /**
     * 开始播放队列中的下一条语音
     */
    @Synchronized
    fun startSpeak() {
        ttsImpl.startSpeak()
    }

    fun stopSpeak() {
        ttsImpl.stopSpeak()
    }

    fun forceStop() {
        ttsImpl.forceStop()
    }

    /**
     * 播放语音
     *
     * @param tempStr TTS数据
     * @param type 播放类型，参见 TTSConfig.SPEEAK_TYPE_*
     */
    fun speak(tempStr: TTSData, type: Int) {
        ttsImpl.speak(tempStr, type)
    }

    /**
     * 播放语音（带回调Key）
     *
     * @param key 回调时透传的Key
     * @param tempStr TTS数据
     * @param type 播放类型，参见 TTSConfig.SPEEAK_TYPE_*
     */
    fun speak(key: String, tempStr: TTSData, type: Int) {
        ttsImpl.speak(key, tempStr, type)
    }

    fun isTTSServiceOK(tts: TextToSpeech?): Boolean {
        return ttsImpl.isTTSServiceOK(tts)
    }


    fun save(ttsData: TTSData, finalFileName: String): Int? {
        return ttsImpl.save(ttsData, finalFileName)
    }

    fun getConfigSpeechRate(): Float {
        return ttsImpl.getConfigSpeechRate()
    }

    fun getDefaultSpeechRate(): Float {
        return ttsImpl.getDefaultSpeechRate()
    }

    fun setSpeechRate(speechRate: Float) {
        ttsImpl.setSpeechRate(speechRate)
    }

    fun getConfigPitch(): Float {
        return ttsImpl.getConfigPitch()
    }

    fun getDefaultPitch(): Float {
        return ttsImpl.getDefaultPitch()
    }

    fun getConfigVoiceVolume(): Int {
        return ttsImpl.getConfigVoiceVolume()
    }

    fun setVoiceVolume(paramVolume: Int) {
        ttsImpl.setVoiceVolume(paramVolume)
    }

    fun setPitch(pitch: Float) {
        ttsImpl.setPitch(pitch)
    }


    fun getEngines(): List<TextToSpeech.EngineInfo>? {
        return ttsImpl.getEngines()
    }

    fun getDefaultEngine(): String? {
        return ttsImpl.getDefaultEngine()
    }

    fun onDestroy() {
        ttsImpl.onDestroy()
    }
}
