package com.bihe0832.android.base.test.dialog

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.View
import com.bihe0832.android.base.test.R
import com.bihe0832.android.common.test.base.BaseTestFragment
import com.bihe0832.android.common.test.item.TestItemData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.notification.NotifyManager
import com.bihe0832.android.lib.permission.PermissionDialog
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.DownloadProgressDialog
import com.bihe0832.android.lib.ui.dialog.LoadingDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.intent.IntentUtils
import java.util.*
import kotlin.collections.HashMap

class TestDialogFragment : BaseTestFragment() {
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(TestItemData("自定义弹框", View.OnClickListener { testCustom(activity) }))
            add(TestItemData("通用弹框", View.OnClickListener { testAlert(activity) }))
            add(TestItemData("进度条弹框", View.OnClickListener { testUpdate(activity) }))
            add(TestItemData("加载弹框", View.OnClickListener { testLoading(activity) }))
            add(TestItemData("自定义内容权限弹框", View.OnClickListener { testCustomPermission(activity) }))
            add(TestItemData("通用权限弹框", View.OnClickListener { testCommonPermission(activity) }))

        }
    }

    private fun testCustom(activity: Activity?) {
        showAlert(TestDialog(activity))
    }

    private fun testAlert(activity: Activity?) {
        showAlert(CommonDialog(activity))
    }

    fun showAlert(dialog: CommonDialog) {
        var title = "分享的标题"
        var content = "分享的内容"
        dialog.setTitle(title)
        dialog.setPositive("分享给我们")
        dialog.setContent("调试信息已经准备好，你可以直接「分享给我们」或将信息「复制到剪贴板」后转发给我们")
        dialog.setImageContentResId(R.mipmap.debug)
        dialog.setFeedBackContent("我们保证你提供的信息仅用于问题定位")
        dialog.setNegative("复制到剪切板")
        dialog.setShouldCanceled(true)
        dialog.setOnClickBottomListener(object : OnDialogListener {
            override fun onPositiveClick() {
                try {
                    IntentUtils.sendTextInfo(activity, title, content)
                    dialog.dismiss()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onNegativeClick() {
                try {
                    val cm = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as (ClipboardManager)
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                        cm.primaryClip = ClipData.newPlainText(null, content)
                    } else {
                        cm.text = content
                    }
                    dialog.dismiss()
                    ToastUtil.showShort(activity, "信息已保存到剪贴板")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancel() {

            }
        })
        dialog.show()
    }

    fun testUpdate(activity: Activity?) {
        var taskName = "TEST"
        var progressDialog = DownloadProgressDialog(activity).apply {
            setTitle("更新测试")
            setMessage("正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~")
            setCurrentSize(1)
            setAPKSize(10000)
            setNegative("取消下载")
            setPositive("后台下载")
            setOnClickListener(object : OnDialogListener {
                override fun onPositiveClick() {
                    dismiss()
                    TaskManager.getInstance().removeTask(taskName)
                }

                override fun onNegativeClick() {
                    dismiss()
                    TaskManager.getInstance().removeTask(taskName)
                }

                override fun onCancel() {
                    TaskManager.getInstance().removeTask(taskName)
                }
            })
        }
        ThreadManager.getInstance().runOnUIThread { progressDialog.show() }
        var i = 0

        TaskManager.getInstance().addTask(object : BaseTask() {
            override fun run() {
                if (i < 100) {
                    activity!!.runOnUiThread(Runnable {
                        progressDialog.setAPKSize(10000L)
                        progressDialog.setCurrentSize(100L * i)
                    })
                } else {
                    TaskManager.getInstance().removeTask(taskName)
                }
                i++
            }

            override fun getNextEarlyRunTime(): Int {
                return 0
            }

            override fun getMyInterval(): Int {
                return 1
            }

            override fun getTaskName(): String {
                return taskName
            }
        })
    }


    fun testLoading(activity: Activity?) {
        LoadingDialog(activity).apply {
            setIsFullScreen(true)
            setCanCanceled(false)
        }.let {
            it.show("这是一个测试~")
        }
    }

    private fun testCustomPermission(activity: Activity?) {
        PermissionManager.addPermissionContent(HashMap<String, String>().apply {
            put(Manifest.permission.RECORD_AUDIO, "M3U8下载助手需要将<font color ='#38ADFF'><b>下载数据存储在SD卡</b></font>才能访问，当前手机尚未开启悬浮窗权限，请点击「点击开启」前往设置！")
        })
        activity?.let { it ->
            PermissionDialog(it).let {permissionDialog->
                permissionDialog.show(Manifest.permission.RECORD_AUDIO, true, object : OnDialogListener {
                    override fun onPositiveClick() {
//                    openFloatPermissionSettings(context)
                        permissionDialog.dismiss()
                    }

                    override fun onNegativeClick() {
                        permissionDialog.dismiss()

                    }

                    override fun onCancel() {
                        permissionDialog.dismiss()
                    }
                })

                PermissionManager.addPermissionContent(HashMap<String, String>().apply {
                    put(Manifest.permission.RECORD_AUDIO, "")
                })
            }
        }
    }

    private fun testCommonPermission(activity: Activity?) {
        PermissionManager.addPermissionScene(HashMap<String, String>().apply {
            put(Manifest.permission.RECORD_AUDIO, "数据存储")
        })
        PermissionManager.addPermissionDesc(HashMap<String, String>().apply {
            put(Manifest.permission.RECORD_AUDIO, "SD卡权限")
        })
        activity?.let { it ->
            PermissionDialog(it).let {permissionDialog->
                permissionDialog.show(Manifest.permission.RECORD_AUDIO, true, object : OnDialogListener {
                    override fun onPositiveClick() {
//                    openFloatPermissionSettings(context)
                        permissionDialog.dismiss()
                    }

                    override fun onNegativeClick() {
                        permissionDialog.dismiss()

                    }

                    override fun onCancel() {
                        permissionDialog.dismiss()
                    }
                })
            }
        }
    }
}