package com.bihe0832.android.app.update

import android.app.Activity
import android.text.TextUtils
import com.bihe0832.android.app.api.AAFNetWorkApi
import com.bihe0832.android.app.R as AppR
import com.bihe0832.android.model.res.R as ModelResR
import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.framework.update.UpdateHelper
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.framework.update.setUpdateType
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.http.common.HTTPServer
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.thread.ThreadManager.getInstance
import java.net.HttpURLConnection

/**
 * AAF 应用更新管理器
 *
 * 负责应用版本更新的检查和展示，包括：
 * - 从服务器获取更新信息
 * - 判断更新类型（强制更新、建议更新等）
 * - 展示更新对话框
 *
 * @author zixie code@bihe0832.com
 * Created on 2019-10-21.
 */
object UpdateManager {

    private val TAG = "UpdateHelper-> "

    /**
     * 检查更新并显示对话框
     *
     * @param activity 当前 Activity
     * @param checkUpdateByUser 是否由用户主动触发检查
     * @param showIfNeedUpdate 如果有更新是否显示对话框
     */
    fun checkUpdateAndShowDialog(activity: Activity, checkUpdateByUser: Boolean, showIfNeedUpdate: Boolean) {
        fetchUpdate(activity, {
            getInstance().runOnUIThread {
                UpdateInfoLiveData.value = it
            }
            UpdateHelper.showUpdate(activity, checkUpdateByUser, showIfNeedUpdate, it)
        }, {
            if (checkUpdateByUser) {
                ZixieContext.showLongToast(activity.getString(ModelResR.string.dialog_apk_update_version_new))
            }
        })
    }

    /**
     * 从服务器获取更新信息
     *
     * @param activity 当前 Activity
     * @param successAction 成功回调
     * @param failedAction 失败回调
     */
    private fun fetchUpdate(
        activity: Activity,
        successAction: (info: UpdateDataFromCloud) -> Unit,
        failedAction: () -> Unit,
    ) {
        HTTPServer.getInstance().doRequest(
            AAFNetWorkApi.getCommonURL(ThemeResourcesManager.getString(AppR.string.update_url), ""),
        ) { statusCode, updateString ->
            AAFLoggerFile.logUpdate("statusCode:$statusCode")
            AAFLoggerFile.logUpdate("updateString:$updateString")
            if (HttpURLConnection.HTTP_OK == statusCode && !TextUtils.isEmpty(updateString)) {
                try {
                    var updateInfo =
                        JsonHelper.fromJson(updateString ?: "", UpdateDataFromCloud::class.java)
                    if (null == updateInfo) {
                        ZLog.d("$TAG:updateInfo null:")
                        failedAction()
                    } else {
                        updateInfo.setUpdateType()
                        ZLog.d("$TAG:fetchUpdate: $statusCode $updateString updateType:${updateInfo.updateType}")
                        successAction(updateInfo)
                    }
                } catch (e: Exception) {
                    ZLog.d("$TAG:fetchUpdate:" + e.message)
                    failedAction()
                }
            } else {
                ZLog.d("$TAG:fetchUpdate: $statusCode $updateString")
                failedAction()
            }
        }
    }
}
