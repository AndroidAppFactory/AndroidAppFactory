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
 * Go 类型设置项的便捷封装，参数命名与 View 体系中的 SettingsDataGo 完全一致。
 *
 * @param mItemText 标题文本，支持 HTML 格式
 * @param mItemIconRes 左侧图标资源 ID，-1 表示不显示
 * @param mItemIconResColorFilter 图标着色，null 表示使用默认主题色
 * @param mTipsText 右侧提示文本，支持 HTML 格式
 * @param mItemNewNum 红点/徽章数字，-1 表示不显示，0 表示仅显示红点，>0 显示数字
 * @param mShowGo 是否显示右箭头，默认 true
 * @param mShowDriver 是否显示底部分割线，默认 false
 * @param mHeaderTextBold 标题是否加粗，默认 false
 * @param mHeaderListener 点击事件
 * @param mHeaderTipsListener 提示文字点击事件，null 时与 mHeaderListener 一致
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/3/25.
 */
@Composable
fun SettingsItemGo(
    mItemText: String,
    @DrawableRes mItemIconRes: Int = -1,
    mItemIconResColorFilter: Color? = null,
    mTipsText: String = "",
    mItemNewNum: Int = -1,
    mShowGo: Boolean = true,
    mShowDriver: Boolean = false,
    mHeaderTextBold: Boolean = false,
    mHeaderListener: ((context: Context) -> Unit)? = null,
    mHeaderTipsListener: ((context: Context) -> Unit)? = null,
) {
    SettingsItemCompose(
        title = mItemText,
        iconRes = mItemIconRes,
        iconTint = mItemIconResColorFilter,
        tipsText = mTipsText,
        newNum = mItemNewNum,
        showGo = mShowGo,
        showDivider = mShowDriver,
        isBold = mHeaderTextBold,
        onClick = mHeaderListener,
        onTipsClick = mHeaderTipsListener,
    )
}

// ==================== 预览函数 ====================

/**
 * 基础 Go 样式：图标 + 标题 + 箭头 + 分割线
 */
@Preview(name = "Go - 基础样式", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewSettingsItemGoBasic() {
    Column {
        SettingsItemCompose(
            title = "关于应用",
            iconRes = ResR.drawable.icon_android,
            showGo = true,
            showDivider = true
        )
        SettingsItemCompose(
            title = "版本记录",
            iconRes = ResR.drawable.icon_menu,
            showGo = true,
            showDivider = true
        )
        SettingsItemCompose(
            title = "分享应用",
            iconRes = ResR.drawable.icon_share,
            showGo = true,
            showDivider = false
        )
    }
}

/**
 * Go 样式：加粗标题 + 红点/数字徽章
 */
@Preview(name = "Go - 徽章样式", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewSettingsItemGoBadge() {
    Column {
        SettingsItemCompose(
            title = "检查更新（红点）",
            iconRes = ResR.drawable.icon_update,
            isBold = true,
            newNum = 0,
            showGo = true,
            showDivider = true
        )
        SettingsItemCompose(
            title = "消息中心（数字徽章）",
            iconRes = ResR.drawable.icon_message,
            newNum = 3,
            showGo = true,
            showDivider = true
        )
        SettingsItemCompose(
            title = "待办事项（大数字）",
            iconRes = ResR.drawable.icon_android,
            newNum = 99,
            showGo = true,
            showDivider = false
        )
    }
}

/**
 * Go 样式：带提示文字
 */
@Preview(name = "Go - 提示文字样式", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewSettingsItemGoWithTips() {
    Column {
        SettingsItemCompose(
            title = "客服QQ",
            iconRes = ResR.drawable.icon_qq,
            tipsText = "<u>123456789</u>",
            showGo = true,
            showDivider = true
        )
        SettingsItemCompose(
            title = "微信公众号",
            iconRes = ResR.drawable.icon_wechat,
            tipsText = "<u>点击关注</u>",
            showGo = true,
            showDivider = true
        )
        SettingsItemCompose(
            title = "意见反馈",
            iconRes = ResR.drawable.icon_message,
            tipsText = "<u>feedback@test.com</u>",
            showGo = true,
            showDivider = false
        )
    }
}

/**
 * Go 样式：极简 - 无图标 / 无箭头 / 纯文本
 */
@Preview(name = "Go - 极简样式", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewSettingsItemGoMinimal() {
    Column {
        SettingsItemCompose(
            title = "无图标无箭头",
            showDivider = true
        )
        SettingsItemCompose(
            title = "仅标题和箭头",
            showGo = true,
            showDivider = true
        )
        SettingsItemCompose(
            title = "仅标题",
            showDivider = false
        )
    }
}

/**
 * Go 样式：长文本溢出 + 自定义着色
 */
@Preview(name = "Go - 长文本和自定义颜色", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewSettingsItemGoOverflow() {
    Column {
        SettingsItemCompose(
            title = "这是一段非常非常长的标题文本用来验证溢出省略号效果是否正常工作",
            iconRes = ResR.drawable.icon_android,
            showGo = true,
            showDivider = true
        )
        SettingsItemCompose(
            title = "自定义图标颜色",
            iconRes = ResR.drawable.icon_android,
            iconTint = Color(0xFF4CAF50),
            showGo = true,
            showDivider = true
        )
        SettingsItemCompose(
            title = "加粗 + 提示 + 徽章 + 箭头（完整样式）",
            iconRes = ResR.drawable.icon_update,
            isBold = true,
            tipsText = "v2.0.0",
            newNum = 1,
            showGo = true,
            showDivider = false
        )
    }
}
