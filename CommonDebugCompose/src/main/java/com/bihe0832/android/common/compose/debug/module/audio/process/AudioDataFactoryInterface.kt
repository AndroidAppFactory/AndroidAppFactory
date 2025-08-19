package com.bihe0832.android.common.compose.debug.module.audio.process

import java.io.File

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/1/10.
 * Description: Description
 *
 */
interface AudioDataFactoryInterface {

    fun processAudioData(logFile: String, index: Int, folder: String, filePath: String)

    fun processAudioList(logFile: String, folder: String, fileList: List<File>)

    fun forceStop()

}