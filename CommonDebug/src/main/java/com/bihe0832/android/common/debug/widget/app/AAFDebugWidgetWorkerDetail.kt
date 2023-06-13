package com.bihe0832.android.common.debug.widget.app

import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.work.WorkerParameters
import com.bihe0832.android.common.debug.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ZixieContext.getVersionCode
import com.bihe0832.android.framework.ZixieContext.getVersionName
import com.bihe0832.android.framework.ZixieContext.getVersionTag
import com.bihe0832.android.framework.debug.ShowDebugClick
import com.bihe0832.android.framework.router.RouterAction.getFinalURL
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.lifecycle.LifecycleHelper.getVersionInstalledTime
import com.bihe0832.android.lib.notification.NotifyManager.getPendingIntent
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.ManufacturerUtil
import com.bihe0832.android.lib.utils.time.DateUtil
import com.bihe0832.android.lib.widget.worker.BaseWidgetWorker

open class AAFDebugWidgetWorkerDetail(context: Context, workerParams: WorkerParameters) : BaseWidgetWorker(context, workerParams) {
    override fun updateWidget(context: Context) {
        ThreadManager.getInstance().start {
            val data = """
                ${DateUtil.getCurrentDateEN()}
                ${ShowDebugClick.getDebugInfo(context)}
                """.trimIndent()
            //只能通过远程对象来设置appwidget中的控件状态
            val remoteViews = RemoteViews(context.packageName, R.layout.com_bihe0832_debug_widget_detail)
            remoteViews.setOnClickPendingIntent(R.id.widget_start_debug, getPendingIntent(context, getFinalURL(RouterConstants.MODULE_NAME_DEBUG)))
            remoteViews.setOnClickPendingIntent(R.id.widget_start_app, getPendingIntent(context, getFinalURL(RouterConstants.MODULE_NAME_SPLASH)))

            //通过远程对象修改textview
            remoteViews.setTextViewText(R.id.widget_text_title, "提取时间：" + DateUtil.getCurrentDateEN())
            remoteViews.setTextViewText(R.id.widget_text_version, "应用版本：" + getVersionName() + "." + getVersionCode())
            remoteViews.setTextViewText(R.id.widget_text_install, "安装时间：" + DateUtil.getDateEN(getVersionInstalledTime()))
            remoteViews.setTextViewText(R.id.widget_text_deviceid, "设备标识：${ZixieContext.deviceId}")
            remoteViews.setTextViewText(R.id.widget_text_tag, "版本标识：" + getVersionTag())
            remoteViews.setTextViewText(R.id.widget_text_manufacturer, "厂商型号：${ManufacturerUtil.MANUFACTURER}, ${ManufacturerUtil.MODEL}")
            var sdkVersion = "系统版本：${BuildUtils.RELEASE}, API  ${BuildUtils.SDK_INT}" + if (ManufacturerUtil.isHarmonyOs()) {
                ", Harmony(${ManufacturerUtil.getHarmonyVersion()})"
            } else {
                ""
            }
            remoteViews.setTextViewText(R.id.widget_text_os, sdkVersion)
            remoteViews.setTextViewText(R.id.widget_text_extra, "其他信息：${getExtInfo()}")

            //获得所有本程序创建的appwidget
            val componentName = ComponentName(context, AAFDebugWidgetProviderDetail::class.java)
            //更新appwidget
            updateWidget(context, componentName, remoteViews)
        }
    }

    open fun getExtInfo(): String {
        return "暂无"
    }
}