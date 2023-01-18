package com.bihe0832.android.lib.ui.dialog.blockdialog

import android.content.DialogInterface
import com.bihe0832.android.lib.block.task.dependence.DependenceBlockTask
import com.bihe0832.android.lib.block.task.dependence.DependenceBlockTaskManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2022/10/24.
 * Description: 阻塞式弹框，只有他依赖的弹框都正常弹出，他才弹出
 *
 */
open class DependenceBlockDialogManager(autoStart: Boolean) {



    private val TAG = "DependenceBlockDialogManager"
    private val mDependenceBlockTaskManager = DependenceBlockTaskManager(autoStart)
    private var startNextAfterFinished = 200L

    class DependenceDialog(dialogID: String, maxWaitingSecond: Int) : DependenceBlockTask.TaskDependence(dialogID, maxWaitingSecond * 1000L)

    fun showDialog(taskID: String, dialog: CommonDialog, dependList: List<DependenceDialog>) {
        ZLog.d(TAG, "Add dialog : $taskID - $dependList")
        mDependenceBlockTaskManager.addTask(taskID, {
            ThreadManager.getInstance().runOnUIThread {
                val originDismissListener: DialogInterface.OnDismissListener? = dialog.onDismissListener
                dialog.setOnDismissListener { disMissDialog ->
                    originDismissListener?.onDismiss(disMissDialog)
                    ThreadManager.getInstance().start({
                        mDependenceBlockTaskManager.finishTask(taskID)
                    }, startNextAfterFinished)
                    ZLog.d(TAG, "dialog $taskID dismiss and Finish task")
                }
                dialog.show()
            }
        }, dependList)
    }

    fun setWaitTimeForStartNextAfterFinished(time: Long) {
        startNextAfterFinished = time
    }

    fun getDependentTaskManager(): DependenceBlockTaskManager {
        return mDependenceBlockTaskManager
    }

    fun start() {
        mDependenceBlockTaskManager.start()
    }
}