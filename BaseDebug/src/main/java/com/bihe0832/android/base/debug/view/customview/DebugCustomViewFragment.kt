package com.bihe0832.android.base.debug.view.customview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.setPadding
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.media.Media
import com.bihe0832.android.lib.media.image.BitmapUtil
import com.bihe0832.android.lib.ui.custom.view.slide.SlideViewLayout
import com.bihe0832.android.lib.ui.view.ext.ViewCaptureLayout
import com.bihe0832.android.lib.utils.time.DateUtil
import kotlinx.android.synthetic.main.fragment_test_custom_view.test_basic_swipe
import kotlinx.android.synthetic.main.fragment_test_custom_view.test_button
import kotlinx.android.synthetic.main.fragment_test_custom_view.tv_content


class DebugCustomViewFragment : BaseFragment() {


    override fun getLayoutID(): Int {
        return R.layout.fragment_test_custom_view
    }

    @SuppressLint("SetTextI18n")
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

        test_button.setOnClickListener { button ->


        }
    }
}