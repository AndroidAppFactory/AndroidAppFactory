package com.bihe0832.android.lib.device.shake

object ShakeManager : ShakeManagerImpl() {

    fun setSpeed(speed: Int) {
        setSpeed(UPTATE_INTERVAL_TIME, speed * 100L)
    }

    fun setSpeed(duration: Int, speed: Long) {
        setSpeedAndInterval(speed, duration)
    }
}