/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/6 下午6:05
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/6 下午5:58
 *
 */
package com.bihe0832.android.lib.ace.editor

import android.app.Activity
import android.text.TextUtils
import android.view.View
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.ClipboardUtil
import com.bihe0832.android.lib.ui.menu.PopupList
import com.bihe0832.android.lib.aaf.res.R as ResR

class AceSelectionActionHelper(private val mContext: Activity) {

    private val OPTION_CUT = "剪切"
    private val OPTION_COPY = "复制"
    private val OPTION_SELECT_ALL = "全选"
    private val OPTION_SEARCH = "搜索"

    interface OnSelectionItemPressed {
        fun onCutClick()
        fun onCopyClick()
        fun onSelectAllClick()
        fun onSearchClick()
    }

    private var mPopup: PopupList = PopupList(mContext).apply {
        normalBackgroundColor = resources.getColor(ResR.color.com_bihe0832_dialog_bg)
        pressedBackgroundColor = resources.getColor(ResR.color.com_bihe0832_dialog_bg)
        normalTextColor = resources.getColor(ResR.color.com_bihe0832_pop_menu_text_color)
        pressedTextColor = resources.getColor(ResR.color.com_bihe0832_select_state_pressed)
        dividerColor = resources.getColor(ResR.color.com_bihe0832_dialog_split)
    }

    private var mOnSelectionItemPressedListener: OnSelectionItemPressed? = null
    private var mHasSelection = false
    private var mReadOnly = false

    fun isShowing(): Boolean {
        return mPopup != null && mPopup.isShowing
    }

    fun hasSelection(hasSelection: Boolean) {
        mHasSelection = hasSelection
    }

    fun setReadOnly(readOnly: Boolean) {
        mReadOnly = readOnly
    }

    fun setOnSelectionItemPressedListener(listener: OnSelectionItemPressed?) {
        mOnSelectionItemPressedListener = listener
    }

    fun getOptions(): MutableList<String> {
        return mutableListOf<String>().apply {
            if (mHasSelection && !mReadOnly) {
                add(OPTION_CUT)
            }
            if (mHasSelection) {
                add(OPTION_COPY)
            }
            add(OPTION_SELECT_ALL)
        }
    }


    fun dismiss() {
        mPopup.hidePopupListWindow()
    }

    fun getDefaultHeight(): Int {
        return mPopup.dividerHeight
    }

    fun show(anchor: View, rawX: Float, rawY: Float) {
        mPopup.show(anchor, rawX + 250, rawY + 100, false, getOptions()) { _, position, label ->
            when (label) {
                OPTION_CUT -> {
                    mOnSelectionItemPressedListener?.onCutClick()
                }

                OPTION_COPY -> {
                    mOnSelectionItemPressedListener?.onCopyClick()
                }

                OPTION_SELECT_ALL -> {
                    mOnSelectionItemPressedListener?.onSelectAllClick()
                }

                OPTION_SEARCH -> {
                    mOnSelectionItemPressedListener?.onSearchClick()
                }
                else -> {
                    ZLog.e("Error pop item ")
                }
            }
        }
    }
}