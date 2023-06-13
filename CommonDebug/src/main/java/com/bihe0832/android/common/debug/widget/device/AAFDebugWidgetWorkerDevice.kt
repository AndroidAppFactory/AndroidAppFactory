package com.bihe0832.android.common.debug.widget.device

import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.work.WorkerParameters
import com.bihe0832.android.common.debug.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.debug.ShowDebugClick
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.ManufacturerUtil
import com.bihe0832.android.lib.utils.time.DateUtil
import com.bihe0832.android.lib.widget.worker.BaseWidgetWorker

open class AAFDebugWidgetWorkerDevice(context: Context, workerParams: WorkerParameters) : BaseWidgetWorker(context, workerParams) {
    override fun updateWidget(context: Context) {
        ThreadManager.getInstance().start {
            //只能通过远程对象来设置appwidget中的控件状态
            val remoteViews = RemoteViews(context.packageName, R.layout.com_bihe0832_debug_widget_device)
            remoteViews.setTextViewText(R.id.widget_text_deviceid, "设备标识：${ZixieContext.deviceId}")
            remoteViews.setTextViewText(R.id.widget_text_manufacturer, "厂商型号：${ManufacturerUtil.MANUFACTURER}, ${ManufacturerUtil.MODEL}, ${ManufacturerUtil.BRAND}")
            var sdkVersion = "系统版本：Android ${BuildUtils.RELEASE}, API  ${BuildUtils.SDK_INT}" + if (ManufacturerUtil.isHarmonyOs()) {
                ", Harmony(${ManufacturerUtil.getHarmonyVersion()})"
            } else {
                ""
            }
            remoteViews.setTextViewText(R.id.widget_text_os, sdkVersion)
            //获得所有本程序创建的appwidget
            val componentName = ComponentName(context, AAFDebugWidgetProviderDevice::class.java)
            //更新appwidget
            updateWidget(context, componentName, remoteViews)
        }
    }
}