package com.bihe0832.android.framework.download

import android.app.Activity
import android.app.DownloadManager
import android.os.Build
import android.text.TextUtils
import com.bihe0832.android.framework.R
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadUtils.cancleDownload
import com.bihe0832.android.lib.download.DownloadUtils.startDownload
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.DownloadProgressDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.log.ZLog


object DownloadHelper {
    private const val DownloadApkNamePrefix = "zixie_"
    fun startDownloadAPK(activity: Activity?, title: String, msg: String?, url: String?, md5: String?, canCancle: Boolean, listener: OnDialogListener?, downloadListener: DownloadListener?) {
        if (null == activity || url.isNullOrBlank()) {
            return
        }

        val context = activity.applicationContext
        var progressDialog = DownloadProgressDialog(activity).apply {
            setTitle(title)
            setMessage(msg)
            setCurrentSize(0)
            if (!canCancle) {
                setCancelable(false)
            }
            setPositive("后台下载")
            setNegtive("取消下载")
            setOnClickListener(object : OnDialogListener {
                override fun onPositiveClick() {
                    ZixieContext.showToastJustAPPFront("已切换到后台下载，你可以在通知栏查看下载进度")
                    dismiss()
                    listener?.onPositiveClick()
                }

                override fun onCloseClick() {
                    TODO("Not yet implemented")
                }

                override fun onNegtiveClick() {
                    cancleDownload(url)
                    dismiss()
                    listener?.onNegtiveClick()
                }
            })
        }
        ThreadManager.getInstance().runOnUIThread { progressDialog.show() }
        val fileName = URLUtils.getFileName(url).let {
            if (TextUtils.isEmpty(it)) {
                DownloadApkNamePrefix + if (TextUtils.isEmpty(md5)) {
                    System.currentTimeMillis()
                } else {
                    md5
                }
            } else {
                it
            }
        }
        val item = DownloadItem()
        item.notificationVisibility = DownloadManager.Request.VISIBILITY_VISIBLE
        item.dowmloadTitle = ZixieContext.applicationContext!!.getString(R.string.app_name) + " " + title
        msg?.let {
            item.downloadDesc = it
        }
        md5?.let {
            item.fileMD5 = it
        }
        item.fileName = fileName
        item.downloadURL = url
        startDownload(context, item, object : DownloadListener {
            override fun onProgress(total: Long, cur: Long) {
                activity.runOnUiThread(Runnable {
                    progressDialog.setAPKSize(total.toInt())
                    progressDialog.setCurrentSize(cur.toInt())
                })
                downloadListener?.onProgress(total, cur)
            }

            override fun onSuccess(finalFileName: String) {
                ZLog.i("startDownloadApk download installApkPath: $finalFileName")
                downloadListener?.onSuccess(finalFileName)
                activity.runOnUiThread(Runnable {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN && activity.isDestroyed) {
                        return@Runnable
                    } else if (activity.isFinishing) {
                        return@Runnable
                    }

                    progressDialog.apply {
                        setAPKSize(0)
                        setPositive("稍候安装")
                        setNegtive("点击安装")
                        setOnClickListener(object : OnDialogListener {
                            override fun onPositiveClick() {
                                progressDialog.dismiss()
                                listener?.onNegtiveClick()
                            }

                            override fun onCloseClick() {
                            }

                            override fun onNegtiveClick() {
                                progressDialog.dismiss()
                                InstallUtils.installAPP(context, finalFileName)
                                listener?.onPositiveClick()
                            }
                        })
                    }.let { it.show() }
                    // 下载完成，自动进行安装
                    InstallUtils.installAPP(context, finalFileName)
                })
            }

            override fun onError(error: Int, errmsg: String) {
                ZixieContext.showToastJustAPPFront("应用下载失败（$error）")
                downloadListener?.onError(error, errmsg)
            }
        })
    }
}