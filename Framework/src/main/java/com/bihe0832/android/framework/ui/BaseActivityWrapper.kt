package com.bihe0832.android.framework.ui

import android.app.Activity
import android.app.ActivityManager
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.text.TextUtils
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.router.Routers
import com.bihe0832.android.lib.utils.apk.APKUtils

fun BaseActivity.onBackPressedSupportAction(autoExit: Boolean) {
    val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
    val taskInfoList = am.getRunningTasks(Int.MAX_VALUE)
    var topActivity = ""
    var activityNum = 0
    for (i in taskInfoList.indices) {
        if (taskInfoList[i].baseActivity.packageName.equals(packageName, ignoreCase = true)) {
            if (TextUtils.isEmpty(topActivity)) {
                topActivity = taskInfoList[i].topActivity.className
            }
            activityNum += taskInfoList[i].numActivities
        } else if (i > 0) {
            break
        }
    }

    if (activityNum < 2) {
        if (isMain(topActivity)) {
            if (autoExit) {
                ZixieContext.exitAPP(null)
            } else {
                onBack()
            }
        } else {
            finishAndGoMain()
        }
    } else {
        if (isMain(topActivity)) {
            if (autoExit) {
                ZixieContext.exitAPP(null)
            } else {
                onBack()
            }
        } else {
            onBack()
        }
    }
}

private fun isMain(activityName: String): Boolean {
    return Routers.getMainActivityList().find { it.name.equals(activityName, true) } != null
}

private fun BaseActivity.finishAndGoMain() {
    var hasStart = false
    val mainActivityList = Routers.getMainActivityList()
    mainActivityList?.let {
        if (it.isNotEmpty()) {
            it.first()?.let {firstElement ->
                hasStart = startActivity(this, firstElement)
            }
        }
    }

    if (!hasStart) {
        APKUtils.startApp(this, packageName)
    }
    finish()
}

private fun startActivity(activity: BaseActivity, threadClazz: Class<out Activity?>): Boolean {
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