package com.bihe0832.android.base.debug.view.customview

import android.view.View
import android.widget.ImageView
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.ui.custom.view.slide.SlideViewLayout
import kotlinx.android.synthetic.main.fragment_test_custom_view.*


class DebugCustomViewFragment : BaseFragment() {


    override fun getLayoutID(): Int {
        return R.layout.fragment_test_custom_view
    }

    override fun initView(view: View) {
        val slideRail: SlideViewLayout = view.findViewById(R.id.slide_rail)

        val lockView: ImageView = view.findViewById(R.id.lock_btn)
        slideRail.init(lockView, object : SlideViewLayout.Callback {
            override fun onUnlock() {
                ZixieContext.showWaiting()
            }

        })


        test_basic_swipe.setLeftSwipe(true)
        tv_content.setOnClickListener {
            slideRail.reset()
        }


    }
}