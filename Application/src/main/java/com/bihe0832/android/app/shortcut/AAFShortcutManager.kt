package com.bihe0832.android.app.shortcut

import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.lib.app.icon.APPIconManager
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.log.ZLog

/**
 * AAF 快捷方式管理器
 *
 * 管理应用图标的动态切换功能，支持：
 * - 默认图标
 * - 节日主题图标
 * - 其他自定义图标
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/8/20.
 */
object AAFShortcutManager {

    /** 默认快捷方式别名 */
    val defaultShortcut = "com.bihe0832.android.test.DefaultAlias"

    /** 所有可用的图标别名列表 */
    val allAliasList = mutableListOf(
        defaultShortcut,
        "com.bihe0832.android.test.FestivalAlias",
    )

    /**
     * 初始化快捷方式管理器
     *
     * 从配置中读取当前图标设置并应用
     *
     * @param ctx 上下文
     */
    fun init(ctx: Context) {
        val configIcon = Config.readConfig("shortCut", defaultShortcut)
        ZLog.d(APPIconManager.TAG, "changeAppIcon init:$configIcon")
        if (!TextUtils.isEmpty(configIcon)) {
            changeIcon(ctx, configIcon)
        }
    }

    /**
     * 切换应用图标
     *
     * @param ctx 上下文
     * @param configIcon 目标图标别名
     */
    fun changeIcon(ctx: Context, configIcon: String) {
        APPIconManager.changeAppIcon(ctx, configIcon, allAliasList, 2000L)
    }
}