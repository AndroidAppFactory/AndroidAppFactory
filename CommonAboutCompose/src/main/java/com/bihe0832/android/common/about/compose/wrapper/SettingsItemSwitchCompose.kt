package com.bihe0832.android.common.about.compose.wrapper

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.about.compose.item.SettingsItemCompose
import com.bihe0832.android.lib.aaf.res.R as ResR

/**
 * Switch 类型设置项的便捷封装，参数命名与 View 体系中的 SettingsDataSwitch 完全一致。
 *
 * @param title 标题文本
 * @param description 描述/副标题文本，为空时不显示
 * @param mItemIconRes 左侧图标资源 ID，-1 表示不显示
 * @param mItemIconResColorFilter 图标着色，null 表示使用默认主题色
 * @param tips 右侧提示文本
 * @param isChecked Switch 当前状态，默认 true
 * @param mShowDriver 是否显示底部分割线，默认 true
 * @param onClickListener 点击事件
 * @param onCheckedChangeListener Switch 状态变化回调
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/3/25.
 */
@Composable
fun SettingsItemSwitch(
    title: String,
    description: String = "",
    @DrawableRes mItemIconRes: Int = -1,
    mItemIconResColorFilter: Color? = null,
    tips: String = "",
    isChecked: Boolean = true,
    mShowDriver: Boolean = true,
    onClickListener: ((context: Context) -> Unit)? = null,
    onCheckedChangeListener: ((Boolean) -> Unit)? = null,
) {
    SettingsItemCompose(
        title = title,
        description = description,
        iconRes = mItemIconRes,
        iconTint = mItemIconResColorFilter,
        tipsText = tips,
        isChecked = isChecked,
        showDivider = mShowDriver,
        isBold = true,
        onClick = onClickListener,
        onCheckedChange = onCheckedChangeListener,
    )
}

// ==================== 预览函数 ====================

/**
 * Switch 样式：标题 + 描述 + 开关
 */
@Preview(name = "Switch - 基础样式", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewSettingsItemSwitchBasic() {
    Column {
        SettingsItemCompose(
            title = "接受新消息通知",
            description = "开启后，不错过重要通知",
            iconRes = ResR.drawable.icon_message,
            isBold = true,
            isChecked = true,
            showDivider = true,
            onCheckedChange = {}
        )
        SettingsItemCompose(
            title = "深色模式",
            description = "跟随系统或手动切换",
            isChecked = false,
            showDivider = true,
            onCheckedChange = {}
        )
        SettingsItemCompose(
            title = "自动更新",
            description = "WiFi 环境下自动更新应用",
            iconRes = ResR.drawable.icon_update,
            isChecked = true,
            showDivider = false,
            onCheckedChange = {}
        )
    }
}

/**
 * Switch 样式：极简 - 仅标题 + 开关
 */
@Preview(name = "Switch - 极简样式", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewSettingsItemSwitchMinimal() {
    Column {
        SettingsItemCompose(
            title = "仅标题和开关（开）",
            isChecked = true,
            showDivider = true,
            onCheckedChange = {}
        )
        SettingsItemCompose(
            title = "仅标题和开关（关）",
            isChecked = false,
            showDivider = true,
            onCheckedChange = {}
        )
        SettingsItemCompose(
            title = "无开关无跳转（纯展示）",
            description = "此项不可交互",
            showDivider = false
        )
    }
}

/**
 * Switch + Go 混合样式
 */
@Preview(name = "Switch + Go 混合样式", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewSettingsItemSwitchWithGo() {
    Column {
        SettingsItemCompose(
            title = "同时含开关和跳转",
            description = "带图标的完整样式",
            iconRes = ResR.drawable.icon_android,
            tipsText = "去设置",
            showGo = true,
            isChecked = true,
            showDivider = true,
            onClick = {},
            onCheckedChange = {}
        )
        SettingsItemCompose(
            title = "通知权限",
            description = "点击跳转到系统设置页",
            tipsText = "去设置",
            showGo = true,
            showDivider = false,
            onClick = {}
        )
    }
}
