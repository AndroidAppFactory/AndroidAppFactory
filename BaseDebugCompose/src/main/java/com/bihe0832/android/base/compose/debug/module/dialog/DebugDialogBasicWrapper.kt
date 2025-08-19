package com.bihe0832.android.base.compose.debug.module.dialog

import android.app.Activity
import com.bihe0832.android.app.dialog.AAFUniqueDialogManager
import com.bihe0832.android.base.compose.debug.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ZixieContext.showToast
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.text.ClipboardUtil
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.blockdialog.PriorityBlockDialogManager
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedStringCallback
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.BottomListDialog
import com.bihe0832.android.lib.ui.dialog.impl.DownloadProgressDialog
import com.bihe0832.android.lib.ui.dialog.impl.ImageDialog
import com.bihe0832.android.lib.ui.dialog.impl.LoadingDialog
import com.bihe0832.android.lib.ui.dialog.impl.RadioDialog
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.MathUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils

private val mPriorityBlockDialogManager = PriorityBlockDialogManager()
internal fun testBlock(activity: Activity) {
    for (i in 0..5) {
        mPriorityBlockDialogManager.showDialog(
            CommonDialog(activity).apply {
                getAlert(activity, this)
                setTitle("弹框 $i")
            },
            MathUtils.getRandNumByLimit(0, 10),
            i * 3000L,
        )
    }
}


internal fun testUnique(activity: Activity) {
    for (i in 0 until 3) {
        AAFUniqueDialogManager.tipsUniqueDialogManager.showUniqueDialog(
            activity,
            "key",
            "这是内容 ${System.currentTimeMillis()}",
            "OK",
        )
    }
}



internal fun getAlert(activity: Activity, dialog: CommonDialog) {
    val title = "分享的标题"
    val content = "分享的内容"
    dialog.setTitle(title)
    dialog.setPositive("分享给我们")
    dialog.setContent("调试信息已经准备好，你可以直接「分享给我们」或将信息「复制到剪贴板」后转发给我们调试信息已经准备好，你可以直接「分享给我们」或将信息「复制到剪贴板」后转发给我们调试信息已经准备好，你可以直接「分享给我们」或将信息「复制到剪贴板」后转发给我们调试信息已经准备好，你可以直接「分享给我们」或将信息「复制到剪贴板」后转发给我们")
//        dialog.setImageContentResId(R.mipmap.debug)
//        dialog.setFeedBackContent("我们承诺你提供的信息仅用于问题定位")
//        dialog.setNegative("复制到剪切板")
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
                ClipboardUtil.copyToClipboard(activity, content)
                dialog.dismiss()
                ToastUtil.showShort(activity, "信息已保存到剪贴板")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCancel() {
        }
    })
}


internal fun testLoading(activity: Activity?) {
    LoadingDialog(activity).apply {
        setIsFullScreen(true)
        setCanCanceled(false)
        setLoadingType(LoadingDialog.LOADING_TYPE_DOTS)
    }.let {
        it.show("加载中 请稍候")
    }
}

internal fun showBottomDialog(activity: Activity) {
    BottomListDialog(activity).apply {
        setItemList(
            mutableListOf<String>(
                "Item 1",
                "<Strong>" + TextFactoryUtils.getSpecialText(
                    "Item 2", android.graphics.Color.RED
                ) + "</Strong>",
                "Item 3",
            ),
        )
        setOnItemClickListener {
            ZixieContext.showToast("Item" + it)
            dismiss()
        }
    }.let {
        it.show()
    }
}

internal fun testInput(activity: Activity) {
    DialogUtils.showInputDialog(activity,
        "测试标题",
        "",
        "默认值",
        DialogCompletedStringCallback { showToast(it) }

    )
}

internal fun showAlert(activity: Activity, dialog: CommonDialog) {
    getAlert(activity, dialog)
    dialog.show()
}

internal fun testAlert(activity: Activity) {
    showAlert(activity, CommonDialog(activity))
}

internal fun testAlertTools(activity: Activity) {
    DialogUtils.showAlertDialog(
        activity,
        "Alert 测试",
        "Alert 测试",
        false,
        object : OnDialogListener {
            override fun onPositiveClick() {
                ThreadManager.getInstance().start({
                    DialogUtils.showAlertDialog(activity, "Alert 测试")
                }, 2)
            }

            override fun onNegativeClick() {
            }

            override fun onCancel() {
            }
        },
    )
}

internal fun testVURLImage(activity: Activity) {
    val dialog = ImageDialog(activity)
    dialog.setImageUrl("https://cdn.bihe0832.com/images/cv_v.png")
    dialog.setOritation(ImageDialog.ORIENTATION_VERTICAL)
    dialog.show()
}

internal fun testHURLImage(activity: Activity) {
    val dialog = ImageDialog(activity)
    dialog.setImageUrl("https://cdn.bihe0832.com/images/cv.png")
    dialog.setOritation(ImageDialog.ORIENTATION_HORIZONTAL)
    dialog.show()
}

internal fun testImage(activity: Activity?) {
    val dialog = ImageDialog(activity)
    dialog.setImageRes(R.drawable.icon_android)
    dialog.show()
}

internal fun testRadio(activity: Activity?) {
    val dialog = RadioDialog(activity)

    val title = "分享的标题"
    val content = "调试信息已经准备好，你可以直接「分享给我们」或将信息「复制到剪贴板」后转发给我们"
    dialog.setTitle(title)
    dialog.setContent(content)
    dialog.setPositive("分享给我们")
    dialog.setNegative("复制到剪切板")
    dialog.setFeedBackContent(ThemeResourcesManager.getString(R.string.theme_change_tips))
    dialog.setShouldCanceled(true)

    dialog.setRadioData(
        mutableListOf<String>().apply {
            for (i in 0..100) {
                add("RadioButton $i")
            }
        },
        1,
    ) { which -> showToast("RadioButton  $which") }
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
                ClipboardUtil.copyToClipboard(activity, content)
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

internal fun testCustom(activity: Activity?) {
    var dialog = DebugDialog(activity)

    var title = "分享的标题"
    var content = "分享的内容"
    dialog.setTitle(title)
    dialog.setImageContentResId(R.mipmap.debug)
    dialog.setFeedBackContent("我们承诺你提供的信息仅用于问题定位")
    dialog.setPositive("分享给我们")
    dialog.setContent("调试信息已经准备好，你可以直接「分享给我们」或将信息「复制到剪贴板」后转发给我们")
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
                ClipboardUtil.copyToClipboard(activity, content)
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

internal fun testUpdate(activity: Activity?) {
    var taskName = "TEST"
    var progressDialog = DownloadProgressDialog(activity).apply {
        setTitle("更新测试")
        setMessage("正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~正在更新~")
        setCurrentSize(
            1, MathUtils.getRandNumByLimit(
                FileUtils.SPACE_KB.toInt(), FileUtils.SPACE_MB.toInt() * 10
            ).toLong()
        )
        setContentSize(FileUtils.SPACE_MB.toLong() * 1011 - FileUtils.SPACE_KB.toInt() * 800)
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
            if (i < 1000) {
                activity!!.runOnUiThread(
                    Runnable {
                        progressDialog.setCurrentSize(
                            FileUtils.SPACE_KB.toLong() * 1256 * i * 7, MathUtils.getRandNumByLimit(
                                FileUtils.SPACE_KB.toInt(), FileUtils.SPACE_MB.toInt() * 10
                            ).toLong() * 7
                        )
                    },
                )
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

