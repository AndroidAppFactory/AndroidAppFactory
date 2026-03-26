package com.bihe0832.android.app.ui.navigation

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.common.about.compose.wrapper.SettingsItemFactory
import com.bihe0832.android.common.about.compose.wrapper.SettingsItemGo
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.permission.settings.PermissionFragment
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.ui.main.CommonRootActivity
import com.bihe0832.android.common.about.compose.item.SettingsItemCompose
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.framework.R as FrameworkR
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.model.res.R as ModelResR

/**
 * AAF 侧边栏导航内容（Compose 版）
 *
 * 与 View 体系中的 [AAFNavigationContentFragment] 对应，
 * 使用 [SettingsItemFactory] 构建设置项列表。
 *
 * 展示侧边栏的设置项列表，包括：
 * - 关于应用
 * - 消息中心
 * - 权限管理
 * - 意见反馈
 * - 分享应用
 * - 语言切换
 * - 清除缓存
 * - 关于开发者
 *
 * @author zixie code@bihe0832.com
 * Created on 2026/3/26.
 */

/**
 * 获取侧边栏内容的 RenderState
 *
 * @return RenderState 侧边栏内容渲染状态
 */
fun getAAFNavigationDrawerContentRender(): RenderState {
    return object : RenderState {
        @Composable
        override fun Content() {
            AAFNavigationContent()
        }
    }
}

/**
 * AAF 侧边栏导航内容 Composable
 *
 * 包含关于应用、消息中心、权限管理、意见反馈、分享应用、
 * 语言切换、清除缓存、关于开发者等设置项。
 * 自动观察更新状态和消息未读数，实时更新红点显示。
 */
@Composable
fun AAFNavigationContent() {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    // 观察更新 LiveData（通过 DisposableEffect + observe 避免依赖 runtime-livedata）
    var updateData by remember { mutableStateOf(UpdateInfoLiveData.value) }
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.Observer<UpdateDataFromCloud?> { data ->
            updateData = data
        }
        UpdateInfoLiveData.observe(lifecycleOwner, observer)
        onDispose {
            UpdateInfoLiveData.removeObserver(observer)
        }
    }

    // 观察消息未读数 LiveData
    var unreadNum by remember { mutableIntStateOf(AAFMessageManager.getUnreadNum()) }
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.Observer<Any?> { _ ->
            unreadNum = AAFMessageManager.getUnreadNum()
        }
        AAFMessageManager.getMessageLiveData().observe(lifecycleOwner, observer)
        onDispose {
            AAFMessageManager.getMessageLiveData().removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(MaterialTheme.colorScheme.primary),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = stringResource(ResR.string.app_name),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 关于应用
        SettingsItemFactory.AboutAPP(cloud = updateData) {
            RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_BASE_ABOUT)
        }

        // 消息中心
        SettingsItemFactory.Message(
            msgNum = if (unreadNum > 0) unreadNum else -1
        ) {
            RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_MESSAGE)
        }

        // 权限管理
        SettingsItemGo(
            mItemText = context.getString(ModelResR.string.common_permission_item_title_privacy),
            mItemIconRes = ResR.drawable.icon_privacy_tip,
            mShowDriver = true,
            mShowGo = true,
            mHeaderListener = {
                val title = context.getString(ModelResR.string.common_permission_item_title_privacy)
                CommonRootActivity.startCommonRootActivity(context, PermissionFragment::class.java, title)
            }
        )

        // 意见反馈
        SettingsItemFactory.FeedbackURL()

        // 分享应用
        SettingsItemFactory.ShareAPP(canSendAPK = true)

        // 语言切换
        SettingsItemGo(
            mItemText = context.getString(ModelResR.string.settings_language_title),
            mItemIconRes = FrameworkR.drawable.icon_language,
            mShowDriver = true,
            mShowGo = true,
            mHeaderListener = {
                RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_LANGUAGE)
            }
        )

        // 清除缓存
        if (activity != null) {
            SettingsItemFactory.ClearCache(activity)
        }
        // 关于开发者
        SettingsItemFactory.Zixie()
    }
}

// ==================== 预览函数 ====================

/**
 * 预览：AAF 侧边栏导航内容
 */
@Preview(name = "AAF 侧边栏", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewAAFNavigationContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // 侧边栏头部标题
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(MaterialTheme.colorScheme.primary),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = "AAF Demo",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 关于应用
        SettingsItemCompose(
            title = "关于应用",
            iconRes = ResR.drawable.icon_android,
            isBold = true,
            newNum = 0,
            showGo = true,
            showDivider = true
        )

        // 消息中心
        SettingsItemCompose(
            title = "消息中心",
            iconRes = ResR.drawable.icon_message,
            newNum = 3,
            showGo = true,
            showDivider = true
        )

        // 权限管理
        SettingsItemCompose(
            title = "权限管理",
            iconRes = ResR.drawable.icon_privacy_tip,
            showGo = true,
            showDivider = true
        )

        // 意见反馈
        SettingsItemCompose(
            title = "意见反馈",
            iconRes = ResR.drawable.icon_message,
            showGo = true,
            showDivider = true
        )

        // 分享应用
        SettingsItemCompose(
            title = "分享应用",
            iconRes = ResR.drawable.icon_share,
            showGo = true,
            showDivider = true
        )

        // 语言切换
        SettingsItemCompose(
            title = "语言切换",
            iconRes = ResR.drawable.icon_android,
            showGo = true,
            showDivider = true
        )

        // 清除缓存
        SettingsItemCompose(
            title = "清除缓存",
            iconRes = ResR.drawable.icon_delete_fill,
            showGo = true,
            showDivider = true
        )

        // 关于开发者
        SettingsItemCompose(
            title = "关于开发者",
            iconRes = ResR.drawable.icon_author,
            showGo = true,
            showDivider = false
        )
    }
}
