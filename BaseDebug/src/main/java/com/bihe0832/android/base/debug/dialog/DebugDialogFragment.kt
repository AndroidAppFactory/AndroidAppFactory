package com.bihe0832.android.base.debug.dialog

import android.app.Activity
import android.graphics.Color
import android.view.View
import com.bihe0832.android.app.dialog.AAFUniqueDialogManager
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.base.debug.empty.DebugBottomActivity
import com.bihe0832.android.base.debug.temp.DebugTempFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ZixieContext.showToast
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.text.ClipboardUtil
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.ui.dialog.*
import com.bihe0832.android.lib.ui.dialog.blockdialog.DependenceBlockDialogManager
import com.bihe0832.android.lib.ui.dialog.blockdialog.PriorityBlockDialogManager
import com.bihe0832.android.lib.ui.dialog.impl.DialogUtils
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCompletedCallback
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.MathUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils

class DebugDialogFragment : DebugEnvFragment() {

    private val INNER_PAUSE_TASK_ID = "AAFInnerTaskForDependenceBlockDialogManager"


    private val mDependenceBlockDialogManager by lazy {
        DependenceBlockDialogManager(false)
    }

    override fun initView(view: View) {
        super.initView(view)

    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("唯一弹框", View.OnClickListener { testUnique() }))
            add(DebugItemData("根据优先级逐次弹框", View.OnClickListener { testBlock() }))
            add(DebugItemData("根据弹框顺序弹不自动弹", View.OnClickListener { testSequence1() }))
            add(DebugItemData("根据弹框顺序弹自动弹", View.OnClickListener { testSequence2() }))
            add(DebugItemData("弹框顺序手动触发启动", View.OnClickListener {
                mDependenceBlockDialogManager.start()
                resume()
            }))
            add(DebugItemData("弹框顺序手动触发暂停", View.OnClickListener { pause() }))

            add(DebugItemData("底部列表弹框", View.OnClickListener { showBottomDialog(activity!!) }))
            add(DebugItemData("底部分享Activity", View.OnClickListener { startActivityWithException(DebugBottomActivity::class.java) }))
            add(DebugItemData("底部Dialog", View.OnClickListener { showAlert(BottomDialog(activity!!)) }))
            add(DebugItemData("通用弹框", View.OnClickListener { testAlert(activity!!) }))
            add(DebugItemData("单选列表弹框", View.OnClickListener { testRadio(activity) }))
            add(DebugItemData("自定义弹框", View.OnClickListener { testCustom(activity) }))
            add(DebugItemData("带输入弹框", View.OnClickListener { tesInput(activity!!) }))

