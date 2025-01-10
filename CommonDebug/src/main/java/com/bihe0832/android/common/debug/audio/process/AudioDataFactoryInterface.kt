package com.bihe0832.android.common.debug.audio.process

import java.io.File

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2025/1/10.
 * Description: Description
 *
 */
interface AudioDataFactoryInterface {

    fun processAudioData(logFile: String, filePath: String)

    fun processAudioList(logFile: String, fileList: List<File>)

    fun forceStop()

}