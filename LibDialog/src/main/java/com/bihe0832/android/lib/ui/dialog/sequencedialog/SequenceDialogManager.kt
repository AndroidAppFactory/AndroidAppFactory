package com.bihe0832.android.lib.ui.dialog.sequencedialog

import com.bihe0832.android.lib.block.task.priority.BlockTaskManager
import com.bihe0832.android.lib.block.task.sequence.SequenceTask
import com.bihe0832.android.lib.block.task.sequence.SequenceTaskManager
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
open class SequenceDialogManager : BlockTaskManager() {

    private val TAG = "SequenceDialogManager"
    private val mSequenceTaskManager = SequenceTaskManager()

    class DependenceDialog(dialogID: String, maxWaitingSecond: Int) : SequenceTask.TaskDependence(dialogID, maxWaitingSecond * 1000L)

    fun showDialog(taskID: String, dialog: CommonDialog, dependList: List<DependenceDialog>) {
        ZLog.d(TAG, "Add dialog : $taskID - $dependList")

        mSequenceTaskManager.addTask(taskID, {
            ThreadManager.getInstance().runOnUIThread {
                dialog.setOnDismissListener {
                    mSequenceTaskManager.finishCurrentTask()
                    ZLog.d(TAG, "dialog $taskID dismiss and Finish task")
                }
                dialog.show()
            }

        }, dependList)
    }

    fun getSequenceTaskManager(): SequenceTaskManager {
        return mSequenceTaskManager
    }

}