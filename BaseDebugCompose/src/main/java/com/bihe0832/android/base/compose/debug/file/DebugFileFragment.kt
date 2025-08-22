package com.bihe0832.android.base.compose.debug.file

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.module.DebugCommonComposeFragment
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.mimetype.FILE_TYPE_ALL
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.log.ZLog
import java.io.File

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/8/22.
 * Description: Description
 *
 */
class DebugFileFragment : DebugCommonComposeFragment() {
    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                DebugContent {
                    DebugItem("文件选择") { context ->
                        (context as? Activity)?.let {
                            FileSelectTools.openFileSelect(it, ZixieContext.getZixieFolder())
                        }
                    }
                    DebugItem("系统文件选择") { context ->
                        (context as? Activity)?.let {
                            FileSelectTools.openAndroidFileSelect(it, FILE_TYPE_ALL)
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode === ZixieActivityRequestCode.FILE_CHOOSER && resultCode === Activity.RESULT_OK) {
            if (resultData != null) {
                resultData.getStringExtra(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL)?.let {
                    ZLog.d(LOG_TAG, "File : $it")
                    ZLog.d(LOG_TAG, "File Content : ${FileUtils.getFileContent(it)}")
                }
            }
        } else if (requestCode === ZixieActivityRequestCode.FILE_CHOOSER_SYSTEM && resultCode === Activity.RESULT_OK) {
            if (resultData != null) {
                resultData.getData()?.let {
                    ZLog.d(LOG_TAG, "File : $it")
                    val filePath: String = it.getPath() ?: ""
                    var tempFile =
                        AAFFileWrapper.getFileTempFolder() + FileUtils.getFileName(filePath)
                    var result = FileUtils.copyFile(context!!, it, File(tempFile))
                    ZLog.d(LOG_TAG, "File Copy : $result $tempFile")
                    tempFile = "/" + FileUtils.getFileName(filePath)
                    result = FileUtils.copyFile(context!!, it, File(tempFile))
                    ZLog.d(LOG_TAG, "File Copy : $result $tempFile")
                    ZixieFileProvider.uriToFile(context!!, it)?.let { file ->
                        ZLog.d(LOG_TAG, "File Copy : $file")
                        ZLog.d(
                            LOG_TAG, "File Content : ${FileUtils.getFileContent(file.absolutePath)}"
                        )
                    }
                }
            }
        }
    }
}