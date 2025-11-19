package com.bihe0832.android.base.debug.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.view.setPadding
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.base.debug.touch.TouchRegionActivity
import com.bihe0832.android.base.debug.view.customview.DebugCustomViewFragment
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.media.Media
import com.bihe0832.android.lib.media.image.bitmap.BitmapUtil
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedStringCallback
import com.bihe0832.android.lib.ui.dialog.senddata.SendTextUtils
import com.bihe0832.android.lib.ui.view.ext.ViewCaptureLayout
import com.bihe0832.android.lib.utils.time.DateUtil

class DebugBaseViewFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugFragmentItemData("特殊TextView 调试", DebugTextViewFragment::class.java))
            add(getDebugFragmentItemData("自定义View 调试", DebugCustomViewFragment::class.java))
            add(
                getDebugItem(
                    "View 导出图片",
                    View.OnClickListener {
                        debugExport(it)
                    },
                ),
            )
            add(
                getDebugItem(
                    "TextView对HTML的支持测试",
                    View.OnClickListener {
                        showInputDialog(
                            "TextView对HTML的支持测试",
                            "请在输入框输入需要验证的文本内容，无需特殊编码",
                            "<font color='#428bca'>测试文字加粗</font> <BR> 正常的文字效果<BR> <b>测试文字加粗</b> <em>文字斜体</em> <p><font color='#428bca'>修改文字颜色</font></p>",
                        ) { result ->
                            SendTextUtils.sendInfoWithHTML(
                                context,
                                "TextView对HTML的支持测试",
                                result, "",
                                "分享给我们", true, true
                            )
                        }
                    },
                ),
            )

            add(
                getDebugItem(
                    "点击区扩大Demo",
                    View.OnClickListener {
                        startActivityWithException(TouchRegionActivity::class.java)
                    },
                ),
            )
        }
    }

    private fun debugExport(it: View?) {
        Media.addToPhotos(context, BitmapUtil.getViewBitmap(it))

        TextView(context).apply {
            text = "BitmapUtil.getViewBitmap:" + DateUtil.getCurrentDateEN()
        }.let {
            Media.addToPhotos(context, BitmapUtil.getViewBitmap(it))
        }

        TextView(context).apply {
            text = "Bitmap createBitmap:" + DateUtil.getCurrentDateEN()
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            width = 200
        }.let { view ->
            val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.setDrawingCacheEnabled(true)
            view.measure(
                View.MeasureSpec.makeMeasureSpec(canvas.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(canvas.height, View.MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            canvas.drawBitmap(view.drawingCache, 0f, 0f, Paint())
            Media.addToPhotos(context, BitmapUtil.saveBitmap(context, bitmap))
        }

        TextView(context).apply {
            text = "ViewCaptureLayout getViewBitmap:" + DateUtil.getCurrentDateEN()
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
            setTextColor(context.getColor(R.color.md_theme_tertiary))
            setPadding(40)
        }.let {
            ViewCaptureLayout(context).apply {
                addView(it, 0)
                background = context.getDrawable(R.drawable.com_bihe0832_base_dialog_bg)
            }.getViewBitmap { data ->
                Media.addToPhotos(context, BitmapUtil.saveBitmap(context, data))
            }
        }
    }
}
