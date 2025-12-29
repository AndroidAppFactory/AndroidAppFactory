package com.bihe0832.android.framework.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import java.util.Locale

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-07-08.
 * Description: 所有的Fragment的基类，目前暂时没有特殊逻辑
 */
open class BaseFragment : SwipeBackFragment() {

    /**
     * View 是否已经创建，有些时候处理
     */
    private var hasCreateView = false

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getLayoutID().let {
            if (it > 0) {
                return inflater.inflate(getLayoutID(), container, false)
            } else {
                getCustomRootView()?.let { view ->
                    return view
                }

                getCustomRootView(inflater, container, savedInstanceState)?.let { view ->
                    return view
                }

                return null
            }
        }
    }

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


    override fun onDestroyView() {
        super.onDestroyView()
        hasCreateView = false
    }

    final override fun onNewBundle(args: Bundle?) {
        super.onNewBundle(args)
        args?.let {
            try {
                parseBundle(it, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (resetDensity()) {
            activity?.let {
                DisplayUtil.resetDensity(it, ConvertUtils.parseFloat(ThemeResourcesManager.getString(
                    ResR.string.custom_density), Constants.CUSTOM_DENSITY))
            }
        }
        hasCreateView = true
        initView(view)
        initData()
    }

    fun isRootViewCreated(): Boolean {
        return hasCreateView
    }

    /**
     * 对于Activity的根Fragment, 建议在 Activity的 onResume 里手动调用根Fragment的 setUserVisibleHint
     * 对于有viewPager等特殊复杂场景的页面，需要自行完成setUserVisibleHint的设置
     * 如果自行设置 setUserVisibleHint ，不要调用 super.setUserVisibleHint
     *
     * 具体示例可以参考：CommonMainFragment
     * @param isVisibleToUser
     */
    final override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        try {
            setUserVisibleHint(isVisibleToUser, hasCreateView)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 前后台切换
     *
     * @param isVisibleToUser 当前是前台或者后台
     * @param hasCreateView 当前View 是否已经创建好
     */
    open fun setUserVisibleHint(isVisibleToUser: Boolean, hasCreateView: Boolean) {
        ZLog.d("setUserVisibleHint","${this.javaClass.simpleName} ,isVisibleToUser:$isVisibleToUser ,hasCreateView:$hasCreateView")
    }

    fun setChildUserVisibleHint(isVisibleToUser: Boolean) {
        if (isAdded) {
            for (fragment in childFragmentManager.fragments) {
                if (fragment.isAdded) {
                    fragment.userVisibleHint = isVisibleToUser
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ZLog.d("onActivityResult： $this, $requestCode, $resultCode, ${data?.data}")
    }


    /**
     * 布局layout
     * @return
     */
    protected open fun getLayoutID(): Int {
        return -1
    }

    /**
     *
     * 返回自定义的根目录View
     */
    protected open fun getCustomRootView(): View? {
        return null
    }

    protected open fun getCustomRootView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return null
    }

    /**
     * 解析intent 传递的参数，一些预加载的逻辑也可以在这里提前处理
     * @param bundle
     */
    protected open fun parseBundle(bundle: Bundle, isOnCreate: Boolean) {

    }

    /**
     * view 初始化
     * @param view
     */
    protected open fun initView(view: View) {

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

    fun dispatchActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        ZLog.d("onActivityResult： $this, $requestCode, $resultCode, ${data?.data}")
        try {
            for (fragment in childFragmentManager.fragments) {
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}