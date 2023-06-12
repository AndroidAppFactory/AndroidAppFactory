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
import com.bihe0832.android.lib.lifecycle.LifecycleHelper.getVersionInstalledTime
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.time.DateUtil
import com.bihe0832.android.lib.widget.worker.BaseWidgetWorker

open class AAFDebugWidgetWorkerSimple(context: Context, workerParams: WorkerParameters) : BaseWidgetWorker(context, workerParams) {
    override fun updateWidget(context: Context) {
        ThreadManager.getInstance().start {
            val data = """
                ${DateUtil.getCurrentDateEN()}
                ${ShowDebugClick.getDebugInfo(context)}
                """.trimIndent()
            //只能通过远程对象来设置appwidget中的控件状态
            val remoteViews = RemoteViews(context.packageName, R.layout.com_bihe0832_debug_widget_simple)
            remoteViews.setTextViewText(R.id.widget_text_title, "应用名称：" + APKUtils.getAppName(context))
            //通过远程对象修改textview
            remoteViews.setTextViewText(R.id.widget_text_version, "应用版本：" + getVersionName() + "." + getVersionCode() + " (${getVersionTag()})")
            remoteViews.setTextViewText(R.id.widget_text_install, "安装时间：" + DateUtil.getDateEN(getVersionInstalledTime()))
            remoteViews.setTextViewText(R.id.widget_text_deviceid, "设备标识：${ZixieContext.deviceId}")

            //获得所有本程序创建的appwidget
            val componentName = ComponentName(context, AAFDebugWidgetProviderSimple::class.java)
            //更新appwidget
            updateWidget(context, componentName, remoteViews)
        }
    }
}