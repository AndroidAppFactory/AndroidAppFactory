package com.bihe0832.android.common.praise

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.superapp.APPMarketHelper
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils
import java.util.concurrent.TimeUnit

object UserPraiseManager {

    const val KEY_PRAISE_DONE = "KEY_PRAISE_DONE"
    const val KEY_PRAISE_VERSION = "KEY_PRAISE_VERSION"
    const val KEY_PRAISE_LAST_SHOW_TIME = "KEY_PRAISE_LAST_SHOW_DATE"
    private var mMarketPackageName: String = ""
    private val mInit: Boolean

    private var mNeedShow = false

    //APP 使用次数超过多少才显示
    private var mAppUseTimeThreshold = 1

    //APP 使用超过几个自然天才显示
    private var mAppUseDayThreshold = 1

    //APP 距离上次弹框超过多久才可以继续弹
    private var mShowInterval = 2

    //弹框内容
    private var mDialogContent = ""

    init {
        //1_3_3_2_这里一个测试
        val praiseConfig = Config.readConfig(KEY_PRAISE_DONE, "")
        praiseConfig.split(":".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray().let { temp ->
            mNeedShow = ConvertUtils.getSafeValueFromArray(temp, 0, "") == "1"
            mAppUseTimeThreshold = ConvertUtils.parseInt(ConvertUtils.getSafeValueFromArray(temp, 1, "1"))
            mAppUseDayThreshold = ConvertUtils.parseInt(ConvertUtils.getSafeValueFromArray(temp, 2, "1"))
            mShowInterval = ConvertUtils.parseInt(ConvertUtils.getSafeValueFromArray(temp, 3, "2"))
            mDialogContent = ConvertUtils.getSafeValueFromArray(temp, 4, "")
            mMarketPackageName = APPMarketHelper.getFirstMarket(ZixieContext.applicationContext)
            mInit = true
        }
    }

    fun canShowUserPraiseDialog(): Boolean {

        if (!mNeedShow) {
            return false
        }

        if (Config.readConfig(KEY_PRAISE_DONE, 0) == 1) {
            return false
        }

        if (mMarketPackageName.isEmpty()) {
            return false
        }

        if (ZixieContext.getAPPUsedTimes() < mAppUseTimeThreshold) {
            return false
        }

        if (ZixieContext.getAPPUsedDays() < mAppUseDayThreshold) {
            return false
        }

        if (ZixieContext.getVersionName() == Config.readConfig(KEY_PRAISE_VERSION, "")) {
            return false
        }

        if (System.currentTimeMillis() - Config.readConfig(KEY_PRAISE_LAST_SHOW_TIME, 0L) < TimeUnit.DAYS.toMillis(mShowInterval.toLong())) {
            return false
        }

        return true
    }

    fun showUserPraiseDialog(activity: Activity, feedbackRouter: String) {
        Config.writeConfig(KEY_PRAISE_LAST_SHOW_TIME, System.currentTimeMillis())
        val dialog = UserPraiseDialog(activity, feedbackRouter)
        dialog.setCanceledOnTouchOutside(false)
        if (mDialogContent.isNotEmpty()) {
            dialog.setContent(mDialogContent)
        }
        dialog.show()
    }

    fun launchAppStore(activity: Activity): Boolean {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val uri = Uri.parse(String.format("market://details?id=%s", activity.packageName))
        return try {
            intent.setPackage(mMarketPackageName)
            intent.data = uri
            IntentUtils.startIntent(activity, intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}