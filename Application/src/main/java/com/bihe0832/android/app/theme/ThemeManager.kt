/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/5/30 下午5:04
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/30 下午5:02
 *
 */

package com.bihe0832.android.app.theme

import android.content.Context
import com.bihe0832.android.app.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.RadioDialog
import com.bihe0832.android.lib.ui.dialog.impl.DialogUtils
import com.bihe0832.android.lib.ui.dialog.impl.SimpleDialogListener

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/5/24.
 * Description: Description
 */


object ThemeManager {

    const val KEY_THEME = "pref_current_theme"
    const val VALUE_THEME_DEFAULT = 2

    private var currentThemeIndex = getTheme()

    fun getTheme(): Int {
        return Config.readConfig(KEY_THEME, VALUE_THEME_DEFAULT)
    }

    fun setTheme(theme: Int) {
        Config.writeConfig(KEY_THEME, theme)
    }

    fun getThemeInfo(): ThemeList.Theme? {
        val themeIndex: Int = getTheme()
        return if (themeIndex >= 0 && themeIndex < ThemeList.themes.size) {
            ThemeList.themes[themeIndex]
        } else null
    }

    fun showDialog(context: Context) {
        RadioDialog(context).apply {
            setTitle(context.getString(R.string.theme_change_title))
            setFeedBackContent(context.getString(R.string.theme_change_tips))
            setPositive(context.getString(R.string.dialog_button_settings))
            setNegative(context.getString(R.string.dialog_button_cancel))
            setShouldCanceled(true)
            val names = mutableListOf<String>()
            ThemeList.themes.forEach { theme ->
                names.add(theme.title + if (theme.isDark) " (" + context.getString(R.string.theme_type_dark) + ")" else "")
            }
            setRadioData(names, currentThemeIndex) { which ->
                currentThemeIndex = which
            }
            setOnClickBottomListener(object : OnDialogListener {
                override fun onPositiveClick() {
                    dismiss()
                    DialogUtils.showConfirmDialog(
                            context,
                            context.getString(R.string.commont_dialog_title),
                            context.getString(R.string.theme_change_confirm_message),
                            context.getString(R.string.dialog_button_settings),
                            context.getString(R.string.dialog_button_cancel),
                            true,
                            object : SimpleDialogListener() {
                                override fun onPositiveClick() {
                                    setTheme(currentThemeIndex)
                                    ZixieContext.restartApp()
                                }
                            }

                    )
                }

                override fun onNegativeClick() {
                    dismiss()
                }

                override fun onCancel() {
                    dismiss()
                }
            })
        }.let {
            it.show()
        }
    }
}