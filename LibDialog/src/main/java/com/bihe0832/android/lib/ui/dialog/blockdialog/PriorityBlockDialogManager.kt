package com.bihe0832.android.lib.ui.dialog.blockdialog

import com.bihe0832.android.lib.block.task.BaseAAFBlockTask
import com.bihe0832.android.lib.block.task.priority.PriorityBlockTaskManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2022/10/24.
 * Description: 阻塞式弹框，逐个根据前一个的优先级展示
 *
 */
open class PriorityBlockDialogManager : PriorityBlockTaskManager() {

    class BlockDialogTask(dialog: CommonDialog, name: String) : BaseAAFBlockTask(name) {
        private var mDialog: CommonDialog? = null
        override fun doTask() {
            try {
                if (mDialog != null) {
                    ThreadManager.getInstance().runOnUIThread {
                        mDialog?.show()
                    }
                } else {
                    unLockBlock()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                unLockBlock()
            }
        }

        init {
            mDialog = dialog
            mDialog!!.setOnDismissListener { unLockBlock() }
        }
    }

    fun addDelayShowTask(task: BaseAAFBlockTask, priority: Int, delayTime: Long) {
        task.apply {
            setPriority(priority)
        }.let {
            ZLog.d("PriorityBlockDialogManager", "showDelayTask : $task after $delayTime")
            ThreadManager.getInstance().start({
                add(it)
            }, delayTime)
        }
    }

    fun showDialog(dialog: CommonDialog, priority: Int, delayTime: Long) {
        addDelayShowTask(BlockDialogTask(dialog, dialog.title ?: ""), priority, delayTime)
    }
}