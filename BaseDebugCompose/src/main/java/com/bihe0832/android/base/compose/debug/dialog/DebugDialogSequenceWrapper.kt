package com.bihe0832.android.base.compose.debug.dialog

import android.app.Activity
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.blockdialog.DependenceBlockDialogManager


internal val mDependenceBlockDialogManager by lazy {
    DependenceBlockDialogManager(false)
}

internal val INNER_PAUSE_TASK_ID = "AAFInnerTaskForDependenceBlockDialogManager"

internal fun resume() {
    mDependenceBlockDialogManager.getDependentTaskManager().finishTask(INNER_PAUSE_TASK_ID)
}

internal fun reset() {
    mDependenceBlockDialogManager.getDependentTaskManager().reset()
}

internal fun pause() {
    mDependenceBlockDialogManager.getDependentTaskManager()
        .addTask(INNER_PAUSE_TASK_ID, 1000, {}, mutableListOf())
}

internal val dependList =
    HashMap<String, List<DependenceBlockDialogManager.DependenceDialog>>().apply {
        put(
            "Dialog0",
            mutableListOf<DependenceBlockDialogManager.DependenceDialog>().apply {
//                add(DependenceBlockDialogManager.DependenceDialog("Dialog-1", 6))
            },
        )
        put(
            "Dialog1",
            mutableListOf<DependenceBlockDialogManager.DependenceDialog>().apply {
                add(DependenceBlockDialogManager.DependenceDialog("Dialog0", 6))
            },
        )
        put(
            "Dialog2",
            mutableListOf<DependenceBlockDialogManager.DependenceDialog>().apply {
                add(DependenceBlockDialogManager.DependenceDialog("Dialog0", 10))
                add(DependenceBlockDialogManager.DependenceDialog("Dialog1", 6))
            },
        )
        put(
            "Dialog3",
            mutableListOf<DependenceBlockDialogManager.DependenceDialog>().apply {
                add(DependenceBlockDialogManager.DependenceDialog("Dialog2", 6))
//                add(DependenceBlockDialogManager.DependenceDialog("Dialog-1", 6))
            },
        )
        put(
            "Dialog4",
            mutableListOf<DependenceBlockDialogManager.DependenceDialog>().apply {
                add(DependenceBlockDialogManager.DependenceDialog("Dialog3", 6))
                add(DependenceBlockDialogManager.DependenceDialog("Dialog1", 6))
            },
        )
        put(
            "Dialog5",
            mutableListOf<DependenceBlockDialogManager.DependenceDialog>().apply {
                add(DependenceBlockDialogManager.DependenceDialog("Dialog4", 6))
                add(DependenceBlockDialogManager.DependenceDialog("Dialog1", 6))
            },
        )
    }

internal fun testSequence1(activity: Activity) {
    val taskIDList =
        mutableListOf<String>("Dialog0", "Dialog1", "Dialog4", "Dialog5", "Dialog6")
    taskIDList.shuffled().forEach { taskID ->
        dependList.get(taskID)?.let {
            mDependenceBlockDialogManager.showDialog(
                taskID,
                CommonDialog(ZixieContext.getCurrentActivity()).apply {
                    getAlert(activity, this)
                    setTitle("弹框 $taskID")
                    setOnDismissListener {
                        DebugUtilsV2.startComposeActivity(
                            ZixieContext.applicationContext!!,
                            "弹框 $taskID",
                            ""
                        )
                    }
                },
                it,
            )
        }
    }
}


internal fun testSequence2(activity: Activity) {
    val taskIDList = mutableListOf<String>("Dialog2", "Dialog3", "Dialog4")
    taskIDList.shuffled().forEach { taskID ->
        dependList.get(taskID)?.let {
            mDependenceBlockDialogManager.showDialog(
                taskID,
                CommonDialog(activity).apply {
                    getAlert(activity, this)
                    setTitle("弹框 $taskID")
                },
                it,
            )
        }
    }
    mDependenceBlockDialogManager.start()
}