            add(DebugItemData("进度条弹框", View.OnClickListener { testUpdate(activity) }))
            add(DebugItemData("加载弹框", View.OnClickListener { testLoading(activity) }))
            add(changeEnv("模拟环境切换并自动重启", CHANGE_ENV_EXIST_TYPE_RESTART))
            add(changeEnv("模拟环境切换并自动退出", CHANGE_ENV_EXIST_TYPE_EXIST))
            add(changeEnv("模拟环境切换并立即生效", CHANGE_ENV_EXIST_TYPE_NOTHING))

        }
    }

    private fun testRadio(activity: Activity?) {
        var dialog = RadioDialog(activity)

        var title = "分享的标题"
        var content = "调试信息已经准备好，你可以直接「分享给我们」或将信息「复制到剪贴板」后转发给我们"
        dialog.setTitle(title)
        dialog.setContent(content)
        dialog.setPositive("分享给我们")
        dialog.setNegative("复制到剪切板")
        dialog.setFeedBackContent(getString(R.string.theme_change_tips))
        dialog.setShouldCanceled(true)

        dialog.setRadioData(mutableListOf<String>().apply {
            for (i in 0..100) {
                add("RadioButton $i")
            }
        }, 1) { which -> showToast("RadioButton  $which") }
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

    private fun testCustom(activity: Activity?) {
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

    private fun testAlert(activity: Activity) {
        showAlert(CommonDialog(activity))
    }

    fun getAlert(dialog: CommonDialog) {
        var title = "分享的标题"
        var content = "分享的内容"
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

    fun showAlert(dialog: CommonDialog) {
        getAlert(dialog)
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
            setLoadingType(LoadingDialog.LOADING_TYPE_DOTS)
        }.let {
            it.show("加载中 请稍候")
        }
    }

    fun showBottomDialog(activity: Activity) {
        BottomListDialog(activity).apply {
            setItemList(mutableListOf<String>("Item 1", "<Strong>" + TextFactoryUtils.getSpecialText("Item 2", Color.RED) + "</Strong>", "Item 3"))
            setOnItemClickListener {
                ZixieContext.showToast("Item" + it)
                dismiss()
            }
        }.let {
            it.show()
        }

    }

    fun tesInput(activity: Activity) {
        DialogUtils.showInputDialog(activity, "测试标题", "", "默认值", InputDialogCompletedCallback {
            showToast(it)
        }

        )
    }

    fun testUnique() {
        for (i in 0 until 3) {
            AAFUniqueDialogManager.tipsUniqueDialogManager.showUniqueDialog(requireActivity(), "212", "这是内容 ${System.currentTimeMillis()}", "OK")
        }
    }

    fun changeEnv(title: String, type: Int): DebugItemData {
        return DebugItemData(title, View.OnClickListener {
            mutableListOf<String>().apply {
                add("测试环境1")
                add("测试环境2")
            }.let { data ->
                getChangeEnvSelectDialog("查看应用版本及环境", data, 0, object : DebugEnvFragment.OnEnvChangedListener {
                    override fun onChanged(index: Int) {
                        showChangeEnvDialog("应用环境重启", data.get(index), type)
                    }
                }).let {
                    it.show()
                }
            }
        })
    }


    private val mPriorityBlockDialogManager = PriorityBlockDialogManager()
    fun testBlock() {
        for (i in 0..5) {
            mPriorityBlockDialogManager.showDialog(CommonDialog(activity).apply {
                getAlert(this)
                setTitle("弹框 $i")
            }, MathUtils.getRandNumByLimit(0, 10), i * 3000L)
        }

    }


    var dependList = HashMap<String, List<DependenceBlockDialogManager.DependenceDialog>>().apply {
        put("Dialog0", mutableListOf<DependenceBlockDialogManager.DependenceDialog>().apply {
//                add(DependenceBlockDialogManager.DependenceDialog("Dialog-1", 6))
        })
        put("Dialog1", mutableListOf<DependenceBlockDialogManager.DependenceDialog>().apply {
            add(DependenceBlockDialogManager.DependenceDialog("Dialog0", 6))
        })
        put("Dialog2", mutableListOf<DependenceBlockDialogManager.DependenceDialog>().apply {
            add(DependenceBlockDialogManager.DependenceDialog("Dialog0", 10))
            add(DependenceBlockDialogManager.DependenceDialog("Dialog1", 6))
        })
        put("Dialog3", mutableListOf<DependenceBlockDialogManager.DependenceDialog>().apply {
            add(DependenceBlockDialogManager.DependenceDialog("Dialog2", 6))
//                add(DependenceBlockDialogManager.DependenceDialog("Dialog-1", 6))

        })
        put("Dialog4", mutableListOf<DependenceBlockDialogManager.DependenceDialog>().apply {
            add(DependenceBlockDialogManager.DependenceDialog("Dialog3", 6))
            add(DependenceBlockDialogManager.DependenceDialog("Dialog1", 6))

        })
        put("Dialog5", mutableListOf<DependenceBlockDialogManager.DependenceDialog>().apply {
            add(DependenceBlockDialogManager.DependenceDialog("Dialog4", 6))
            add(DependenceBlockDialogManager.DependenceDialog("Dialog1", 6))
        })
    }

    fun testSequence1() {

        val taskIDList = mutableListOf<String>("Dialog0", "Dialog1", "Dialog4", "Dialog5", "Dialog6")
        taskIDList.shuffled().forEach { taskID ->
            dependList.get(taskID)?.let {
                mDependenceBlockDialogManager.showDialog(taskID, CommonDialog(ZixieContext.getCurrentActivity()).apply {
                    getAlert(this)
                    setTitle("弹框 $taskID")
                    setOnDismissListener {
                        startDebugActivity(DebugTempFragment::class.java, "临时测试(Temp)")
                    }
                }, it)
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean, hasCreateView: Boolean) {
        super.setUserVisibleHint(isVisibleToUser, hasCreateView)
        if (!isVisibleToUser) {
            pause()
        }
    }

    fun testSequence2() {
        val taskIDList = mutableListOf<String>("Dialog2", "Dialog3", "Dialog4")
        taskIDList.shuffled().forEach { taskID ->
            dependList.get(taskID)?.let {
                mDependenceBlockDialogManager.showDialog(taskID, CommonDialog(activity).apply {
                    getAlert(this)
                    setTitle("弹框 $taskID")
                }, it)
            }

        }
        mDependenceBlockDialogManager.start()
    }

    private fun resume() {
        mDependenceBlockDialogManager.getDependentTaskManager().finishTask(INNER_PAUSE_TASK_ID)
    }

    private fun pause() {
        mDependenceBlockDialogManager.getDependentTaskManager().addTask(INNER_PAUSE_TASK_ID, 1000, {}, mutableListOf())
    }
}