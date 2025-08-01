package com.bihe0832.android.common.compose.debug

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import androidx.compose.runtime.Composable
import com.bihe0832.android.common.compose.common.activity.CommonComposeActivity
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.os.BuildUtils

/**
 * @author zixie code@bihe0832.com Created on 2025/2/17.
 * Description: 仅支持全Compose 页面，且页面对应的Compose UI 通过Key 获取
 */
open class DebugComposeRootActivity : CommonComposeActivity() {

    private var rootFragmentClassName = ""
    private var rootFragmentTitleName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildUtils.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        }
        rootFragmentClassName = intent.getStringExtra(DEBUG_MODULE_CLASS_NAME)!!
        rootFragmentTitleName = intent.getStringExtra(DEBUG_MODULE_TITLE_NAME)!!
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

    companion object {
        val DEBUG_MODULE_CLASS_NAME: String = "com.bihe0832.android.common.module.class.name"
        val DEBUG_MODULE_TITLE_NAME: String = "com.bihe0832.android.common.module.title.name"
        val TAG = "DebugComposeRootActivity"

        fun startComposeActivity(
            context: Context, activityClass: Class<*>, titleName: String, key: String
        ) {
            val paramData = HashMap<String, String>()
            paramData[DEBUG_MODULE_CLASS_NAME] = key
            paramData[DEBUG_MODULE_TITLE_NAME] = titleName
            startActivityWithException(context, activityClass, paramData)
        }

        fun startComposeActivity(context: Context, titleName: String, key: String) {
            startComposeActivity(context, DebugComposeRootActivity::class.java, titleName, key)
        }

        fun startActivityWithException(
            context: Context?, cls: Class<*>, data: Map<String, String>?
        ) {
            val intent = Intent(context, cls)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data?.let {
                for ((key, value) in it) {
                    intent.putExtra(key, value)
                }
            }
            context?.startActivity(intent)
        }
    }


}
