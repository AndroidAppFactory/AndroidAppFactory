package com.bihe0832.android.common.praise

import android.app.Activity
import android.content.Intent
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.superapp.APPMarketHelper
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.utils.ConvertUtils
import java.util.concurrent.TimeUnit

object UserPraiseManager {

    // 是否评价过
    const val KEY_PRAISE_DONE = "KEY_PRAISE_DONE"

    //上次评价的版本
    const val KEY_PRAISE_VERSION = "KEY_PRAISE_VERSION"

    // 最后一次展示时间
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
    private var mHeadTitle = ""

    init {
        //1_3_3_2_这是一个测试
        val praiseConfig = Config.readConfig(KEY_PRAISE_DONE, "")
        praiseConfig.split("_".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
            .let { temp ->
                mNeedShow = ConvertUtils.getSafeValueFromArray(temp, 0, "") == "1"
                mAppUseTimeThreshold =
                    ConvertUtils.parseInt(ConvertUtils.getSafeValueFromArray(temp, 1, "1"))
                mAppUseDayThreshold =
                    ConvertUtils.parseInt(ConvertUtils.getSafeValueFromArray(temp, 2, "1"))
                mShowInterval =
                    ConvertUtils.parseInt(ConvertUtils.getSafeValueFromArray(temp, 3, "2"))
                mHeadTitle = ConvertUtils.getSafeValueFromArray(temp, 4, "")
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

        if (ZixieContext.getVersionCode() == Config.readConfig(KEY_PRAISE_VERSION, 0L)) {
            return false
        }

        if (System.currentTimeMillis() - Config.readConfig(
                KEY_PRAISE_LAST_SHOW_TIME,
                0L
            ) < TimeUnit.DAYS.toMillis(
                mShowInterval.toLong()
            )
        ) {
            return false
        }

        return true
    }

    fun setMarketPackageName(markName: String) {
        mMarketPackageName = markName;
    }

    fun setMarketPackageName(markName: List<String>) {
        mMarketPackageName =
            APPMarketHelper.getFirstMarket(ZixieContext.applicationContext, markName)
    }

    fun launchMarket(activity: Activity): Boolean {
        return APPMarketHelper.launchMarket(activity, mMarketPackageName, activity.packageName)
    }

    fun showUserPraiseDialog(activity: Activity, feedbackRouter: String) {
        showUserPraiseDialog(activity, feedbackRouter) {
            launchMarket(activity)
        }
    }

    fun showUserPraiseDialog(
        activity: Activity,
        feedbackRouter: String,
        successAction: () -> Unit
    ) {
        Config.writeConfig(KEY_PRAISE_LAST_SHOW_TIME, System.currentTimeMillis())
        UserPraiseDialog(activity, feedbackRouter).apply {
            if (mHeadTitle.isNotEmpty()) {
                setHeadTitleContent(mHeadTitle)
            }
            onClickBottomListener = object :
                OnDialogListener {
                override fun onPositiveClick() {
                    successAction.invoke()
                    doPraiseAction()
                    dismiss()
                }

                override fun onNegativeClick() {
                    doPraiseAction()
                    RouterAction.openFinalURL(feedbackRouter, Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    dismiss()
                }

                override fun onCancel() {
                    dismiss()
                }

            }
        }.show()
    }

    private fun doPraiseAction() {
        Config.readConfig(KEY_PRAISE_DONE, 1)
        Config.readConfig(KEY_PRAISE_VERSION, ZixieContext.getVersionCode())
    }
}