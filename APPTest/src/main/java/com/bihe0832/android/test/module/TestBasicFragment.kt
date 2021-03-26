package com.bihe0832.android.test.module

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.framework.update.UpdateHelper
import com.bihe0832.android.lib.download.wrapper.DownloadAPK
import com.bihe0832.android.lib.immersion.enableActivityImmersive
import com.bihe0832.android.lib.immersion.hideBottomUIMenu
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
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
            UpdateHelper.showUpdate(
                    activity!!,
                    "1",
                    "fsdfsdfsd",
                    "fsdfsdfsd",
                    "https://imtt.dd.qq.com/16891/apk/D1A7AE1C0B980EB66278E14008C9A6FF.apk",
                    "",
                    UpdateDataFromCloud.UPDATE_TYPE_NEED)
        }

        test_basic_button_local_1.setOnClickListener {
            UpdateHelper.showUpdate(
                    activity!!,
                    "2",
                    "fsdfsdfsd",
                    "fsdfsdfsd",
                    "https://imtt.dd.qq.com/16891/apk/D1A7AE1C0B980EB66278E14008C9A6FF.apk",
                    "",
                    UpdateDataFromCloud.UPDATE_TYPE_NEED)
//            (activity!! as AppCompatActivity).apply {
//                enableActivityImmersive(
//                        ContextCompat.getColor(this, R.color.process_color),
//                        ContextCompat.getColor(this, R.color.dialog_button))
//
//            }
//            test_basic_content.loadImage("https://i.17173cdn.com/9ih5jd/YWxqaGBf/forum/201810/18/193421zmhdvfx9plm1mdhx.png")
        }

        test_basic_button_local_2.setOnClickListener {
            (activity!! as AppCompatActivity).apply {
                enableActivityImmersive(
                        ContextCompat.getColor(this, R.color.result_point_color),
                        ContextCompat.getColor(this, R.color.result_point_color))

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