package com.bihe0832.android.common.compose.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.lifecycle.LifecycleHelper


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/6/30.
 * Description: Description
 *
 */

object LayerToGrayState {

    private var _isGrayEnabled by mutableStateOf(isEnabled())

    private fun isEnabled(): Boolean {
        return (LifecycleHelper.getCurrentTime() / 1000) in Config.readConfig(
            Constants.CONFIG_KEY_LAYER_START_VALUE,
            0L,
        )..Config.readConfig(Constants.CONFIG_KEY_LAYER_END_VALUE, 0L)
    }

    fun update() {
        _isGrayEnabled = isEnabled()
    }

    fun setGrayEnabled(boolean: Boolean) {
        _isGrayEnabled = boolean
    }

    fun isGrayEnabled(): Boolean {
        return _isGrayEnabled
    }
}
