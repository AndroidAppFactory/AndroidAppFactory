package com.bihe0832.android.common.about.compose

import android.content.Context
import android.text.TextUtils
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.common.compose.common.activity.CommonComposeActivity
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.utils.intent.IntentUtils
import java.util.Calendar
import com.bihe0832.android.framework.R as FrameworkR
import com.bihe0832.android.model.res.R as ModelResR

/**
 * Compose 版关于页面 Activity
 *
 * 对应 View 体系中的 AboutActivity + AboutFragment。
 * 继承 CommonComposeActivity，自动拥有 Toolbar + 主题 + 返回键处理。
 * 子类只需 override [getSettingsContent] 提供设置项列表。
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/3/25.
 */
open class AboutComposeActivity : CommonComposeActivity() {

    @Composable
    override fun getTitleName(): String {
        return stringResource(ModelResR.string.about)
    }

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                AboutPageContent()
            }
        }
    }

    /**
     * 关于页面的完整内容
     */
    @Composable
    protected open fun AboutPageContent() {
        val context = LocalContext.current
        val scrollState = rememberScrollState()

        // 观察更新 LiveData（通过 DisposableEffect + observe 避免依赖 runtime-livedata）
        val lifecycleOwner = LocalLifecycleOwner.current
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ========== 头部区域：App 图标 + 版本号 ==========
            AboutHeaderCompose(
                iconRes = getAppIconRes(),
                onDebugAction = { onDebugAction() }
            )

            // ========== 中间区域：设置项列表 ==========
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                getSettingsContent(context, updateData)
            }

            // ========== 底部区域：隐私协议 + 版权 ==========
            Spacer(modifier = Modifier.weight(1f))

            // 隐私协议链接
            PrivacyContent(context)

            // 版权文本
            Text(
                text = getCommonRightText(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    /**
     * 设置项列表内容，子类 override 此方法提供具体的设置项。
     * 可使用 SettingsItemGo / SettingsItemSwitch / SettingsItemCompose 来构建。
     *
     * @param context 上下文
     * @param updateData 更新数据，用于判断是否显示红点等
     */
    @Composable
    protected open fun getSettingsContent(context: Context, updateData: UpdateDataFromCloud?) {
        // 子类提供设置项
    }

    /**
     * 隐私协议内容
     */
    @Composable
    protected open fun PrivacyContent(context: Context) {
        val privacyUrl = ThemeResourcesManager.getString(FrameworkR.string.privacy_url)
        if (!TextUtils.isEmpty(privacyUrl)) {
            val privacyText =
                context.resources.getString(ModelResR.string.privacy_agreement_entrance)
            val annotatedString = AnnotatedString.fromHtml(privacyText)

            ClickableText(
                text = annotatedString,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                onClick = {
                    IntentUtils.openWebPage(context, privacyUrl)
                }
            )
        }
    }


    /**
     * 获取 App 图标资源 ID，子类可以 override
     */
    protected open fun getAppIconRes(): Int {
        return R.mipmap.icon
    }

    /**
     * 版权文本，子类可以 override
     */
    protected open fun getCommonRightText(): String {
        return "Copyright © 2019 - " + Calendar.getInstance()[Calendar.YEAR] + " " +
                ThemeResourcesManager.getString(ModelResR.string.author) +
                ". All Rights Reserved"
    }

    /**
     * Debug 连续点击触发的操作，子类可以 override
     */
    protected open fun onDebugAction() {
        // 子类可以实现 debug 功能
    }
}
