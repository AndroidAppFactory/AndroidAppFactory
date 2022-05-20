package com.bihe0832.android.base.test.temp

import android.graphics.Color
import android.view.View
import com.bihe0832.android.base.test.R
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.ui.image.loadCenterCropImage
import com.bihe0832.android.lib.ui.image.loadFitCenterImage
import com.bihe0832.android.lib.ui.image.loadImage
import com.bihe0832.android.lib.ui.image.loadRoundCropImage
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_test_basic.*

class TestBasicFragment : BaseFragment() {

    override fun getLayoutID(): Int {
        return R.layout.fragment_test_basic
    }

    override fun initView(view: View) {
        super.initView(view)


        test_basic_button.setOnClickListener {
            test_basic_content.loadFitCenterImage("http://cdn.bihe0832.com/images/cv.png")
        }

        test_basic_button_local_1.setOnClickListener {
            test_basic_content.loadCenterCropImage("http://cdn.bihe0832.com/images/cv.png")
        }

        test_basic_button_local_2.setOnClickListener {
            test_basic_content.loadImage("http://cdn.bihe0832.com/images/zixie_32.ico", true, Color.GRAY, Color.GRAY, RequestOptions().optionalCircleCrop())
        }

        test_basic_button_local_3.setOnClickListener {
            test_basic_content.loadRoundCropImage("http://cdn.bihe0832.com/images/zixie_32.ico", DisplayUtil.dip2px(context, 3f))
        }
    }

    companion object {
        private const val TAG = "TestCardActivity-> "
    }
}