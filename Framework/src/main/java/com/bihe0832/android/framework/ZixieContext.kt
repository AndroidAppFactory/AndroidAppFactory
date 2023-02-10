/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/23 下午8:07
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/23 下午8:01
 *
 */

package com.bihe0832.android.framework

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.widget.Toast
import com.bihe0832.android.lib.channel.ChannelTools
import com.bihe0832.android.lib.device.DeviceIDUtils
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.lifecycle.ActivityObserver
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.lifecycle.LifecycleHelper
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.DialogUtils
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.lib.utils.os.BuildUtils
import java.io.File
import kotlin.system.exitProcess


/**
 * Created by zixie on 2017/6/27.
 *
 * 核心内容
 */
object ZixieContext {

    var screenWidth = 0
    var screenHeight = 0

    private var versionName = ""
    private var versionCode = 0L
    private var mDebug = true
    private var mOfficial = true
    private var mTag = "Tag_ZIXIE_1.0.0_1"

    private val zixieFolderPath by lazy {
        FileUtils.getFolderPathWithSeparator(ZixieFileProvider.getZixieFilePath(applicationContext!!))
    }

    @Synchronized
    fun init(app: Application, appIsDebug: Boolean, appIsOfficial: Boolean, appTag: String) {
        application = app
        applicationContext = app.applicationContext
        mDebug = appIsDebug
        mOfficial = appIsOfficial
        mTag = appTag
        initModule({ ChannelTools.init(app, "DEBUG") }, false)
    }

    var applicationContext: Context? = null
        private set

    var application: Application? = null
        private set

    fun isDebug(): Boolean {
        return mDebug
    }

    fun isOfficial(): Boolean {
        return mOfficial
    }

    fun getVersionTag(): String {
        return mTag
    }

    val channelID: String
        get() = ChannelTools.getChannel()

    val deviceId: String
        get() = DeviceIDUtils.getAndroidId(applicationContext) ?: ""

    val deviceKey: Long
        get() = ConvertUtils.getUnsignedInt(deviceId.hashCode())

    fun showDebugEditionToast() {
        if (!isOfficial()) {
            applicationContext?.let { context ->
                showLongToast(context.getString(R.string.common_tips_debug))
            }
        }
    }

