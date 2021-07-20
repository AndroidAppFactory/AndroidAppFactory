package com.bihe0832.android.app.update

import android.app.Activity
import android.text.TextUtils
import com.bihe0832.android.app.R
import com.bihe0832.android.app.api.AAFNetWorkApi
import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.framework.update.UpdateHelper
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.framework.update.setUpdateType
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.http.common.HTTPServer
import com.bihe0832.android.lib.http.common.HttpBasicRequest
import com.bihe0832.android.lib.http.common.HttpResponseHandler
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager.getInstance
import java.net.HttpURLConnection

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2019-10-21.
 * Description: Description
 *
 */
object UpdateManager {

    private val TAG = "MnaUpdateHelper-> "

    fun checkUpdateAndShowDialog(activity: Activity, checkUpdateByUser: Boolean) {
        fetchUpdate(activity, {
            getInstance().runOnUIThread {
                UpdateInfoLiveData.value = it
            }
            UpdateHelper.showUpdate(activity, checkUpdateByUser, it)
        }, {
            if (checkUpdateByUser) {
                ZixieContext.showLongToast("当前已是最新版本")
            }
        })
    }

    private fun fetchUpdate(activity: Activity, successAction: (info: UpdateDataFromCloud) -> Unit, failedAction: () -> Unit) {
        object : HttpBasicRequest() {
            override fun getUrl(): String {
                return AAFNetWorkApi.getCommonURL(activity.getString(R.string.update_url), "")
            }

            override fun getResponseHandler(): HttpResponseHandler {
                return HttpResponseHandler { statusCode, updateString ->
                    AAFLoggerFile.logUpdate("statusCode:$statusCode")
                    AAFLoggerFile.logUpdate("updateString:$updateString")
                    if (HttpURLConnection.HTTP_OK == statusCode && !TextUtils.isEmpty(updateString)) {
                        try {
//                            var updateString = "{\n" +
//                                    "  \"showRedMaxVersionCode\": 2,\n" +
//                                    "  \"needUpdateMinVersionCode\": 2,\n" +
//                                    "  \"forceUpdateMinVersionCode\": 0,\n" +
//                                    "  \"forceUpdateList\": \" \",\n" +
//                                    "  \"needUpdateList\": \" \",\n" +
//                                    "  \"newVersionName\": \"1.1.0\",\n" +
//                                    "  \"newVersionCode\": 2,\n" +
//                                    "  \"newVersionInfo\": \"1. 全新UI <BR> 2. 支持卸载\",\n" +
//                                    "  \"newVersionMD5\": \"7a413381aa84c837cc5de1577aec23a9\",\n" +
//                                    "  \"newVersionURL\": \"https://github.com/bihe0832/AndroidAppFactory-Sample/releases/download/V1.0.0.1/ZAPK_V1.0.0_1_release.apk\"\n" +
//                                    "}"
                            var updateInfo = JsonHelper.fromJson(updateString, UpdateDataFromCloud::class.java)
                            if (null == updateInfo) {
                                ZLog.d("${TAG}:updateInfo null:")
                                failedAction()
                            } else {
                                updateInfo.setUpdateType()
                                ZLog.d("${TAG}:fetchUpdate: $statusCode $updateString updateType:${updateInfo.updateType}")
                                successAction(updateInfo)
                            }
                        } catch (e: Exception) {
                            ZLog.d("${TAG}:fetchUpdate:" + e.message)
                            failedAction()
                        }
                    } else {
                        ZLog.d("${TAG}:fetchUpdate: $statusCode $updateString")
                        failedAction()
                    }
                }
            }
        }.let {
            HTTPServer.getInstance().doRequestAsync(it)
        }
    }
}