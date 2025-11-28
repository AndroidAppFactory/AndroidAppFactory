package com.bihe0832.android.base.compose.debug.audio


import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bihe0832.android.base.compose.debug.R
import com.bihe0832.android.common.compose.debug.item.DebugComposeActivityItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.module.audio.DebugAudioListActivity
import com.bihe0832.android.common.compose.debug.module.audio.DebugAudioListWithProcessActivity
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.video.FFmpegTools
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.audio.AudioUtils
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.lib.audio.player.AudioPlayListener
import com.bihe0832.lib.audio.player.block.AudioPLayerManager

private const val LOG_TAG = "DebugComposeAudioView"

@Composable
fun DebugAudioComposeView() {
    val context = LocalContext.current

    DebugContent {
        val blockAudioPlayerManager = AudioPLayerManager()
        DebugComposeActivityItem("本地 WAV 查看", DebugAudioListActivity::class.java)
        DebugComposeActivityItem("本地 WAV 查看2", DebugAudioListWithProcessActivity::class.java)
        DebugItem("播放远程音频") {
            DownloadFile.download(it,
                "https://cdn.bihe0832.com/audio/03.wav",
                false,
                object : SimpleDownloadListener() {
                    override fun onComplete(filePath: String, item: DownloadItem): String {
                        blockAudioPlayerManager.play(filePath)
                        blockAudioPlayerManager.play(it, R.raw.one)
                        blockAudioPlayerManager.play(it, R.raw.four)
                        return filePath
                    }

                    override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {

                    }

                    override fun onProgress(item: DownloadItem) {

                    }

                })

        }

        DebugItem("远程音频裁剪") {
            DownloadFile.download(it,
                "https://cdn.bihe0832.com/audio/03.wav",
                "",
                false,
                "FAEE24E98CD256E204FAF5168FA39D24",
                false,
                object : SimpleDownloadListener() {
                    override fun onComplete(filePath: String, item: DownloadItem): String {
                        ZLog.d(FileUtils.getFileMD5(filePath))
                        FFmpegTools.splitAudioWithDuration(filePath,
                            0.7f,
                            2f,
                            object : AAFDataCallback<String>() {
                                override fun onSuccess(result: String?) {
                                    blockAudioPlayerManager.play(filePath)
                                    blockAudioPlayerManager.play(result ?: "")
                                }
                            })
                        return filePath
                    }

                    override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {

                    }

                    override fun onProgress(item: DownloadItem) {

                    }

                })

        }
        DebugItem("播放本地音频") {
            blockAudioPlayerManager.play(it, R.raw.one)
            blockAudioPlayerManager.play(it, R.raw.two, 0.1f, 1.0f, 1.0f, 1, null)
            blockAudioPlayerManager.play(it, R.raw.three)
            blockAudioPlayerManager.play(it, R.raw.icon)
            blockAudioPlayerManager.play(it, R.raw.test)
            blockAudioPlayerManager.play(it, R.raw.three)
        }
        DebugItem("PCM转WAV") {
            DownloadFile.download(
                context,
                "https://cdn.bihe0832.com/audio/02.pcm",
                AAFFileWrapper.getTempFolder(),
                false,
                "5135554C0D30763067D2ADA22D45BB3B",
                object : SimpleDownloadListener() {
                    override fun onProgress(item: DownloadItem) {

                    }

                    override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {

                    }

                    override fun onComplete(filePath: String, item: DownloadItem): String {
                        val wavFile = filePath.replace(".pcm", "1.wav")
                        AudioUtils.convertPCMToWAV(48000, 1, 16, filePath, wavFile)
                        blockAudioPlayerManager.play(wavFile)
                        return filePath
                    }
                })
        }

        DebugItem("本地音频极限测试") {
            ThreadManager.getInstance().start {
                ZLog.d("本地音频极限测试开始")
                for (i in 0..257) {
                    blockAudioPlayerManager.play(it, R.raw.one)
                }
                ZLog.d("本地音频极限测试结束")
            }
        }
        DebugItem("播放扫码音频") {
            blockAudioPlayerManager.play(it, R.raw.beep)
        }


        DebugItem("立即结束并清空队列") {
            blockAudioPlayerManager.stopAll(true)
        }

        DebugItem("清空队列") {
            blockAudioPlayerManager.stopAll(false)
        }

        DebugItem("清空队列并播放") {
            blockAudioPlayerManager.stopAll(false)
            blockAudioPlayerManager.play(it, R.raw.three)
        }

        DebugItem("强制中断当前并播放下一个") {
            blockAudioPlayerManager.play(
                it,
                R.raw.three,
                0.1f,
                1.0f,
                1.0f,
                1,
                object : AudioPlayListener {
                    override fun onLoad() {
                        ZLog.d(LOG_TAG, "onLoad")
                    }

                    override fun onLoadComplete(soundid: Int, status: Int) {
                        ZLog.d(LOG_TAG, "onLoadComplete")
                    }

                    override fun onPlayStart() {
                        ZLog.d(LOG_TAG, "onPlayStart")
                    }

                    override fun onPlayFinished(error: Int, msg: String) {
                        ZLog.d(LOG_TAG, "onPlayFinished")
                    }

                })
            ThreadManager.getInstance().start({
                blockAudioPlayerManager.finishCurrent()
                blockAudioPlayerManager.play(it,
                    R.raw.three,
                    0.1f,
                    1.0f,
                    1.0f,
                    1,
                    object : AudioPlayListener {
                        override fun onLoad() {
                            ZLog.d(LOG_TAG, "onLoad")
                        }

                        override fun onLoadComplete(soundid: Int, status: Int) {
                            ZLog.d(LOG_TAG, "onLoadComplete")
                        }

                        override fun onPlayStart() {
                            ZLog.d(LOG_TAG, "onPlayStart")
                        }

                        override fun onPlayFinished(error: Int, msg: String) {
                            ZLog.d(LOG_TAG, "onPlayFinished")
                        }

                    })
            }, 800L)

        }
    }
}