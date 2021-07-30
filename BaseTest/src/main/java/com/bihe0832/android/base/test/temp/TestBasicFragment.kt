package com.bihe0832.android.base.test.temp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_test_basic, container, false)
    }

    fun initView() {


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

    override fun onResume() {
        super.onResume()
        initView()
    }

    companion object {
        private const val TAG = "TestCardActivity-> "
    }
}