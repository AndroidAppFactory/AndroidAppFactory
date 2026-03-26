package com.bihe0832.android.common.about.compose.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.common.compose.ui.utils.BadgeView

/**
 * 红点/徽章 Compose 组件（兼容旧接口）
 *
 * 内部委托给通用的 BadgeView 实现。
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/3/25.
 */

/**
 * 红点/徽章组件
 *
 * @param num 数字，0 表示仅显示红点，>0 显示数字
 */
@Composable
fun SettingsBadge(num: Int) {
    BadgeView(
        num = num,
        dotSize = 8.dp,
        fontSize = 10.sp
    )
}