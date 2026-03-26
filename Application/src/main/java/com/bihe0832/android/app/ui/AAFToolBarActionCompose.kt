package com.bihe0832.android.app.ui

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.common.compose.ui.utils.BadgeView
import com.bihe0832.android.common.qrcode.QrcodeUtils
import com.bihe0832.android.lib.aaf.res.R

/**
 * AAF 标题栏操作按钮通用 Compose 组件
 *
 * 提供扫码按钮和消息中心按钮（带未读红点），供各 Activity 的标题栏复用。
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/3/25.
 */

/**
 * 扫码按钮
 *
 * 点击后调用 QrcodeUtils.openQrScan 打开扫码页面。
 */
@Composable
fun ScanIconButton() {
    val context = LocalContext.current
    IconButton(onClick = {
        (context as? Activity)?.let { QrcodeUtils.openQrScan(it) }
    }) {
        Icon(
            modifier = Modifier.width(24.dp),
            imageVector = ImageVector.vectorResource(R.drawable.icon_scan),
            contentDescription = "扫描二维码"
        )
    }
}

/**
 * 消息中心按钮（带未读红点）
 *
 * 自动监听消息未读数 LiveData，有未读消息时在按钮右上角显示红点/数字徽章。
 * 点击后跳转到消息中心页面。
 */
@Composable
fun MessageIconButton() {
    val lifecycleOwner = LocalLifecycleOwner.current

    // 观察消息未读数 LiveData
    var unreadCount by remember { mutableIntStateOf(AAFMessageManager.getUnreadNum()) }
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.Observer<Any?> { _ ->
            unreadCount = AAFMessageManager.getUnreadNum()
        }
        AAFMessageManager.getMessageLiveData().observe(lifecycleOwner, observer)
        onDispose {
            AAFMessageManager.getMessageLiveData().removeObserver(observer)
        }
    }

    Box {
        IconButton(onClick = {
            RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_MESSAGE)
        }) {
            Icon(
                modifier = Modifier.width(24.dp),
                imageVector = ImageVector.vectorResource(R.drawable.icon_message),
                contentDescription = "消息中心"
            )
        }
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
            ) {
                BadgeView(
                    num = unreadCount,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * 默认的标题栏操作按钮组合（扫码 + 消息中心）
 *
 * 供 getTitleActionContentRender() 直接使用。
 */
@Composable
fun AAFDefaultTitleActions() {
    Row {
        ScanIconButton()
        MessageIconButton()
    }
}
