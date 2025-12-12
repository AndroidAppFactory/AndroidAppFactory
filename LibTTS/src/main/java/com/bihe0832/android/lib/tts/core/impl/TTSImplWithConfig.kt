package com.bihe0832.android.lib.tts.core.impl

import android.annotation.SuppressLint
import android.content.Context
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.tts.core.TTSConfig
import com.bihe0832.android.lib.utils.ConvertUtils
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * TTS默认音调配置值
 */
val CONFIG_VALUE_PITCH = 0.4f

/**
 * TTS默认语速配置值
 */
val CONFIG_VALUE_SPEECH_RATE = 0.4f

/**
 * 带配置管理的TTS实现类
 *
 * 在TTSImplNotifyWithKey基础上增加了配置持久化功能，支持：
 * - 语速、音调、音量的配置保存和读取
 * - TTS引擎的配置保存
 * - 多场景配置隔离（通过mScene参数）
 * - 多监听器管理
 *
 * 配置会自动持久化到本地，下次启动时自动恢复。
 *
 * @param mScene 场景标识，用于区分不同场景的配置
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 */
@SuppressLint("StaticFieldLeak")
class TTSImplWithConfig(private var mScene: String) : TTSImplNotifyWithKey() {
    private val TAG = TTSImpl.TAG

    private val mTTSListenerList = CopyOnWriteArrayList<TTSListener>()
    private val mTTSInitListenerList = CopyOnWriteArrayList<TTSInitListener>()

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

    fun initTTSImplWithConfig(
        context: Context,
        loc: Locale,
        engine: String? = "",
    ) {
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

    fun getConfigSpeechRate(): Float {
        val speechRate =
            Config.readConfig(TTSConfig.CONFIG_KEY_SPEECH_RATE + mScene, getDefaultSpeechRate())
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
        super.setSpeechRate(speechRate)
        val result = Config.writeConfig(TTSConfig.CONFIG_KEY_SPEECH_RATE + mScene, speechRate)
        ZLog.i(TAG, "setSpeechRate: $speechRate $speechRate $result")
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
        super.setPitch(pitch)
        val result = Config.writeConfig(TTSConfig.CONFIG_KEY_PITCH + mScene, pitch)
        ZLog.i(TAG, "setPitch: $pitch $result")
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
