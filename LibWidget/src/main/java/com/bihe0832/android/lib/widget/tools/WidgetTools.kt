package com.bihe0832.android.lib.widget.tools


import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.fragment.app.Fragment
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.ManufacturerUtil
import com.bihe0832.android.lib.widget.BaseWidgetProvider
import com.bihe0832.android.lib.widget.R
import com.bihe0832.android.lib.widget.permission.ShortcutPermission


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/6/15.
 * Description: Description
 *
 */

object WidgetTools {

    private fun getPickWidgetIntent(context: Context, code: Int): Intent {
        val mAppWidgetHost = AppWidgetHost(context, code)
        val appWidgetId = mAppWidgetHost.allocateAppWidgetId()
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        return intent;
    }

    fun pickWidget(activity: Activity, code: Int) {
        val intent = getPickWidgetIntent(activity, code)
        activity.startActivityForResult(intent, code)
    }

    fun pickWidget(fragment: Fragment, code: Int) {
        val intent = getPickWidgetIntent(fragment.requireContext(), code)
        fragment.startActivityForResult(intent, code)
    }

    fun pickWidget(context: Context) {
        pickWidget(
            context,
            context.getString(R.string.widget_add_dialog_title),
            "<font color ='" + context.resources.getColor(R.color.colorAccent) + "'>" + context.resources.getString(
                R.string.widget_add_dialog_desc
            ),
            context.getString(R.string.dialog_button_close),
            ""
        )
    }

    fun hasAddWidget(context: Context): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetProviderInfos = appWidgetManager.installedProviders
        for (appWidgetProviderInfo in appWidgetProviderInfos) {
            // 检查小部件是否在主屏幕或其他小部件主屏幕上
            if (appWidgetProviderInfo.provider.packageName == context.packageName) {
                // 小部件已经被添加
                return true
            }
        }
        return false
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

    fun pickWidget(
        context: Context,
        title: String,
        content: String,
        positiveDesc: String,
        negativeString: String
    ) {
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

    fun getAppWidgetId(data: Intent?): Int {
        return data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
    }

    fun hasAddWidgetPermission(context: Context): Boolean {
        if (ManufacturerUtil.isMiRom) {
            return ShortcutPermission.hasPermission(context)
        }
        return true
    }

    fun addWidgetToHome(context: Context, classT: Class<out BaseWidgetProvider>?) {
        classT?.let {
            val componentName = ComponentName(context, it)
            addWidgetToHome(context, componentName, -1)
        }
    }

    fun addWidgetToHome(context: Context, appWidgetId: Int) {
        addWidgetToHome(context, null, appWidgetId)
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