    //任何时候都弹
    fun showToast(msg: String, duration: Int) {
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.R) {
            //大于等于 30
            if (ApplicationObserver.isAPPBackground()) {
                ThreadManager.getInstance().runOnUIThread {
                    Toast.makeText(applicationContext, msg, duration).show()
                }
            } else {
                ToastUtil.show(applicationContext, msg, duration)
            }
        } else {
            ToastUtil.show(applicationContext, msg, duration)
        }
    }

    //任何时候都弹
    fun showToast(msg: String) {
        showToast(msg, Toast.LENGTH_SHORT)
    }

    //任何时候都弹
    fun showLongToast(msg: String) {
        showToast(msg, Toast.LENGTH_LONG)
    }

    //仅APP在前台弹
    fun showLongToastJustAPPFront(msg: String) {
        if (!ApplicationObserver.isAPPBackground()) {
            showLongToast(msg)
        }
    }

    //仅APP在前台弹
    fun showToastJustAPPFront(msg: String) {
        if (!ApplicationObserver.isAPPBackground()) {
            showToast(msg)
        }
    }

    //仅debug弹
    fun showDebug(msg: String) {
        if (isDebug()) {
            showToast(msg)
        }
    }

    fun showWaiting() {
        applicationContext?.let { context ->
            showLongToast(context.getString(R.string.common_tips_waiting))
        }
    }

    fun getVersionNameAndCode(): String {
        return getVersionName() + "." + getVersionCode()
    }

    fun getVersionName(): String {
        if (TextUtils.isEmpty(versionName)) {
            versionName = APKUtils.getAppVersionName(applicationContext)
        }
        return versionName
    }

    fun getVersionCode(): Long {
        if (versionCode < 1) {
            versionCode = APKUtils.getAppVersionCode(applicationContext)
        }
        return versionCode
    }

    fun isFirstStart(): Int {
        return LifecycleHelper.isFirstStart
    }

    fun getAPPLastVersionInstalledTime(): Long {
        return LifecycleHelper.getVersionInstalledTime()
    }

    fun getAPPInstalledTime(): Long {
        return LifecycleHelper.getAPPInstalledTime()
    }

    fun getAPPCurrentStartTime(): Long {
        return LifecycleHelper.getAPPCurrentStartTime()
    }

    fun getAPPLastStartTime(): Long {
        return LifecycleHelper.getAPPLastStartTime()
    }

    fun getAPPUsedDays(): Int {
        return LifecycleHelper.getAPPUsedDays()
    }

    fun getAPPUsedTimes(): Int {
        return LifecycleHelper.getAPPUsedTimes()
    }


    fun getZixieFolder(): String {
        return zixieFolderPath
    }

    fun getZixieExtFolder(): String {
        var path = if (FileUtils.checkStoragePermissions(applicationContext)) {
            "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}zixie${File.separator}"
        } else {
            getZixieFolder()
        }
        return FileUtils.getFolderPathWithSeparator(path)
    }

    fun getLogFolder(): String {
        return FileUtils.getFolderPathWithSeparator(getZixieFolder() + "temp${File.separator}log" + File.separator)
    }

    fun initModule(action: () -> Unit, canInitWithBackgroundThread: Boolean) {
        try {
            if (canInitWithBackgroundThread) {
                ThreadManager.getInstance().start { action() }
            } else {
                action()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 尽量使用页面归属的Activity，在没有页面的前提下，优先使用applicationContext，只有别无选择的场景，使用该方式
     */
    fun getCurrentActivity(): Activity? {
        return ActivityObserver.getCurrentActivity()
    }

    fun exitAPP() {
        ThreadManager.getInstance().start({
            ThreadManager.getInstance().runOnUIThread {
                ActivityObserver.getCurrentActivity()?.let { context ->
                    (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)?.let {
                        it.killBackgroundProcesses(context.packageName)
                        if (BuildUtils.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                            for (appTask in it.appTasks) {
                                appTask.finishAndRemoveTask()
                            }
                            context.finishAndRemoveTask()
                        } else {
                            context.finish()
                        }
                    }
                }
                ActivityObserver.getActivityList().forEach {
                    it.finish()
                }
                ApplicationObserver.onAllActivityDestroyed()

                android.os.Process.killProcess(android.os.Process.myPid())//获取PID
                exitProcess(1)//常规java、c#的标准退出法，返回值为0代表正常退出**
            }
        }, 200L)
    }


    fun exitAPP(callbackListener: OnDialogListener?) {
        getCurrentActivity()?.let {
            DialogUtils.showConfirmDialog(
                    it,
                    applicationContext!!.resources.getString(R.string.common_reminder_title),
                    String.format(applicationContext!!.resources.getString(R.string.exist_msg), applicationContext!!.resources.getString(R.string.app_name)),
                    applicationContext!!.resources.getString(R.string.comfirm), applicationContext!!.resources.getString(R.string.cancel), true,
                    object : OnDialogListener {
                        override fun onPositiveClick() {
                            callbackListener?.onPositiveClick()
                            exitAPP()
                        }

                        override fun onNegativeClick() {
                            callbackListener?.onNegativeClick()
                        }

                        override fun onCancel() {
                            callbackListener?.onCancel()
                        }
                    })
        }
    }

    open fun restartApp() {
        restartApp(ConvertUtils.parseLong(applicationContext?.getString(R.string.common_waiting_duration_restart), 1500L))
    }

    open fun restartApp(waitTime: Long) {
        applicationContext?.let { context ->
            showLongToast(context.getString(R.string.common_tips_restart))
            ThreadManager.getInstance().start({
                IntentUtils.restartAPP(context)
                exitProcess(0)
            }, waitTime)
        }
    }
}
