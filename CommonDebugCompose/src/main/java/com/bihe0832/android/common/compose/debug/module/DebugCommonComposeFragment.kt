package com.bihe0832.android.common.compose.debug.module

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity.RESULT_OK
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bihe0832.android.common.compose.common.fragment.CommonComposeFragment
import com.bihe0832.android.common.compose.debug.DebugUtils
import com.bihe0832.android.common.compose.debug.log.DebugLogComposeActivity
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.select.FileSelectTools

/**
 * @author zixie code@bihe0832.com Created on 2025/2/17.
 * Description: 仅支持全Compose 页面，且页面对应的Compose UI 通过Key 获取
 */
open class DebugCommonComposeFragment : CommonComposeFragment() {

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                val context = LocalContext.current
                GetDebugCommonModuleView { showLog(context) }
            }
        }
    }

    open fun showLog(context: Context) {
        DebugUtils.startActivityWithException(context, DebugLogComposeActivity::class.java)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FileSelectTools.FILE_CHOOSER && resultCode == RESULT_OK) {
            data?.extras?.getString(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, "")?.let { filePath ->
                FileUtils.sendFile(context!!, filePath).let {
                    if (!it) {
                        ZixieContext.showToast("分享文件:$filePath 失败")
                    }
                }
            }
        }
    }
}
