package com.bihe0832.android.common.compose.debug.item

import android.app.Activity
import android.content.Context
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.log.DebugLogComposeActivity
import com.bihe0832.android.common.permission.AAFPermissionManager
import com.bihe0832.android.common.permission.PermissionResultOfAAF
import com.bihe0832.android.common.permission.special.PermissionsActivityWithSpecial
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.lifecycle.ActivityObserver
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.lifecycle.INSTALL_TYPE_APP_FIRST
import com.bihe0832.android.lib.lifecycle.INSTALL_TYPE_NOT_FIRST
import com.bihe0832.android.lib.lifecycle.INSTALL_TYPE_VERSION_FIRST
import com.bihe0832.android.lib.lifecycle.LifecycleHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedIntCallback
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.RadioDialog
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.time.DateUtil

fun showAPPInfo(context: Context) {
    DebugUtilsV2.showInfoWithHTML(context, "应用信息", getAPPInfo(context))
}

fun getAPPInfo(context: Context): List<String> {
    return mutableListOf<String>().apply {
        val version = if (ZixieContext.isDebug()) {
            "内测版"
        } else {
            if (ZixieContext.isOfficial()) {
                "外发版"
            } else {
                "预发布版"
            }
        }
        add("应用名称: ${APKUtils.getAppName(context)}")
        add("应用包名: ${ZixieContext.applicationContext!!.packageName}")
        add("安装时间: ${DateUtil.getDateEN(LifecycleHelper.getVersionInstalledTime())}")
        add("版本类型: $version")
        add("<font color ='#3AC8EF'><b>应用版本: ${ZixieContext.getVersionName()}.${ZixieContext.getVersionCode()}</b></font>")
        add("版本标识: ${ZixieContext.getVersionTag()}")
        add(
            "签名MD5: ${
                APKUtils.getSigMd5ByPkgName(
                    ZixieContext.applicationContext,
                    ZixieContext.applicationContext?.packageName,
                )
            }",
        )
    }
}

fun showUsedInfo(context: Context) {
    DebugUtilsV2.showInfo(context, "应用使用情况", getUsedInfo(context))
}

fun getUsedInfo(context: Context): List<String> {
    return mutableListOf<String>().apply {
        add("应用安装时间: ${DateUtil.getDateEN(LifecycleHelper.getAPPInstalledTime())}")
        add("应用安装时间: ${DateUtil.getDateEN(LifecycleHelper.getAPPInstalledTime())}")
        add("当前版本安装时间: ${DateUtil.getDateEN(LifecycleHelper.getVersionInstalledTime())}")

        add("上次启动版本号: ${LifecycleHelper.getAPPLastVersion()}")
        add("上次启动时间: ${DateUtil.getDateEN(LifecycleHelper.getAPPLastStartTime())}")

        add(
            "本次启动类型: ${
                LifecycleHelper.isFirstStart.let {
                    when (it) {
                        INSTALL_TYPE_NOT_FIRST -> "非首次启动"
                        INSTALL_TYPE_VERSION_FIRST -> "版本首次启动"
                        INSTALL_TYPE_APP_FIRST -> "应用首次启动"
                        else -> "类型错误（$it）"
                    }
                }
            }",
        )
        add("本次启动时间: ${DateUtil.getDateEN(ApplicationObserver.getAPPStartTime())}")
        add("累积使用天数: ${LifecycleHelper.getAPPUsedDays()}")
        add("累积使用次数: ${LifecycleHelper.getAPPUsedTimes()}")
        add("当前版本使用次数: ${LifecycleHelper.getCurrentVersionUsedTimes()}")

        add("最后一次退后台: ${DateUtil.getDateEN(ApplicationObserver.getLastPauseTime())}")
        add("最后一次回前台: ${DateUtil.getDateEN(ApplicationObserver.getLastResumedTime())}")

        add("当前页面: ${ActivityObserver.getCurrentActivity()?.javaClass?.name}")
    }
}

fun showOtherAPPInfo(context: Context) {
    val builder = StringBuilder()
    addPackageInfo(context, "com.tencent.mobileqq", builder)
    addPackageInfo(context, "com.tencent.mm", builder)
    addPackageInfo(context, "com.tencent.qqlite", builder)
    addPackageInfo(context, "com.tencent.mobileqqi", builder)
    addPackageInfo(context, "com.tencent.tim", builder)
    DebugUtilsV2.showInfo(context, "第三方应用信息", builder.toString())
}

fun addPackageInfo(context: Context, packageName: String, builder: StringBuilder) {
    val info = APKUtils.getInstalledPackage(context, packageName)
    builder.append("\n$packageName: ")
    if (null == info) {
        builder.append("未安装")
    } else {
        builder.append("\n\tname: ${APKUtils.getAppName(context, packageName)}\n")
        builder.append("	versionName: ${info.versionName}\n")
        builder.append("	versionCode: ${info.versionCode}\n")
    }
}

