package com.bihe0832.android.base.debug.view.customview

import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_test_custom_view.*
import kotlinx.android.synthetic.main.fragment_test_text.*


class DebugCustomViewFragment : BaseFragment() {


    override fun getLayoutID(): Int {
        return R.layout.fragment_test_custom_view
    }

    override fun initView(view: View) {
        test_basic_swipe.setLeftSwipe(true)
    }
}