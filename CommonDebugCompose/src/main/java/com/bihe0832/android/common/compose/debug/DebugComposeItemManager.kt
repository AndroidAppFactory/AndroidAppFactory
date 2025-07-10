package com.bihe0832.android.common.compose.debug

import androidx.compose.runtime.Composable
import java.util.Locale

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: Description
 *
 */
interface DebugComposeManager {
    @Composable
    fun getDebugComposeItem(key: DebugViewKey, currentLanguage: Locale)
}

open class DebugViewKey(val viewKey: String)


object DebugComposeItemManager {
    private var mDebugComposeManager: DebugComposeManager? = null
    fun setDebugComposeManagerImpl(impl: DebugComposeManager) {
        mDebugComposeManager = impl
    }

    @Composable
    fun getDebugComposeItem(key: DebugViewKey, currentLanguage: Locale) {
        mDebugComposeManager?.getDebugComposeItem(key, currentLanguage)
    }
}