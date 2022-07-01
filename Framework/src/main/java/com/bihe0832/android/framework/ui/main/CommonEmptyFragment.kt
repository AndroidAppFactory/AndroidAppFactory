package com.bihe0832.android.framework.ui.main

import android.os.Bundle
import android.view.View
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ui.BaseFragment
import kotlinx.android.synthetic.main.common_fragment_empty.*

/**
 * @author hardyshi code@bihe0832.com Created on 4/9/22.
 *
 * 一个空白的Fragment，用于一些便捷调试的场景
 */
open class CommonEmptyFragment : BaseFragment() {


    private var mTitleString = this.toString()

    open override fun getLayoutID(): Int {
        return R.layout.common_fragment_empty
    }

    override fun parseBundle(bundle: Bundle, isOnCreate: Boolean) {
        super.parseBundle(bundle, isOnCreate)
        mTitleString = bundle.getString(INTENT_KEY_TITLE, this.toString())
    }

    protected override fun initView(view: View) {
        fragment_content.setText(mTitleString)
    }

    companion object {
        private val INTENT_KEY_TITLE: String = "title"

        @JvmStatic
        fun newInstance(title: String): CommonEmptyFragment {
            val fragment = CommonEmptyFragment()
            fragment.arguments = Bundle().apply {
                putString(INTENT_KEY_TITLE, title)
            }
            return fragment
        }
    }
}