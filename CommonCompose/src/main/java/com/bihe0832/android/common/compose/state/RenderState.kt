package com.bihe0832.android.common.compose.state

import androidx.compose.runtime.Composable
import java.util.Locale

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/3.
 * Description: Description
 *
 */

// 基类定义状态接口
interface RenderState {
    @Composable
    fun Content(currentLanguage: Locale)

}