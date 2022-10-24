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
object BlockDialogManager {
    private val mTaskQueue = BlockTaskManager()

    fun showDialog(dialog: CommonDialog, priority: Int, delayTime: Long) {
        ZLog.d("BlockDialogManager", "showDialog : $dialog after $delayTime")
        ThreadManager.getInstance().start({
            showDialog(dialog, priority)
        }, delayTime)
    }

    private fun showDialog(dialog: CommonDialog, priority: Int) {
        mTaskQueue.add(BlockDialogTask(dialog, dialog.title, priority))
    }

    private class BlockDialogTask(dialog: CommonDialog, name: String?, priority: Int) : BaseAAFBlockTask(name) {
        private var mDialog: CommonDialog? = null
        override fun doTask() {
            try {
                ThreadManager.getInstance().runOnUIThread {
                    mDialog!!.show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        init {
            setPriority(priority)
            mDialog = dialog
            mDialog!!.setOnDismissListener { unLockBlock() }
        }
    }
}