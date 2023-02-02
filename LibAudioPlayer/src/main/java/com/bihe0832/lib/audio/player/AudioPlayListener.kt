package com.bihe0832.lib.audio.player

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/10/31.
 * Description: Description
 *
 */
interface AudioPlayListener {

    fun onLoad()
    fun onLoadComplete(soundid: Int, status: Int)
    fun onPlayStart()
    fun onPlayFinished(error: Int, msg: String)
}