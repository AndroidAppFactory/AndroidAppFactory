package com.bihe0832.android.common.compose.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.bihe0832.android.common.compose.state.MultiLanguageState
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import java.util.Locale

abstract class BaseComposeFragment : Fragment() {

    abstract fun getContentRender(): RenderState

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            try {
                parseBundle(it, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    final override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                getContentRender().Content()
            }
        }
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (resetDensity()) {
            activity?.let {
                DisplayUtil.resetDensity(
                    it, ConvertUtils.parseFloat(
                        ThemeResourcesManager.getString(
                            R.string.custom_density
                        ), Constants.CUSTOM_DENSITY
                    )
                )
            }
        }
        initView(view)
        initData()
    }

    open fun initView(view: View) {

    }


    /**
     * 解析intent 传递的参数，一些预加载的逻辑也可以在这里提前处理
     * @param bundle
     */
    protected open fun parseBundle(bundle: Bundle, isOnCreate: Boolean) {

    }

    /**
     * 数据加载，此时View已经准备好，如果有预加载就放在initView
     */
    protected open fun initData() {

    }


    open fun resetDensity(): Boolean {
        return true
    }

    /**
     * 需要外部主动调用触发，不会自动触发
     */
    open fun onLocaleChanged(lastLocale: Locale, toLanguageTag: Locale) {

    }

}