fun showLog(context: Context) {
    DebugUtilsV2.startActivityWithException(context, DebugLogComposeActivity::class.java)
}

fun requestPermissionForDebug(
    activity: Activity,
    permissionList: List<String>,
    permissionResult: PermissionManager.OnPermissionResult? = null,
) {

    val scene = "AAFDebug"
    val groupID = permissionList.first()

    PermissionManager.addPermissionGroup(scene, groupID, permissionList)
    PermissionManager.addPermissionGroupDesc(scene, groupID, "调试权限")
    PermissionManager.addPermissionGroupContent(
        scene,
        groupID,
        "这是一个用于调试过程中的临时权限申请，请同意授权方便开发调试",
    )

    AAFPermissionManager.checkSpecialPermission(
        activity,
        scene,
        true,
        mutableListOf(groupID),
        PermissionsActivityWithSpecial::class.java,
        object : PermissionResultOfAAF(false) {
            override fun onSuccess() {
                super.onSuccess()
                ZixieContext.showToast("授权成功")
                permissionResult?.onSuccess()
            }

            override fun onUserCancel(
                scene: String,
                permissionGroupID: String,
                permission: String
            ) {
                super.onUserCancel(scene, permissionGroupID, permission)
                ZLog.d("放弃授权")
                permissionResult?.onUserCancel(scene, permissionGroupID, permission)
            }

            override fun onUserDeny(
                scene: String,
                permissionGroupID: String,
                permission: String
            ) {
                super.onUserDeny(scene, permissionGroupID, permission)
                ZLog.d("拒绝授权")
                permissionResult?.onUserDeny(scene, permissionGroupID, permission)
            }

            override fun onFailed(msg: String) {
                super.onFailed(msg)
                ZLog.d("授权失败")
                permissionResult?.onFailed(msg)
            }
        },
    )
}

fun getChangeEnvSelectDialog(
    activity: Activity,
    title: String,
    data: List<String>,
    index: Int,
    ins: DialogCompletedIntCallback
): RadioDialog {
    RadioDialog(activity).apply {
        setTitle("${title}切换")
        setHtmlContent("点击下方列表选择将 <font color='#38ADFF'> $title </font> 切换为：")
        setRadioData(data, index, null)
        setPositive("确定")
        setNegative("取消")
        setShouldCanceled(true)
        setOnClickBottomListener(object :
            OnDialogListener {
            override fun onPositiveClick() {
                dismiss()
                try {
                    ins.onResult(checkedIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onNegativeClick() {
                dismiss()

            }

            override fun onCancel() {
                dismiss()
            }
        })
    }.let {
        return it
    }
}

fun showChangeEnvResult(
    activity: Activity,
    title: String,
    key: String,
    value: String,
    actionType: Int
) {
    showChangeEnvResult(activity, title, key, value, value, actionType)
}

fun showChangeEnvResult(
    activity: Activity,
    title: String,
    key: String,
    value: String,
    tipsText: String,
    actionType: Int
) {
    try {
        val setResultForServer = Config.writeConfig(key, value)
        if (setResultForServer) {
            showChangeEnvDialog(activity, title, tipsText, actionType)
        } else {
            ToastUtil.showShort(activity, "${title}切换失败，请重试")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


const val CHANGE_ENV_EXIST_TYPE_NOTHING = 0
const val CHANGE_ENV_EXIST_TYPE_EXIST = 1
const val CHANGE_ENV_EXIST_TYPE_RESTART = 2

fun showChangeEnvDialog(activity: Activity, title: String, tipsText: String, actionType: Int) {
    try {
        val tips =
            "${title}已切换为：<BR> <font color=\"#c0392b\">$tipsText</font> <BR> 点击确认后" + when (actionType) {
                CHANGE_ENV_EXIST_TYPE_EXIST -> "APP会自动退出，手动启动APP后生效"
                CHANGE_ENV_EXIST_TYPE_RESTART -> "APP会自动重启，APP重启后生效。<font color=\"#EC4C40\">重启过程会偶现白屏，请耐心等待</font>"
                else -> "生效"
            }

        DialogUtils.showConfirmDialog(
            activity,
            "${title}切换",
            tips,
            canCancel = false,
            object :
                OnDialogListener {
                fun clickAction(actionType: Int) {
                    when (actionType) {
                        CHANGE_ENV_EXIST_TYPE_EXIST -> ZixieContext.exitAPP()
                        CHANGE_ENV_EXIST_TYPE_RESTART -> ZixieContext.restartApp(0L)
                    }
                }

                override fun onPositiveClick() {
                    clickAction(actionType)
                }

                override fun onNegativeClick() {
                    clickAction(actionType)
                }

                override fun onCancel() {
                    clickAction(actionType)
                }
            },
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
