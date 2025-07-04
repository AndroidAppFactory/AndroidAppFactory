package com.bihe0832.android.common.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/2.
 * Description: Description
 *
 */


@Composable
fun dpToSp(dp: Dp): TextUnit {
    val density = LocalDensity.current
    return with(density) { (dp.value / fontScale).sp }
}