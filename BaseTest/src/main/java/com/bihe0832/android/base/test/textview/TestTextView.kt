package com.bihe0832.android.base.test.textview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bihe0832.android.base.test.R
import com.bihe0832.android.framework.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_test_text.*

class TestTextView : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_test_text, container, false)
    }

    var testList = mutableListOf<String>(
            "这是一个测试测试0",
            "这是一个测试测试这是一个测试测试这是一个测试1",
            "这是一个测试测试这是一个测试测试这是一个测试测试这是一",
            "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试3",
            "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试测试4",
            "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是测试5",
            "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这试测试6",
            "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试试这是这是一个测试测7",
            "这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个测试测试这是一个试测这是一个试测这是一个试测这是一个试测试这是一个测试测试8"
    )
    var index = 6
    fun initView() {


        test_basic_button.setOnClickListener {
            info_content_1.text = testList[index + 0]
            info_content_2.text = testList[index + 1]
            info_content_3.text = testList[index + 2]
            index += 3
            if (index > 7) {
                index = 0
            }
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