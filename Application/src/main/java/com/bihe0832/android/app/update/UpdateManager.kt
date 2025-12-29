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
 *
 * @author zixie code@bihe0832.com
 * Created on 2019-10-21.
 * Description: Description
 *
 */
object UpdateManager {

    private val TAG = "UpdateHelper-> "

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
                    //                            var updateString = "{\"newVersionName\":\"1.3.1\",\"newVersionCode\":\"140\",\"newVersionURL\":\"https://android.bihe0832.com/app/release/ZAPK_official.apk\",\"newVersionMD5\":\"12973fbecaceaf6e1426d1936eb56d91\",\"newVersionInfo\":\"1. 添加邮件通知<BR>2. 修复下拉刷新的问题\",\"showRedMaxVersionCode\":\"139\",\"needUpdateMinVersionCode\":\"139\",\"forceUpdateMinVersionCode\":\"1\",\"needUpdateList\":\"\",\"forceUpdateList\":\"\"}"
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
