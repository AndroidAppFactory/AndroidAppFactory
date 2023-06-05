package com.bihe0832.android.common.accessibility.view

import com.bihe0832.android.common.accessibility.action.AAFAccessibilityManager
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.log.ZLog

/**
 * 按键位置记录管理
 */
object PositionConfig {

    private val CONFIG_KEY_PRE = "com.bihe0832.android.accessibility.position"

    fun getPositionList(key: String): List<String> {
        return readPosition(key)?.split(",") ?: emptyList()
    }

    fun readPosition(key: String): String? {
        return Config.readConfig(CONFIG_KEY_PRE + key, "")
    }

    fun writePosition(key: String, left: Int, top: Int, centerX: Int, centerY: Int) {
        val saveLocation = "$left,$top,$centerX,$centerY"
        ZLog.d(AAFAccessibilityManager.TAG, "[${key}] save position (left, top, centerX, centerY):$saveLocation")
        Config.writeConfig(CONFIG_KEY_PRE + key, saveLocation)
    }
}