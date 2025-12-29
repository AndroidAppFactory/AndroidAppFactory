package com.bihe0832.android.framework.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.bihe0832.android.common.deprecated.R
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.theme.ThemeResourcesManager

/**
 * @author zixie code@bihe0832.com Created on 4/9/22.
 *
 * 一个空白的Fragment，用于一些便捷调试的场景
 */
open class CommonEmptyFragment : BaseFragment() {


    private var mContentString = this.toString()
    private var mContentBackgroundColor = Color.WHITE

    open override fun getLayoutID(): Int {
        return R.layout.common_fragment_empty
    }

    override fun parseBundle(bundle: Bundle, isOnCreate: Boolean) {
        super.parseBundle(bundle, isOnCreate)
        mContentString = bundle.getString(INTENT_KEY_TITLE, this.toString())
        mContentBackgroundColor = bundle.getInt(
            INTENT_KEY_COLOR,
            ThemeResourcesManager.getColor(ResR.color.windowBackground)!!
        )
    }

    protected override fun initView(view: View) {
        val fragment_content = view.findViewById<TextView>(R.id.fragment_content)
        fragment_content.setText(mContentString)
        fragment_content.setBackgroundColor(mContentBackgroundColor)
    }

    companion object {
        val INTENT_KEY_TITLE: String = "title"
        val INTENT_KEY_COLOR: String = "color"

        @JvmStatic
        fun newInstance(title: String, color: Int): CommonEmptyFragment {
            val fragment = CommonEmptyFragment()
            fragment.arguments = Bundle().apply {
                putString(INTENT_KEY_TITLE, title)
                putInt(INTENT_KEY_COLOR, color)
            }
            return fragment
        }

        @JvmStatic
        fun newInstance(title: String): CommonEmptyFragment {
            val fragment = CommonEmptyFragment()
            fragment.arguments = Bundle().apply {
                putString(INTENT_KEY_TITLE, title)
            }
            return fragment
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean, hasCreateView: Boolean) {
        super.setUserVisibleHint(isVisibleToUser, hasCreateView)
        ZLog.d(
            "CommonEmptyFragment",
            "$mContentString setUserVisibleHint isVisibleToUser:$isVisibleToUser, hasCreateView:$hasCreateView"
        )
    }
}