package com.bihe0832.android.app.shortcut

import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.lib.app.icon.APPIconManager
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.log.ZLog

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/8/20.
 * Description: Description
 *
 */
object AAFShortcutManager {

    val defaultShortcut = "com.bihe0832.android.test.DefaultAlias"

    val allAliasList = mutableListOf(
        defaultShortcut,
        "com.bihe0832.android.test.FestivalAlias",
    )

    fun init(ctx: Context) {
        val configIcon = Config.readConfig("shortCut", defaultShortcut)
        ZLog.d(APPIconManager.TAG, "changeAppIcon init:$configIcon")
        if (!TextUtils.isEmpty(configIcon)) {
            changeIcon(ctx, configIcon)
        }
    }

    fun changeIcon(ctx: Context, configIcon: String) {
        APPIconManager.changeAppIcon(ctx, configIcon, allAliasList, 2000L)
    }
}