package com.bihe0832.android.framework.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.permission.ui.PermissionsActivity
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-07-08.
 * Description: 所有的Fragment的基类，目前暂时没有特殊逻辑
 */
open class BaseFragment : SwipeBackFragment() {

    /**
     * View 是否已经创建，有些时候处理
     */
    private var hasCreateView = false

    final override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        getCustomRootView()?.let {
            return it
        }
        return inflater.inflate(getLayoutID(), container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle: Bundle? = getArguments()
        bundle?.let {
            parseBundle(it)
        }
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (resetDensity()) {
            activity?.let {
                DisplayUtil.resetDensity(
                        it,
                        ConvertUtils.parseFloat(
                                it.resources.getString(R.string.custom_density),
                                Constants.CUSTOM_DENSITY
                        )
                )
            }
        }
        hasCreateView = true
        initView(view)
        initData()
    }

    /**
     * 仅用于简单的Fragment的setUserVisibleHint设置，
     * 对于有viewPager等特殊复杂场景的页面，需要自行完成setUserVisibleHint的设置
     * 如果自行设置 setUserVisibleHint ，不要调用 super.setUserVisibleHint
     * @param isVisibleToUser
     */
    final override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        setUserVisibleHint(isVisibleToUser, hasCreateView)
        if (isAdded) {
            for (fragment in childFragmentManager.fragments) {
                if (fragment.isAdded) {
                    fragment.userVisibleHint = isVisibleToUser
                }
            }

            if (getPermissionList().isNotEmpty()) {
                PermissionManager.checkPermission(
                        context,
                        javaClass.simpleName,
                        false,
                        getPermissionActivityClass(),
                        getPermissionResult(),
                        *getPermissionList().toTypedArray()
                )
            }
        }
    }

    /**
     * 布局layout
     * @return
     */
    protected open fun getLayoutID(): Int {
        return -1
    }

    /**
     * 解析intent 传递的参数
     * @param bundle
     */
    protected open fun parseBundle(bundle: Bundle) {

    }

    /**
     *
     * 返回自定义的根目录View
     */
    protected open fun getCustomRootView(): View? {
        return null
    }

    /**
     * view 初始化
     * @param view
     */
    protected open fun initView(view: View) {

    }

    /**
     * 数据加载
     */
    protected open fun initData() {

    }

    open fun setUserVisibleHint(isVisibleToUser: Boolean, hasCreateView: Boolean) {

    }

    open fun resetDensity(): Boolean {
        return true
    }

    open fun getPermissionList(): List<String> {
        return ArrayList()
    }

    open fun getPermissionResult(): PermissionManager.OnPermissionResult {
        return PermissionResultOfAAF(true)
    }

    open fun getPermissionActivityClass(): Class<out PermissionsActivity> {
        return PermissionsActivity::class.java
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ZLog.d("onActivityResult： $this, $requestCode, $resultCode, ${data?.data}")
        if (needDispatchActivityResult()) {
            dispatchActivityResult(requestCode, resultCode, data)
        }
    }

    fun needDispatchActivityResult(): Boolean {
        return true
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