package com.bihe0832.android.lib.tts.core.impl

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.tts.core.TTSConfig
import com.bihe0832.android.lib.tts.core.TTSData
import com.bihe0832.android.lib.utils.ConvertUtils
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 * Description: Description
 */
val CONFIG_VALUE_PITCH = 0.4f
val CONFIG_VALUE_SPEECH_RATE = 0.4f

@SuppressLint("StaticFieldLeak")
class TTSImplWithConfig(private var mScene: String) : TTSImplNotifyWithKey() {
    private val TAG = TTSImpl.TAG

    private val mTTSListenerList = CopyOnWriteArrayList<TTSListener>()
    private val mTTSInitListenerList = CopyOnWriteArrayList<TTSInitListener>()
    private var mVolume = getVoiceVolumeFloat()

    fun addTTSListener(listener: TTSListener) {
        mTTSListenerList.add(listener)
    }

    fun removeTTSListener(listener: TTSListener) {
        if (mTTSListenerList.contains(listener)) {
            mTTSListenerList.remove(listener)
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

    override fun initTTSImpl(
        context: Context,
        loc: Locale,
        engine: String?,
        initListener: TTSInitListener?,
        speakListener: TTSSpeakListener?,
    ) {
        super.initTTSImpl(context, loc, engine, initListener, speakListener)
        ZLog.e("TTSImplWithConfig init mTTSInitListenerList and mTTSSpeakListenerList can not used !!!")
    }

    fun initTTSImplWithConfig(
        context: Context,
        loc: Locale,
        engine: String? = "",
    ) {
        initConfig()
        engine?.let {
            setEngine(it)
        }
        super.initTTSImplWithKey(context, loc, engine, object : TTSInitListener {
            override fun onInitError() {
                mTTSInitListenerList.forEach {
                    it.onInitError()
                }
            }

            override fun onLangUnAvailable() {
                mTTSInitListenerList.forEach {
                    it.onLangUnAvailable()
                }
            }

            override fun onLangAvailable() {
                mTTSInitListenerList.forEach {
                    it.onLangAvailable()
                }
            }

        }, object : TTSListener {
            override fun onStart(utteranceId: String, data: String) {
                mTTSListenerList.forEach {
                    it.onStart(utteranceId, data)
                }
            }

            override fun onError(utteranceId: String, data: String) {
                mTTSListenerList.forEach {
                    it.onError(utteranceId, data)
                }
            }

            override fun onComplete(utteranceId: String, data: String) {
                mTTSListenerList.forEach {
                    it.onComplete(utteranceId, data)
                }
            }
        })
    }


    private fun initConfig() {
        setPitch(getConfigPitch())
        setSpeechRate(getConfigSpeechRate())
        setVoiceVolume(getConfigVoiceVolume())
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

    override fun speak(key: String, data: TTSData, type: Int) {
        super.speak(key, data.apply {
            addSpeakParams(
                Bundle().apply {
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, mVolume)
                },
            )
        }, type)
    }


    fun getConfigSpeechRate(): Float {
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

    override fun setSpeechRate(speechRate: Float) {
        var tempmSpeechRate = speechRate
        if (tempmSpeechRate < 0) {
            tempmSpeechRate = 0.1f
        }
        val result = super.setSpeechRate(speechRate * 4)
        val result1 = Config.writeConfig(TTSConfig.CONFIG_KEY_SPEECH_RATE + mScene, tempmSpeechRate)

        ZLog.i(TAG, "setSpeechRate: $speechRate $tempmSpeechRate $result $result1")
    }

    fun getConfigPitch(): Float {
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

    override fun setPitch(pitch: Float) {
        var tempPitch = pitch

        if (tempPitch < 0) {
            tempPitch = 0.1f
        }
        val result = super.setPitch(tempPitch * 2)
        val result1 = Config.writeConfig(TTSConfig.CONFIG_KEY_PITCH + mScene, tempPitch)
        ZLog.i(TAG, "setPitch: $pitch $tempPitch $result $result1")
    }

    override fun setVoiceVolume(paramVolume: Int): Int {
        super.setVoiceVolume(paramVolume).let {
            Config.writeConfig(TTSConfig.CONFIG_KEY_SPEECH_VOICE_VOLUME + mScene, it)
            ZLog.i(TAG, "setVoiceVolume: $paramVolume $it")
            return it
        }
    }

    fun getConfigVoiceVolume(): Int {
        return Config.readConfig(TTSConfig.CONFIG_KEY_SPEECH_VOICE_VOLUME + mScene, 100)
    }

}
