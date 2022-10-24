package com.bihe0832.android.lib.ui.dialog.blockdialog

import com.bihe0832.android.lib.block.task.BaseAAFBlockTask
import com.bihe0832.android.lib.block.task.BlockTaskManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2022/10/24.
 * Description: Description
 *
 */
class BlockDialogManager : BlockTaskManager() {

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
            }
        }

        init {
            mDialog = dialog
            mDialog!!.setOnDismissListener { unLockBlock() }
        }
    }

    fun showDelayTask(task: BaseAAFBlockTask, priority: Int, delayTime: Long) {
        ZLog.d("MnaBlockTaskManager", "showDelayTask : $task after $delayTime")
        ThreadManager.getInstance().start({
            add(task.apply {
                setPriority(priority)
            })
        }, delayTime)
    }

    fun showDialog(dialog: CommonDialog, priority: Int, delayTime: Long) {
        showDelayTask(BlockDialogTask(dialog, dialog.title), priority, delayTime)
    }


}