package com.bihe0832.android.framework.file

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import java.io.File

/**
 *
 *   基于AAF的文件信息转换
 */
object AAFFileTools {

    const val DEFAULT_LOG_FILE_SIZE = FileUtils.SPACE_MB * 3

    fun getPathFromUri(uri: Uri?): String {
        uri?.let {
            return ZixieFileProvider.uriToFile(ZixieContext.applicationContext, uri).absolutePath
        }
        return ""
    }

    fun getUriFromPath(path: String?): Uri? {
        try {
            return ZixieFileProvider.getZixieFileProvider(ZixieContext.applicationContext!!, File(path))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun openFile(filePath: String) {
        try {
            val map = HashMap<String, String>()
            map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode(filePath)
            RouterAction.openFinalURL(
                RouterAction.getFinalURL(RouterConstants.MODULE_NAME_EDITOR, map),
                Intent.FLAG_ACTIVITY_NEW_TASK,
            )
        } catch (e: java.lang.Exception) {
            // 当系统没有携带文件打开软件，提示
            e.printStackTrace()
        }
    }

    fun openFileWithTips(activity: Activity, filePath: String) {
        try {
            if (FileUtils.checkFileExist(filePath) && File(filePath).length() > DEFAULT_LOG_FILE_SIZE) {
                DialogUtils.showConfirmDialog(
                    activity,
                    "超大文件查看",
                    "文件 「<font color ='#3AC8EF'><b>${FileUtils.getFileName(filePath)} (${
                        FileUtils.getFileLength(
                            File(
                                filePath,
                            ).length(),
                        )
                    })」</b></font> 文件较大，手机打开耗时较久或者容易出现打开失败的情况，建议发送到电脑查看。本地路径: <BR> $filePath ",
                    "发送文件",
                    "继续查看",
                    object :
                        OnDialogListener {
                        override fun onPositiveClick() {
                            sendFile(filePath)
                        }

                        override fun onNegativeClick() {
                            openFile(filePath)
                        }

                        override fun onCancel() {
                        }
                    },
                )
            } else {
                openFile(filePath)
            }
        } catch (e: java.lang.Exception) {
            // 当系统没有携带文件打开软件，提示
            e.printStackTrace()
        }
    }

    fun sendFile(filePath: String) {
        try {
            // 设置intent的data和Type属性
            ZixieContext.applicationContext?.let { context ->
                try {
                    // 设置intent的data和Type属性
                    FileUtils.sendFile(context, filePath)
                } catch (e: java.lang.Exception) {
                    // 当系统没有携带文件打开软件，提示
                    // 当系统没有携带文件打开软件，提示
                    e.printStackTrace()
                }
            }
        } catch (e: java.lang.Exception) {
            // 当系统没有携带文件打开软件，提示
            e.printStackTrace()
        }
    }
}
