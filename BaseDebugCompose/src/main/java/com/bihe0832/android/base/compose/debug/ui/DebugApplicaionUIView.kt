package com.bihe0832.android.base.compose.debug.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bihe0832.android.app.compose.AAFComposeStateManager
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.app.shortcut.AAFShortcutManager
import com.bihe0832.android.base.compose.debug.R
import com.bihe0832.android.base.compose.debug.language.DebugLanguageComposeView
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.compose.state.AAFDarkColorScheme
import com.bihe0832.android.common.compose.state.AAFLightColorScheme
import com.bihe0832.android.common.compose.state.ThemeState
import com.bihe0832.android.common.compose.state.aafStringResource
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.config.ZixieLimitConfig
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.language.MultiLanguageHelper
import com.bihe0832.android.lib.lifecycle.ActivityObserver
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog
import java.util.Locale

@Composable
fun DebugApplicaionUIView() {
    val list = listOf(
        "com.bihe0832.android.test.DefaultAlias",
        "com.bihe0832.android.test.FestivalAlias"
    )
    DebugContent {
        DebugTips(stringResource(R.string.app_name))
        DebugTips(aafStringResource(R.string.app_name))
        DebugItem("哀悼日全局置灰") {
            Config.writeConfig(
                Constants.CONFIG_KEY_LAYER_START_VALUE, System.currentTimeMillis() / 1000 - 3600
            )
            Config.writeConfig(
                Constants.CONFIG_KEY_LAYER_END_VALUE, System.currentTimeMillis() / 1000 + 3600
            )
            ZixieContext.restartApp()
        }

        DebugItem("哀悼日解除置灰") {
            Config.writeConfig(Constants.CONFIG_KEY_LAYER_START_VALUE, 0)
            Config.writeConfig(Constants.CONFIG_KEY_LAYER_END_VALUE, 0)
            ZixieContext.restartApp()
        }

        DebugItem("切换桌面图标1") {
            AAFShortcutManager.changeIcon(it, AAFShortcutManager.defaultShortcut)

        }

        DebugItem("切换桌面图标2") {
            AAFShortcutManager.changeIcon(it, "com.bihe0832.android.test.FestivalAlias")
        }

        DebugItem("一键换肤，暗夜模式") {
            ThemeState.changeTheme(AAFDarkColorScheme)
        }

        DebugItem("一键换肤，亮色模式") {
            ThemeState.changeTheme(AAFLightColorScheme)
        }

        DebugItem("应用前后台信息") { testAPPObserver() }
        DebugItem("多语言测试") { showLanguageInfo(it) }

        DebugItem("多语言切换") { RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_LANGUAGE) }

        DebugComposeItem("多语言调试", "DebugLanguageComposeView") {
            DebugLanguageComposeView()
        }
        DebugItem("切换语言到中文") {
            AAFComposeStateManager.changeLanguage(it, Locale.CHINESE)
        }
        DebugItem("切换语言到英文") {
            AAFComposeStateManager.changeLanguage(it, Locale.US)
        }

        DebugItem("查看使用次数") {
            testHasTimes()
        }

        DebugItem("消耗次数") {
            testCostTimes()
            testHasTimes()
        }
    }
}

val scene1 = "Test113"
val scene2 = "Test221"
val scene3 = "Test313"
val scene4 = "Test421"
private fun testHasTimes() {
    ZLog.d(
        "testHasTimes",
        "scene1  ,getCanUseTimes： ${
            ZixieLimitConfig.getCanUseTimes(scene1, 1, 3)
        },hasTimes： ${ZixieLimitConfig.hasTimes(scene1, 1, 3)}"
    )
    ZLog.d(
        "testHasTimes",
        "scene3  ,getCanUseTimes： ${
            ZixieLimitConfig.getCanUseTimes(scene3, 1, 3,true)
        },hasTimes： ${ZixieLimitConfig.hasTimes(scene3, 1, 3,true)}"
    )
    ZLog.d(
        "testHasTimes",
        "scene2  ,getCanUseTimes： ${
            ZixieLimitConfig.getCanUseTimes(scene2, 2, 1)
        },hasTimes： ${ZixieLimitConfig.hasTimes( scene2, 2, 1)}"
    )
    ZLog.d(
        "testHasTimes",
        "scene4  ,getCanUseTimes： ${
            ZixieLimitConfig.getCanUseTimes(scene4, 2, 1,true)
        },hasTimes： ${ZixieLimitConfig.hasTimes( scene4, 2, 1,true)}"
    )

}

private fun testCostTimes() {
    ZixieLimitConfig.costTimes(scene1, 1)
    ZixieLimitConfig.costTimes(scene3, 2,true)
    ZixieLimitConfig.costTimes(scene2, 2)
    ZixieLimitConfig.costTimes(scene4, 2,true)
}

private fun testAPPObserver() {
    ZLog.d("testAPPObserver", "getAPPStartTime ： ${ApplicationObserver.getAPPStartTime()}")
    ZLog.d("testAPPObserver", "getLastPauseTime ： ${ApplicationObserver.getLastPauseTime()}")
    ZLog.d(
        "testAPPObserver", "getLastResumedTime ： ${ApplicationObserver.getLastResumedTime()}"
    )
    ZLog.d("testAPPObserver", "getCurrentActivity ： ${ActivityObserver.getCurrentActivity()}")
    ActivityObserver.getActivityList().forEach {
        ZLog.d(
            "testAPPObserver",
            "getCurrentActivity ： ${it.javaClass.name} - ${it.hashCode()} - ${it.taskId}"
        )
    }
}


private fun showLanguageInfo(context: Context) {
    DebugUtilsV2.showInfo(context, "引用当前多语言设置", mutableListOf<String>().apply {
        add("系统当前语言: ${MultiLanguageHelper.getSystemLocale().displayName}")
        add("应用当前语音: ${MultiLanguageHelper.getContextLocale(context).displayName}")
        add("应用设置语音: ${MultiLanguageHelper.getLanguageConfig(context).displayName}")
        add("页面Context: ${context.resources.getString(R.string.debug_msg)}")
        add(
            "页面Context实时: ${
                MultiLanguageHelper.getRealResources(context).getString(R.string.debug_msg)
            }"
        )
        add("Application Context: ${ZixieContext.applicationContext!!.resources.getString(R.string.debug_msg)}")
        add(
            "Application Context 实时: ${
                MultiLanguageHelper.getRealResources(ZixieContext.applicationContext!!)
                    .getString(R.string.debug_msg)
            }"
        )
    })
}