package com.bihe0832.android.common.compose.debug.module.audio

import com.bihe0832.android.common.compose.debug.module.audio.process.AudioDataFactoryCallback
import com.bihe0832.android.common.compose.debug.module.audio.process.AudioDataFactoryInterface
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.lib.audio.wav.WaveFileReader
import com.bihe0832.android.lib.file.FileUtils.getFileLength
import com.bihe0832.android.lib.thread.ThreadManager
import java.io.File

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/1/10.
 * Description: Description
 *
 */
open class DebugAudioDataFactory(private val processCallback: AudioDataFactoryCallback) :
    AudioDataFactoryInterface {

    private var forceStop = false

    fun commonProcessAudioData(logFile: String, index: Int, folder: String, filePath: String) {
        LoggerFile.logH5(
            logFile, "",
            "<font color='#3AC8EF'><B>音频文件${index + 1}：</B></font>" +
                    filePath.replace(folder, "")
        )
        LoggerFile.logH5(
            logFile, "", LoggerFile.getAudioH5LogData(filePath, "audio/wav")
        )
    }

    override fun processAudioData(logFile: String, index: Int, folder: String, filePath: String) {
        commonProcessAudioData(logFile, index, folder, filePath)
        val file = File(filePath)
        val waveFileReader = WaveFileReader(filePath)
        if (waveFileReader.isSuccess) {
            LoggerFile.logH5(
                logFile,
                "",
                "文件大小：" + getFileLength(file.length()) + "，" + waveFileReader.toShowString()
            )
        } else {
            LoggerFile.logH5(
                logFile, "", "音频文件异常，解析失败，请检查音频格式"
            )
        }
        Thread.sleep(300L)
    }

    override fun processAudioList(logFile: String, folder: String, fileList: List<File>) {
        processCallback.onStart()
        forceStop = false
        Thread.sleep(1000L)
        ThreadManager.getInstance().start {
            val start = System.currentTimeMillis()
            fileList.let {
                val num = it.size
                var isSuccess = true
                for (index in it.indices) {
                    if (forceStop) {
                        isSuccess = false
                        break
                    } else {
                        ThreadManager.getInstance().runOnUIThread {
                            processCallback.onProcess(index + 1, num)
                        }
                        processAudioData(logFile, index, folder, it[index].absolutePath)
                    }
                }
                forceStop = false
                if (System.currentTimeMillis() - start < 4000) {
                    Thread.sleep(System.currentTimeMillis() - start)
                }
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