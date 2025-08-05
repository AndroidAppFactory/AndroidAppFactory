package com.bihe0832.android.common.compose.debug.common

import android.os.Bundle
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import com.bihe0832.android.common.compose.common.activity.CommonComposeFragmentActivity
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.framework.ZixieContext.showDebug
import com.bihe0832.android.lib.utils.ReflecterHelper

open class DebugComposeFragmentActivity : CommonComposeFragmentActivity() {

    private var rootFragmentClassName = ""
    private var rootFragmentTitleName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        rootFragmentClassName = intent.getStringExtra(DebugUtilsV2.DEBUG_MODULE_CLASS_NAME) ?: ""
        rootFragmentTitleName = intent.getStringExtra(DebugUtilsV2.DEBUG_MODULE_TITLE_NAME) ?: ""
        super.onCreate(savedInstanceState)
    }

    override fun getFragment(): Fragment? {
        val rootFragmentClassName = getRootFragmentClassName()
        try {
            if (TextUtils.isEmpty(rootFragmentClassName)) {
                showDebug("类名错误，请检查后重试")
                finish()
            }

            val rootFragmentClass = Class.forName(rootFragmentClassName)
            if (rootFragmentClass.javaClass.isAssignableFrom(Fragment::class.java.javaClass)) {
                val fragment = ReflecterHelper.newInstance(rootFragmentClassName, null) as? Fragment
                return fragment
            } else {
                showDebug(rootFragmentClassName + "不是继承 Fragment")
                finish()
            }
        } catch (e: ClassNotFoundException) {
            showDebug("没有找到$rootFragmentClassName")
            finish()
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    protected open fun getRootFragmentClassName(): String? {
        return rootFragmentClassName
    }

    @Composable
    override fun getTitleName(): String {
        if (TextUtils.isEmpty(rootFragmentTitleName)) {
            try {
                return if (getRootFragmentClassName() != null) {
                    getRootFragmentClassName()!!.substring(
                        getRootFragmentClassName()!!.lastIndexOf(
                            "."
                        ) + 1
                    )
                } else {
                    this.javaClass.simpleName
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return this.javaClass.simpleName
        }
        return rootFragmentTitleName
    }
}
