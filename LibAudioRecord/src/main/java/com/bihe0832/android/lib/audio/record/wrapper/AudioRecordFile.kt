package com.bihe0832.android.lib.audio.record.wrapper

import android.app.Activity
import android.content.Context
import com.bihe0832.android.lib.audio.AudioRecordConfig
import com.bihe0832.android.lib.audio.record.AudioRecordManager
import com.bihe0832.android.lib.audio.wav.WavHeader
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/7/18.
 * Description:
 *
 */
class AudioRecordFile(
    private val scene: String,
    private val file: File,
) {

    private var config: AudioRecordConfig? = null
    private var outputStream: OutputStream? = null

    private var hasStart = false

    fun stopRecord(context: Context) {
        ZLog.d("${AudioRecordManager.TAG} AudioRecordFile stopRecord:$scene $file")
        AudioRecordManager.pauseRecord(scene)
        if (config != null) {
            if (outputStream != null) {
                try {
                    outputStream!!.flush()
                    outputStream!!.close()
                    outputStream = null
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            ZLog.d("${AudioRecordManager.TAG} AudioRecordFile stopRecord:$scene $file ${file.length()}")
            FileUtils.writeDataToFile(file.absolutePath, 0, WavHeader(config, file.length()).toBytes(), false)
            ZLog.d("${AudioRecordManager.TAG} AudioRecordFile stopRecord:$scene $file ${file.length()}")
        }
        AudioRecordManager.stopRecord(context, scene)
        hasStart = false
    }

    fun startRecord(): Boolean {
        return startRecord(null, "")
    }

    fun startRecord(activity: Activity?, content: String): Boolean {
        ZLog.d("${AudioRecordManager.TAG} AudioRecordFile startRecord:$scene ${file.absoluteFile}")
        if (hasStart) {
            return true
        }
        try {
            val parentFile = file.parentFile
            if (parentFile == null || !FileUtils.checkAndCreateFolder(parentFile.absolutePath)) {
                return false
            }
            hasStart = true
            if (outputStream == null) {
                outputStream = FileOutputStream(file)
            }
            return AudioRecordManager.startRecord(activity, scene, content) { audioRecordConfig, audioChunk, ret ->
                if (audioChunk != null && ret > 0) {
                    config = audioRecordConfig
                    outputStream!!.write(audioChunk.toBytes()) // 将数据写入文件
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }
}