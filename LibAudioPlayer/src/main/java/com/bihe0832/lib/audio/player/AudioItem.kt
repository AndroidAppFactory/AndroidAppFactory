package com.bihe0832.lib.audio.player

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/10/28.
 * Description: Description
 *
 */
class AudioItem(val soundid: Int, val duration: Long) {

    var playListener: AudioPlayListener? = null
    var priority = 0

    override fun toString(): String {
        return "AudioItem(soundid=$soundid, duration=$duration)"
    }


}
