package com.bihe0832.android.common.compose.debug.common

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import androidx.compose.runtime.Composable
import com.bihe0832.android.common.compose.debug.DebugBaseComposeActivity
import com.bihe0832.android.common.compose.debug.DebugComposeItemManager
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.os.BuildUtils

/**
 * @author zixie code@bihe0832.com Created on 2025/2/17.
 * Description: 仅支持全Compose 页面，且页面对应的Compose UI 通过Key 获取
 */
open class DebugComposeRootActivity : DebugBaseComposeActivity() {

    private val TAG = "DebugComposeRootActivit"
    private var rootFragmentClassName = ""
    private var rootFragmentTitleName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildUtils.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        }
        rootFragmentClassName = intent.getStringExtra(DebugUtilsV2.DEBUG_MODULE_CLASS_NAME)!!
        rootFragmentTitleName = intent.getStringExtra(DebugUtilsV2.DEBUG_MODULE_TITLE_NAME)!!
        ZLog.d(TAG, "rootFragmentClassName: $rootFragmentClassName")
        ZLog.d(TAG, "rootFragmentTitleName: $rootFragmentTitleName")
    }

    @Composable
    override fun getTitleName(): String {
        return rootFragmentTitleName
    }

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                DebugComposeItemManager.GetDebugComposeItem(rootFragmentClassName)
            }
        }
    }
}
