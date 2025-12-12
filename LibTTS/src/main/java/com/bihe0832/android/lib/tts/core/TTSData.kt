package com.bihe0832.android.lib.tts.core

import android.os.Bundle
import android.speech.tts.TextToSpeech

/**
 * TTS语音数据封装类
 *
 * 封装TTS播放所需的文本内容和参数配置，每个实例会自动生成唯一的utteranceId用于回调识别
 *
 * 使用示例：
 * ```kotlin
 * val ttsData = TTSData("你好，世界")
 * ttsData.addSpeakParams(Bundle().apply {
 *     putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.8f)
 * })
 * ```
 *
 * @param speakText 要播放的文本内容
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/3/29.
 */
class TTSData(val speakText: String = "") {

    private val mUtteranceId: Int = TTSConfig.getTTSID()

    private val speakBundle: Bundle = Bundle()

    fun getUtteranceId(): String {
        return mUtteranceId.toString()
    }

    /**
     * 所有的key 取自 TextToSpeech.Engine.KEY_PARAM
     */
    fun addSpeakParams(value: Bundle) {
        speakBundle.putAll(value)
    }

    fun getSpeakBundle(): Bundle {
        return speakBundle.apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, getUtteranceId())
        }
    }

    fun getSpeakMap(): HashMap<String, String> {
        return bundleToMap(speakBundle).apply {
            put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, getUtteranceId())
        }
    }

    private fun bundleToMap(bundle: Bundle?): HashMap<String, String> {
        val map: HashMap<String, String> = HashMap()
        if (bundle != null) {
            for (key in bundle.keySet()) {
                var value = bundle[key]
                if (value is Bundle) {
                    value = bundleToMap(value as Bundle?)
                }
                map[key] = value.toString()
            }
        }
        return map
    }

    override fun toString(): String {
        return "TTSData(speakText='$speakText', mUtteranceId=$mUtteranceId, speakBundle=$speakBundle)"
    }
}
