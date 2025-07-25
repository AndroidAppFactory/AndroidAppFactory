package com.bihe0832.android.common.compose.common.fragment

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.R
import com.bihe0832.android.common.compose.base.BaseComposeFragment
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.ui.EmptyView
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import java.util.Locale

/**
 * @author zixie code@bihe0832.com Created on 4/9/22.
 *
 * 一个空白的Fragment，用于一些便捷调试的场景
 */
open class CommonComposeEmptyFragment : BaseComposeFragment() {

    private var mContentString = this.toString()
    private var mContentBackgroundColor = Color.White

    @Preview
    @Composable
    open fun FragmentContentRenderPreview() {
        getContentRender().Content()
    }

    @Composable
    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                EmptyView(mContentString, mContentBackgroundColor)
            }
        }
    }

    override fun parseBundle(bundle: Bundle, isOnCreate: Boolean) {
        super.parseBundle(bundle, isOnCreate)
        mContentString = bundle.getString(INTENT_KEY_TITLE, this.toString())
        mContentBackgroundColor = Color(
            bundle.getInt(
                INTENT_KEY_COLOR, ThemeResourcesManager.getColor(R.color.windowBackground)!!
            )
        )
    }

    companion object {

        private val INTENT_KEY_TITLE: String = "title"
        private val INTENT_KEY_COLOR: String = "color"

        @JvmStatic
        fun newInstance(title: String, color: Int): CommonComposeEmptyFragment {
            val fragment = CommonComposeEmptyFragment()
            fragment.arguments = Bundle().apply {
                putString(INTENT_KEY_TITLE, title)
                putInt(INTENT_KEY_COLOR, color)
            }
            return fragment
        }

        @JvmStatic
        fun newInstance(title: String): CommonComposeEmptyFragment {
            val fragment = CommonComposeEmptyFragment()
            fragment.arguments = Bundle().apply {
                putString(INTENT_KEY_TITLE, title)
            }
            return fragment
        }
    }
}