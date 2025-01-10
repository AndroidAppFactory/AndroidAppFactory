package com.bihe0832.android.common.debug.audio.process

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2025/1/10.
 * Description: Description
 *
 */
interface AudioDataFactoryCallback {
    fun onStart()
    fun onProcess(current: Int, num: Int)
    fun onCancel()
    fun onComplete()
}