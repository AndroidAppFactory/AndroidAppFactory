package com.bihe0832.android.framework.ui

import android.os.Bundle
import com.bihe0832.android.lib.permission.PermissionManager
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-07-08.
 * Description: 所有的Fragment的基类，目前暂时没有特殊逻辑
 */
open class BaseFragment : SwipeBackFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isAdded) {
            for (fragment in childFragmentManager.fragments) {
                if (fragment.isAdded) {
                    fragment.userVisibleHint = isVisibleToUser
                }
            }

            if (getPermissionList().isNotEmpty()) {
                PermissionManager.checkPermission(context, false, getPermissionResult(), *getPermissionList().toTypedArray())
            }
        }
    }

    open fun getPermissionList(): List<String> {
        return ArrayList()
    }

    open fun getPermissionResult(): PermissionManager.OnPermissionResult {
        return PermissionResultOfAAF()
    }
}