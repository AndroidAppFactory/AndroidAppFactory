package com.bihe0832.android.lib.tts

import android.annotation.SuppressLint
import android.content.Context
import android.speech.tts.TextToSpeech
import com.bihe0832.android.lib.tts.core.TTSData
import com.bihe0832.android.lib.tts.core.TTSHelper
import java.util.Locale

/**
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 * Description: Description
 */
@SuppressLint("StaticFieldLeak")
object LibTTS {
    private val ttsImpl by lazy {
        TTSHelper()
    }

    fun addTTSSpeakListener(listener: TTSHelper.TTSSpeakListener) {
        ttsImpl.addTTSSpeakListener(listener)
    }

    fun removeTTSSpeakListener(listener: TTSHelper.TTSSpeakListener) {
        ttsImpl.removeTTSSpeakListener(listener)
    }

    fun addTTSInitListener(listener: TTSHelper.TTSInitListener) {
        ttsImpl.addTTSInitListener(listener)
    }

    fun removeTTSInitListener(listener: TTSHelper.TTSInitListener) {
        ttsImpl.removeTTSInitListener(listener)
    }

    fun init(context: Context, scene: String, loc: Locale, listener: TTSHelper.TTSInitListener) {
        ttsImpl.init(context, scene, loc, listener)
    }

    fun init(context: Context, scene: String, loc: Locale, engine: String?, listener: TTSHelper.TTSInitListener) {
        ttsImpl.init(context, scene, loc, engine, listener)
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

    fun getSpeechRate(): Float {
        return ttsImpl.getSpeechRate()
    }

    fun getDefaultSpeechRate(): Float {
        return ttsImpl.getDefaultSpeechRate()
    }

    fun setSpeechRate(speechRate: Float) {
        ttsImpl.setSpeechRate(speechRate)
    }

    fun getPitch(): Float {
        return ttsImpl.getPitch()
    }

    fun getDefaultPitch(): Float {
        return ttsImpl.getDefaultPitch()
    }

    fun setPitch(pitch: Float) {
        return ttsImpl.setPitch(pitch)
    }


    fun getEngines(): List<TextToSpeech.EngineInfo>? {
        return ttsImpl.getEngines()
    }

    fun getDefaultEngine(): String? {
        return ttsImpl.getDefaultEngine()
    }

    fun onDestroy() {
        return ttsImpl.onDestroy()
    }
}
