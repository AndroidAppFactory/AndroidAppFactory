package com.bihe0832.android.lib.widget.tools


import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.widget.BaseWidgetProvider
import com.bihe0832.android.lib.widget.R


/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2023/6/15.
 * Description: Description
 *
 */

object WidgetTools {

    fun pickWidget(activity: Activity, code: Int) {
        val mAppWidgetHost = AppWidgetHost(activity, code)
        val appWidgetId = mAppWidgetHost.allocateAppWidgetId()
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        activity.startActivityForResult(intent, code)
    }

    fun pickWidget(context: Context) {
        pickWidget(context, "小组件快捷添加", "<font color ='" + context.resources.getColor(R.color.colorAccent) + "'><b>长按组件信息</b></font>可快速添加小组件到手机", "关闭", "")
    }

    fun hasAddWidget(context: Context, classT: Class<out BaseWidgetProvider>): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, classT))
        for (widgetId in appWidgetIds) {
            val info = appWidgetManager.getAppWidgetInfo(widgetId)
            // 检查小部件是否在主屏幕或其他小部件主屏幕上
            if (info != null && info.provider.packageName == context.packageName) {
                // 小部件已经被添加
                return true
            }
        }
        return false
    }

    fun pickWidget(context: Context, title: String, content: String, positiveDesc: String, negativeString: String) {
        WidgetSelectDialog(context).apply {
            setTitle(title)
            setHtmlContent(content)
            setPositive(positiveDesc)
            setNegative(negativeString)
            setShouldCanceled(true)
            setOnClickBottomListener(object : OnDialogListener {
                override fun onPositiveClick() {
                    dismiss()
                }

                override fun onNegativeClick() {
                    onPositiveClick()
                }

                override fun onCancel() {
                    onPositiveClick()
                }
            })
        }.let {
            it.show()
        }
    }

    fun onActivityResult(context: Context, data: Intent?) {
        val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        addWidgetToHome(context, null, appWidgetId)
    }

    fun addWidgetToHome(context: Context, classT: Class<out BaseWidgetProvider>?) {
        classT?.let {
            val componentName = ComponentName(context, it)
            addWidgetToHome(context, componentName, -1)
        }
    }

    fun addWidgetToHome(context: Context, componentName: ComponentName?, appWidgetId: Int) {
        try {
            var temp = componentName
            val mAppWidgetManager = AppWidgetManager.getInstance(context.applicationContext);
            if (temp == null) {
                mAppWidgetManager.getAppWidgetInfo(appWidgetId)?.let { appWidgetInfo ->
                    temp = ComponentName(context, appWidgetInfo.provider.className)
                }
            }
            temp?.let {
                val pinnedWidget = Intent()
                pinnedWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, componentName)
                if (BuildUtils.SDK_INT >= Build.VERSION_CODES.O) {
                    mAppWidgetManager.requestPinAppWidget(it, null, null)
                }
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }
}