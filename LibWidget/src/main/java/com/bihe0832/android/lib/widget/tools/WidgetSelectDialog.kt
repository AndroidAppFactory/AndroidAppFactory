package com.bihe0832.android.lib.widget.tools

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.bihe0832.android.lib.widget.R

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/6/15.
 * Description: Description
 */
class WidgetSelectDialog : CommonDialog {

    private var mItemLayout: LinearLayout? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, themeResId: Int) : super(context, themeResId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(-1, -2)
        val margin = DisplayUtil.dip2px(this.context, 16.0f)
        layoutParams.setMargins(0, margin / 2, 0, margin / 2)
        this.mItemLayout = LinearLayout(this.context).apply {
            this.orientation = LinearLayout.VERTICAL
            this.setLayoutParams(layoutParams)
        }
        addViewToContent(this.mItemLayout)
        initView()
    }

    fun initView() {
        val mAppWidgetManager = AppWidgetManager.getInstance(context)
        val widgetProviders: List<AppWidgetProviderInfo> = mAppWidgetManager.getInstalledProviders()
        this.mItemLayout?.removeAllViews()
        for (widgetProvider in widgetProviders.reversed()) {
            try {
                if (widgetProvider.provider.packageName.equals(context!!.packageName)) {
                    this.mItemLayout?.addView(getView(widgetProvider))
                    ZLog.d("Widget:$widgetProvider")
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getView(widgetProvider: AppWidgetProviderInfo): View {
        LayoutInflater.from(context).inflate(R.layout.com_bihe0832_widget_select_item, null, false).apply {
            findViewById<ImageView>(R.id.widget_icon).setImageResource(widgetProvider.previewImage)
            findViewById<TextView>(R.id.widget_title).setText(widgetProvider.loadLabel(context!!.packageManager))
            setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {
                    WidgetTools.addWidgetToHome(context, widgetProvider.provider, -1)
                    return true
                }
            })
        }.let {
            return it
        }
    }
}