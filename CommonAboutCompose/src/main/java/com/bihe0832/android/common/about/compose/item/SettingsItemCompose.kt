package com.bihe0832.android.common.about.compose.item

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.lib.aaf.res.R as ResR

/**
 * Settings Item Compose 统一设置项组件
 *
 * 合并了原 SettingsGoCompose 和 SettingsSwitchCompose，
 * 对应 View 体系中的 SettingsDataGo / SettingsDataSwitch 及其对应的 Holder 和布局。
 * 包含：图标 + 标题(+描述) + 提示文字 + 红点徽章 + 右箭头 + Switch 开关 + 分割线
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/3/25.
 */

/**
 * 统一设置项 Composable
 *
 * @param title 标题文本，支持 HTML 格式
 * @param description 描述/副标题文本，支持 HTML 格式，为空时不显示
 * @param iconRes 左侧图标资源 ID，-1 表示不显示
 * @param iconTint 图标着色，null 表示使用默认主题色
 * @param tipsText 右侧提示文本，支持 HTML 格式
 * @param newNum 红点/徽章数字，-1 表示不显示，0 表示仅显示红点，>0 显示数字
 * @param showGo 是否显示右箭头
 * @param showDivider 是否显示底部分割线
 * @param descriptionTopPadding 描述文字与标题的顶部间距，默认 4dp
 * @param titleFontSize 标题字体大小，默认 14sp
 * @param descriptionFontSize 描述/副标题字体大小，默认 10sp
 * @param switchScale Switch 开关缩放比例，默认 0.8f（缩小到 80%）
 * @param isBold 标题是否加粗
 * @param isChecked Switch 当前状态
 * @param onClick 点击事件
 * @param onTipsClick 提示文字点击事件，null 时与 onClick 一致
 * @param onCheckedChange Switch 状态变化回调，非空时显示 Switch
 */
@Composable
fun SettingsItemCompose(
    title: String,
    description: String = "",
    @DrawableRes iconRes: Int = -1,
    iconTint: Color? = null,
    tipsText: String = "",
    newNum: Int = -1,
    showGo: Boolean = false,
    showDivider: Boolean = false,
    descriptionTopPadding: Dp = 8.dp,
    titleFontSize: TextUnit = 14.sp,
    descriptionFontSize: TextUnit = 10.sp,
    switchScale: Float = 0.8f,
    isBold: Boolean = false,
    isChecked: Boolean = false,
    onClick: ((context: Context) -> Unit)? = null,
    onTipsClick: ((context: Context) -> Unit)? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    val context = LocalContext.current
    var checkedState by remember(isChecked) { mutableStateOf(isChecked) }

    Column(modifier = Modifier.wrapContentHeight()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable { onClick.invoke(context) }
                    } else {
                        Modifier
                    }
                )
                .padding(start = 16.dp, end = 10.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 左侧图标
            if (iconRes != -1) {
                Icon(
                    imageVector = ImageVector.vectorResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconTint ?: MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // 2. 标题和描述（占据剩余空间）
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = AnnotatedString.fromHtml(title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = titleFontSize,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = AnnotatedString.fromHtml(description),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = descriptionFontSize,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = descriptionTopPadding)
                    )
                }
            }

            // 3. 提示文字
            if (tipsText.isNotEmpty()) {
                Text(
                    text = AnnotatedString.fromHtml(tipsText),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable {
                            (onTipsClick ?: onClick)?.invoke(context)
                        }
                        .padding(end = 6.dp)
                )
            }

            // 4. 红点/徽章
            if (newNum >= 0) {
                SettingsBadge(num = newNum)
                Spacer(modifier = Modifier.width(4.dp))
            }

            // 5. 右箭头
            if (showGo) {
                Icon(
                    imageVector = ImageVector.vectorResource(ResR.drawable.icon_right_go),
                    contentDescription = null,
                    modifier = Modifier.height(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 6. Switch 开关
            if (onCheckedChange != null) {
                Switch(
                    modifier = Modifier.scale(switchScale),
                    checked = checkedState,
                    onCheckedChange = { newValue ->
                        checkedState = newValue
                        onCheckedChange.invoke(newValue)
                    }
                )
            }
        }

        // 7. 底部分割线
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        }
    }
}

// ==================== 预览函数 ====================

/**
 * 模拟真实设置页面（Go + Switch 混合）
 */
@Preview(name = "完整设置页面", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewSettingsItemFullPage() {
    Column {
        SettingsItemCompose(
            title = "关于应用",
            iconRes = ResR.drawable.icon_android,
            isBold = true,
            newNum = 0,
            showGo = true,
            showDivider = true
        )
        SettingsItemCompose(
            title = "消息中心",
            iconRes = ResR.drawable.icon_message,
            newNum = 5,
            showGo = true,
            showDivider = true
        )
        SettingsItemCompose(
            title = "消息推送",
            description = "开启后接收最新动态",

            iconRes = ResR.drawable.icon_message,
            isChecked = true,
            showDivider = true,
            onCheckedChange = {}
        )
        SettingsItemCompose(
            title = "深色模式",
            description = "跟随系统设置自动切换",
            isBold = true,
            descriptionTopPadding = 10.dp,
            isChecked = false,
            showDivider = true,
            onCheckedChange = {}
        )
        SettingsItemCompose(
            title = "客服QQ",
            iconRes = ResR.drawable.icon_qq,
            tipsText = "<u>123456789</u>",
            showGo = true,
            showDivider = true
        )
        SettingsItemCompose(
            title = "清除缓存",
            iconRes = ResR.drawable.icon_delete_fill,
            showGo = true,
            showDivider = true
        )
        SettingsItemCompose(
            title = "关于开发者",
            iconRes = ResR.drawable.icon_author,
            showGo = true,
            showDivider = false
        )
    }
}
