package com.bihe0832.android.common.compose.base
import android.app.Activity
import android.app.ActivityManager
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.text.TextUtils
import android.view.View
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.router.Routers
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.os.BuildUtils

/**
 * @param autoShowExitDialog 是否自动弹出退出弹框，如果：
 *
 *      为 true ，会弹出是否退出应用弹框
 *      为 false, 会直接调用 onBackAction()
 */
fun BaseComposeActivity.onBackPressedSupportAction(autoShowExitDialog: Boolean) {
    var topActivity = this.javaClass.name
    if (isMain(topActivity)) {
        mainBackAction(autoShowExitDialog)
        return
    }

    var activityNum = 0

    try {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.M) {
            val taskInfoList = am.appTasks
            for (i in taskInfoList.indices) {
                if (taskInfoList[i].taskInfo.baseIntent.component?.packageName.equals(
                        packageName, ignoreCase = true
                    )
                ) {
                    if (TextUtils.isEmpty(topActivity)) {
                        topActivity = taskInfoList[i].taskInfo?.topActivity?.className ?: ""
                    }
                    activityNum += taskInfoList[i].taskInfo?.numActivities ?: 0
                } else if (i > 0) {
                    break
                }
            }
        } else if (BuildUtils.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val taskInfoList = am.runningAppProcesses
            for (i in taskInfoList.indices) {
                if (taskInfoList[i]?.processName?.startsWith(
                        packageName, ignoreCase = true
                    ) == true
                ) {
                    activityNum += 1
                } else if (i > 0) {
                    break
                }
            }
        } else {
            val taskInfoList = am.getRunningTasks(Int.MAX_VALUE)
            for (i in taskInfoList.indices) {
                if (taskInfoList[i].baseActivity?.packageName.equals(
                        packageName, ignoreCase = true
                    )
                ) {
                    if (TextUtils.isEmpty(topActivity)) {
                        topActivity = taskInfoList[i].topActivity?.className ?: ""
                    }
                    activityNum += taskInfoList[i].numActivities
                } else if (i > 0) {
                    break
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    if (activityNum < 2) {
        if (isMain(topActivity)) {
            mainBackAction(autoShowExitDialog)
        } else {
            goMain()
            onBack()
        }
    } else {
        if (isMain(topActivity)) {
            mainBackAction(autoShowExitDialog)
        } else {
            onBack()
        }
    }
}

internal fun isMain(activityName: String): Boolean {
    return  true
//    return Routers.getMainActivityList().find { it.name.equals(activityName, true) } != null
}

private fun BaseComposeActivity.goMain() {
    var hasStart = false
    val mainActivityList = Routers.getMainActivityList()
    mainActivityList?.let {
        if (it.isNotEmpty()) {
            it.first()?.let { firstElement ->
                hasStart = startActivity(this, firstElement)
            }
        }
    }

    if (!hasStart) {
        APKUtils.startApp(this, packageName)
    }
}

private fun startActivity(activity: BaseComposeActivity, threadClazz: Class<out Activity?>): Boolean {
    try {
        val intent = Intent(activity, threadClazz)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activity.startActivity(intent)
        return true
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

internal fun BaseComposeActivity.setLayerToGray() {
    Paint().apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
            setSaturation(0f)
        })
    }.let {
        window.decorView.setLayerType(View.LAYER_TYPE_HARDWARE, it)
    }
}

internal fun BaseComposeActivity.clearLayerToGray() {
    window.decorView.setLayerType(View.LAYER_TYPE_NONE, null)
}

internal fun BaseComposeActivity.mainBackAction(autoShowExitDialog: Boolean) {
    if (autoShowExitDialog) {
        ZixieContext.exitAPP(null)
    } else {
        onBack()
    }
}
