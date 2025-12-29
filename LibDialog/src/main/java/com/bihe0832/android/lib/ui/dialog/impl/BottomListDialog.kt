package com.bihe0832.android.lib.ui.dialog.impl

import android.app.Activity
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.ui.dialog.R
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.bihe0832.android.lib.aaf.res.R as ResR

class BottomListDialog(activity: Activity) : BottomDialog(activity) {

    private val textList: ArrayList<String> = arrayListOf()
    private var itemClickListener: ((Int) -> Unit)? = null

    fun setItemList(list: List<String>) {
        this.textList.clear()
        this.textList.addAll(list)
    }

    fun setOnItemClickListener(listener: ((Int) -> Unit)) {
        this.itemClickListener = listener
    }

    override fun initView() {
        super.initView()
        shouldCanceled = true
        initItemListView()
        setOnClickBottomListener(object :
            OnDialogListener {
            override fun onPositiveClick() {
                onNegativeClick()
            }

            override fun onNegativeClick() {
                itemClickListener?.invoke(-1)
            }

            override fun onCancel() {
                onNegativeClick()
            }
        })
    }

    protected open fun initItemListView() {
        val layout_List = findViewById<LinearLayout>(R.id.layout_List)
        layout_List.removeAllViews()
        val textViewLayoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val paddingValue = DisplayUtil.dip2px(context, 16f)

        val lineViewLayoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(context, 1f))
        textList.map { text ->
            TextView(context).apply {
                layoutParams = textViewLayoutParams
                gravity = Gravity.CENTER_HORIZONTAL
                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    context.resources.getDimension(ResR.dimen.com_bihe0832_dialog_button_text_size),
                )
                this.text = TextFactoryUtils.getSpannedTextByHtml(text)
                setTextColor(
                    getContext().getResources().getColorStateList(ResR.drawable.com_bihe0832_base_dialog_positive_style),
                )
                setOnClickListener {
                    itemClickListener?.invoke(textList.indexOf(text))
                }
                setPadding(0, paddingValue, 0, paddingValue)
            }.let {
                layout_List.addView(it)
            }

            View(context).apply {
                layoutParams = lineViewLayoutParams
                setBackgroundColor(context.resources.getColor(ResR.color.com_bihe0832_dialog_split))
            }.let {
                layout_List.addView(it)
            }
        }
    }

    override fun getLayoutID(): Int {
        return R.layout.com_bihe0832_common_bottom_list_dialog_layout
    }
}
