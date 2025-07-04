package com.bihe0832.android.common.compose.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/6/30.
 * Description: Description
 *
 */

object DensityState {

    private var _currentDensity by mutableStateOf(0f)

    fun changeDensity(density: Float) {
        _currentDensity = density
    }

    fun getCurrentDensity(): Float {
        return _currentDensity
    }

}
