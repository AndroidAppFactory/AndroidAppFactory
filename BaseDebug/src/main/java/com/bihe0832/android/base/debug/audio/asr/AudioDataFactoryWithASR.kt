package com.bihe0832.android.base.debug.audio.asr

import android.app.Activity
import com.bihe0832.android.base.debug.audio.asr.ASRModelDownloadManager.checkAndDoAction
import com.bihe0832.android.common.debug.audio.DebugAudioDataFactory
import com.bihe0832.android.common.debug.audio.process.AudioDataFactoryCallback
import com.bihe0832.android.lib.audio.AudioRecordConfig
import com.bihe0832.android.lib.audio.record.AudioRecordManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.speech.DEFAULT_ENDPOINT_MODEL_DIR
import com.bihe0832.android.lib.speech.getDefaultOnlineRecognizerConfig
import com.bihe0832.android.lib.speech.recognition.ASROfflineManager
import com.bihe0832.android.lib.speech.recognition.ASROnlineManager
import com.k2fsa.sherpa.onnx.OnlineStream

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2025/1/10.
 * Description: Description
 *
 */
class AudioDataFactoryWithASR(processCallback: AudioDataFactoryCallback) :
    DebugAudioDataFactory(processCallback) {

    val mASROnlineManager by lazy { ASROnlineManager() }
    var mOnlineStream: OnlineStream? = null
    val mASROnlineManager2 by lazy { ASROnlineManager() }
    var mOnlineStream2: OnlineStream? = null

    val mParaformerASROfflineManager by lazy { ASROfflineManager() }
    val mSmallParaformerASROfflineManager by lazy { ASROfflineManager() }
//    private val mWhisperBaseASROfflineManager by lazy { ASROfflineManager() }

    //    private val mWhisperTinyASROfflineManager by lazy { ASROfflineManager() }
//    private val mZipformerZH14ASROnlineManager by lazy { ASROnlineManager() }
//    private val mZipformerBilingualZHENASROnlineManager by lazy { ASROnlineManager() }
//    private val mKeywordSpotterManager by lazy { KeywordSpotterManager() }

    fun isReady(): Boolean {
        ZLog.d(AudioRecordManager.TAG, "----------")
        ZLog.d(
            AudioRecordManager.TAG, "mASROfflineManager: ${mParaformerASROfflineManager.isReady()}"
        )
        ZLog.d(
            AudioRecordManager.TAG,
            "mASROfflineManager: ${mSmallParaformerASROfflineManager.isReady()}"
        )
        ZLog.d(
            AudioRecordManager.TAG, "mASROnlineManager: ${mASROnlineManager.isReady()}"
        )
        ZLog.d(
            AudioRecordManager.TAG, "mASROnlineManager2: ${mASROnlineManager2.isReady()}"
        )
        ZLog.d(AudioRecordManager.TAG, "----------")
        return mParaformerASROfflineManager.isReady() && mSmallParaformerASROfflineManager.isReady() && mASROnlineManager.isReady() && mASROnlineManager2.isReady()
    }

    fun init(activity: Activity) {
        checkAndDoAction(
            activity, modelDir_ASROfflineRecognizerConfig_paraformer_small,
            url_ASROfflineRecognizerConfig_paraformer_small,
            md5_ASROfflineRecognizerConfig_paraformer_small,
        ) {
            mSmallParaformerASROfflineManager.initRecognizer(
                getASROfflineRecognizerConfig_paraformer_small()
            )
            checkAndDoAction(
                activity, modelDir_ASROfflineRecognizerConfig_paraformer,
                url_ASROfflineRecognizerConfig_paraformer,
                md5_ASROfflineRecognizerConfig_paraformer,
            ) {
                mParaformerASROfflineManager.initRecognizer(
                    getASROfflineRecognizerConfig_paraformer()
                )

//                checkAndDoAction(
//                    activity, modelDir_ASROnlineRecognizerConfig,
//                    url_ASROnlineRecognizerConfig,
//                    md5_ASROnlineRecognizerConfig,
//                ) {
//                    mASROnlineManager2.initRecognizer(getASROnlineRecognizerConfig())
//                    if (mASROnlineManager2.isReady()) {
//                        mOnlineStream2 = mASROnlineManager2.start()
//                    }
//                }
            }
        }

        mASROnlineManager.initRecognizer(
            activity, getDefaultOnlineRecognizerConfig(
                AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, DEFAULT_ENDPOINT_MODEL_DIR
            )
        )

        if (mASROnlineManager.isReady()) {
            mOnlineStream = mASROnlineManager.start()
        }
    }

    fun reset() {
        mSmallParaformerASROfflineManager.destroy()
        mParaformerASROfflineManager.destroy()
        mASROnlineManager.destroy()
        mASROnlineManager2.destroy()
    }
}