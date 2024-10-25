package com.bihe0832.android.lib.tts

import android.annotation.SuppressLint
import android.content.Context
import android.speech.tts.TextToSpeech
import com.bihe0832.android.lib.tts.core.TTSData
import com.bihe0832.android.lib.tts.core.impl.TTSImpl
import com.bihe0832.android.lib.tts.core.impl.TTSImplNotifyWithKey
import com.bihe0832.android.lib.tts.core.impl.TTSImplWithConfig
import java.util.Locale

/**
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 * Description: Description
 */
@SuppressLint("StaticFieldLeak")
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

    fun init(context: Context, loc: Locale, engine: String, listener: TTSImpl.TTSInitListener?) {
        listener?.let {
            ttsImpl.addTTSInitListener(listener)
        }
        ttsImpl.initTTSImplWithConfig(context, loc, engine)
    }

    fun isSpeak(): Boolean {
        return ttsImpl.isSpeak()
    }

    fun hasMoreSpeak(): Boolean {
        return ttsImpl.hasMoreSpeak()
    }

    @Synchronized
    fun startSpeak() {
        ttsImpl.hasMoreSpeak()
    }

    fun stopSpeak() {
        ttsImpl.stopSpeak()
    }

    fun forceStop() {
        ttsImpl.forceStop()
    }

    fun speak(tempStr: TTSData, type: Int) {
        ttsImpl.speak(tempStr, type)
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
