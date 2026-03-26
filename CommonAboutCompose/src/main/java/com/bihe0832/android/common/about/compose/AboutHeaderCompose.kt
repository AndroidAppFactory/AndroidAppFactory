package com.bihe0832.android.common.about.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.model.res.R as ModelResR

/**
 * 关于页面头部组件：App 图标 + 版本号
 *
 * 支持点击图标显示详细版本号，以及连续快击触发 Debug 模式。
 *
 * @param iconRes App 图标资源 ID
 * @param iconSize 图标大小，默认 72.dp
 * @param versionFontSize 版本号字体大小，默认 14.sp
 * @param topSpacing 图标上方间距，默认 56.dp
 * @param iconToVersionSpacing 图标与版本号之间的间距，默认 25.dp
 * @param bottomSpacing 版本号下方间距，默认 56.dp
 * @param debugClickThreshold 触发 Debug 的连续快击次数，默认 4 次
 * @param debugClickInterval 判定为连续点击的最大间隔毫秒数，默认 500ms
 * @param onDebugAction Debug 模式触发时的回调
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/3/25.
 */
@Composable
fun AboutHeaderCompose(
    iconRes: Int,
    iconSize: Dp = 72.dp,
    versionFontSize: TextUnit = 14.sp,
    topSpacing: Dp = 56.dp,
    iconToVersionSpacing: Dp = 25.dp,
    bottomSpacing: Dp = 56.dp,
    debugClickThreshold: Int = 4,
    debugClickInterval: Long = 500L,
    onDebugAction: () -> Unit = {}
) {
    val context = LocalContext.current

    // 版本号文本状态，点击后显示详细版本
    var versionText by remember {
        mutableStateOf(
            context.resources.getString(ModelResR.string.settings_update_current) + ZixieContext.getVersionName()
        )
    }

    // Debug 连续快击的状态
    var debugClickCount by remember { mutableStateOf(0) }
    var lastClickTime by remember { mutableStateOf(0L) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(topSpacing))

        // App 图标（点击触发 ShowDebugClick 逻辑）
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "App Icon",
            modifier = Modifier
                .size(iconSize)
                .clickable {
                    val currentTime = System.currentTimeMillis()
                    val elapsed = currentTime - lastClickTime
                    lastClickTime = currentTime
                    if (elapsed < debugClickInterval) {
                        debugClickCount++
                        if (debugClickCount >= debugClickThreshold) {
                            onDebugAction()
                            debugClickCount = 0
                        }
                    } else {
                        debugClickCount = 1
                    }
                    // 普通点击 - 显示详细版本号
                    versionText =
                        context.resources.getString(ModelResR.string.settings_update_current) + ZixieContext.getVersionNameAndCode()
                }
        )

        Spacer(modifier = Modifier.height(iconToVersionSpacing))

        // 版本号文本
        Text(
            text = versionText,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = versionFontSize,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(bottomSpacing))
    }
}

// ==================== 预览函数 ====================

/**
 * 默认样式预览
 */
@Preview(name = "Header - 默认样式", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewAboutHeaderDefault() {
    AboutHeaderCompose(
        iconRes = android.R.mipmap.sym_def_app_icon
    )
}

/**
 * 自定义间距和字体大小
 */
@Preview(name = "Header - 自定义间距", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewAboutHeaderCustomSpacing() {
    AboutHeaderCompose(
        iconRes = android.R.mipmap.sym_def_app_icon,
        iconSize = 96.dp,
        versionFontSize = 16.sp,
        topSpacing = 32.dp,
        iconToVersionSpacing = 16.dp,
        bottomSpacing = 32.dp
    )
}

/**
 * 紧凑样式预览
 */
@Preview(name = "Header - 紧凑样式", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewAboutHeaderCompact() {
    AboutHeaderCompose(
        iconRes = android.R.mipmap.sym_def_app_icon,
        iconSize = 48.dp,
        versionFontSize = 12.sp,
        topSpacing = 24.dp,
        iconToVersionSpacing = 12.dp,
        bottomSpacing = 24.dp
    )
}
