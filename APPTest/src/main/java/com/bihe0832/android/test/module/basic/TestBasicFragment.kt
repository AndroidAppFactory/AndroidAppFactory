package com.bihe0832.android.test.module.basic

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.immersion.enableActivityImmersive
import com.bihe0832.android.lib.immersion.hideBottomUIMenu
import com.bihe0832.android.test.R
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
//            test_basic_content.loadFitCenterImage("http://img.netbian.com/file/2019/0905/8520ae74b84b193f00fd16890778a4bc.jpg")
            (activity!! as AppCompatActivity).apply {
                hideBottomUIMenu()
            }
        }

        test_basic_button_local_1.setOnClickListener {
            (activity!! as AppCompatActivity).apply {
                enableActivityImmersive(
                        ContextCompat.getColor(this, R.color.process_color),
                        ContextCompat.getColor(this, R.color.dialog_button), false)

            }
//            test_basic_content.loadImage("https://i.17173cdn.com/9ih5jd/YWxqaGBf/forum/201810/18/193421zmhdvfx9plm1mdhx.png")
        }

        test_basic_button_local_2.setOnClickListener {
            (activity!! as AppCompatActivity).apply {
                enableActivityImmersive(
                        ContextCompat.getColor(this, R.color.lib_refresh_spinkit_color),
                        ContextCompat.getColor(this, R.color.result_point_color), true)

            }
//            test_basic_content.loadImage("https://i.17173cdn.com/9ih5jd/YWxqaGBf/forum/201810/18/193421zmhdvfx9plm1mdhx.png", false)
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