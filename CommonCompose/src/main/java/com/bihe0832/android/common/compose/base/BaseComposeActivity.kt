package com.bihe0832.android.common.compose.base

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bihe0832.android.common.compose.state.DensityState
import com.bihe0832.android.common.compose.state.LayerToGrayState
import com.bihe0832.android.common.compose.state.MultiLanguageState
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.state.ThemeState
import com.bihe0832.android.common.compose.ui.activity.ActivityThemeView
import com.bihe0832.android.lib.language.MultiLanguageHelper
import com.bihe0832.android.lib.utils.os.DisplayUtil
import java.util.Locale


abstract class BaseComposeActivity : ComponentActivity() {

    private var lastLocale = ""

    abstract fun getActivityContentRender(): RenderState

    open fun title(): String {
        return ""
    }

    override fun attachBaseContext(newBase: Context?) {
        if (supportMultiLanguage() && newBase != null) {
            val newContext = MultiLanguageHelper.modifyContextLanguageConfig(newBase)
            super.attachBaseContext(newContext)
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (supportMultiLanguage()) {
            MultiLanguageHelper.modifyContextLanguageConfig(
                resources, MultiLanguageHelper.getLanguageConfig(this)
            )
        }
        if (resetDensity()) {
            DisplayUtil.resetDensity(
                this,
                DensityState.getCurrentDensity(),
            )
        }
        if (isMain(this.javaClass.name)) {
            LayerToGrayState.update()
        }
        setContent {
            val currentLanguage by rememberUpdatedState(
                MultiLanguageState.getCurrentLanguageState()
            )
            if (supportMultiLanguage()) {
                LaunchedEffect(currentLanguage) {
                    if (supportMultiLanguage()) {
                        MultiLanguageHelper.modifyContextLanguageConfig(
                            this@BaseComposeActivity, currentLanguage
                        )
                        MultiLanguageHelper.modifyContextLanguageConfig(
                            resources, currentLanguage
                        )
                        if (!TextUtils.isEmpty(lastLocale)) {
                            if (currentLanguage.toLanguageTag() != lastLocale) {
                                onLocaleChanged(Locale.forLanguageTag(lastLocale), currentLanguage)
                            }
                        }
                        lastLocale = currentLanguage.toLanguageTag()
                    }
                }
            }

//            if (resetDensity()) {
//                val currentLanguage by rememberUpdatedState(DensityState.getCurrentDensity())
//                LaunchedEffect(currentLanguage) {
//
//                }
//            }
            val layerToGrayState by rememberUpdatedState(LayerToGrayState.isGrayEnabled())
            LaunchedEffect(layerToGrayState) {
                if (layerToGrayState) {
                    setLayerToGray()
                } else {
                    clearLayerToGray()
                }
            }
            val lifecycleOwner = LocalLifecycleOwner.current
            // 观察配置变化以处理置灰
            DisposableEffect(Unit) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_START) {
                        // 重新加载字符串资源
                        if (isMain(this.javaClass.name) || LayerToGrayState.isGrayEnabled()) {
                            LayerToGrayState.update()
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
            getActivityRootContentRender().Content()
        }
    }

    open fun getActivityRootContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                val themeType by rememberUpdatedState(ThemeState.getCurrentThemeState())
                ActivityThemeView(
                    themeType, getActivityContentRender()
                )
            }
        }
    }


    open fun resetDensity(): Boolean {
        return true
    }

    open fun supportMultiLanguage(): Boolean {
        return true
    }

    protected open fun exitAuto(): Boolean {
        return true
    }

    fun getLastLocale(): Locale {
        return Locale.forLanguageTag(lastLocale)
    }

    open fun onLocaleChanged(lastLocale: Locale, toLanguageTag: Locale) {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        onBackPressedSupportAction(exitAuto())
    }

    /**
     * onBackPressed final以后，外部完成返回键的一些额外处理工作
     */
    open fun onBack() {
        finish()
    }

    override fun finish() {
        intent = null
        super.finish()
    }
}

