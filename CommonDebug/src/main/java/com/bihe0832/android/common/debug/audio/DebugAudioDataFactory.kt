package com.bihe0832.android.common.debug.audio

import com.bihe0832.android.common.debug.audio.process.AudioDataFactoryCallback
import com.bihe0832.android.common.debug.audio.process.AudioDataFactoryInterface
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.lib.audio.wav.WaveFileReader
import com.bihe0832.android.lib.file.FileUtils.getFileLength
import com.bihe0832.android.lib.thread.ThreadManager
import java.io.File

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2025/1/10.
 * Description: Description
 *
 */
open class DebugAudioDataFactory(private val processCallback: AudioDataFactoryCallback) :
    AudioDataFactoryInterface {

    private var forceStop = false

    override fun processAudioData(logFile: String, filePath: String) {
        LoggerFile.logH5(
            logFile, "", LoggerFile.getAudioH5LogData(filePath, "audio/wav")
        )
        val file = File(filePath)
        val waveFileReader = WaveFileReader(filePath)
        LoggerFile.logH5(
            logFile, "", "文件大小：" + if (waveFileReader.isSuccess) {
                val fileLength = "文件大小：" + getFileLength(file.length())
                fileLength + "，" + waveFileReader.toShowString()
            } else {
                "音频文件异常，解析失败，请检查音频格式"
            }
        )
        Thread.sleep(3000L)
    }

    override fun processAudioList(logFile: String, fileList: List<File>) {
        processCallback.onStart()
        forceStop = false
        ThreadManager.getInstance().start {
            fileList.let {
                val num = it.size
                var isSuccess = false
                for (index in it.indices) {
                    if (forceStop) {
                        break
                    } else {
                        ThreadManager.getInstance().runOnUIThread {
                            processCallback.onProcess(index + 1, num)
                        }
                        processAudioData(logFile, it[index].absolutePath)
                    }
                    isSuccess = true
                }
                forceStop = false
                ThreadManager.getInstance().runOnUIThread {
                    if (isSuccess) {
                        processCallback.onComplete()
                    } else {
                        processCallback.onCancel()
                    }
                }
            }
        }
    }

    override fun forceStop() {
        forceStop = true
    }

}