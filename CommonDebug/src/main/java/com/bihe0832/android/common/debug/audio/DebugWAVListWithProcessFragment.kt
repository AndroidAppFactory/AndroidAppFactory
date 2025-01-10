/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.common.debug.audio

import android.view.View
import com.bihe0832.android.common.debug.audio.process.AudioDataFactoryCallback
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.router.showH5Log
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.ui.dialog.impl.LoadingDialog

open class DebugWAVListWithProcessFragment : DebugWAVListFragment() {

    private var dialog: LoadingDialog? = null
    private var mDebugAudioDataFactory: DebugAudioDataFactory? = null

    override fun initView(view: View) {
        super.initView(view)
        mDebugAudioDataFactory = getAudioDataProcessInterface()
        dialog = LoadingDialog(activity!!).apply {
            setOnCancelListener {
                mDebugAudioDataFactory?.forceStop()
            }
        }
    }

    open fun getLogFile(): String {
        return LoggerFile.getZixieFileLogPathByModule(
            "audio", ZixieContext.getLogFolder(), LoggerFile.TYPE_HTML
        )
    }

    open fun getProcessAudioList(logFile: String, logHeader: String): DebugItemData {
        return getDebugItem(
            "<font color ='#3AC8EF'><b>批量处理音频并记录</b></font>"
        ) {
            LoggerFile.initFile(
                logFile,
                LoggerFile.getH5LogHeader("<title>本地音频批量处理结果展示</title>\n") + logHeader + LoggerFile.getH5Content(),
                true
            )
            mDebugAudioDataFactory?.processAudioList(logFile, getFileList())
        }
    }

    override fun getHeader(): ArrayList<CardBaseModule> {
        val data = ArrayList<CardBaseModule>().apply {
            addAll(super.getHeader())
            add(
                getProcessAudioList(
                    getLogFile(),
                    "<div><font color='#3AC8EF'>文件目录：</font><BR>" +
                            "${
                                folder.replace(context!!.packageName, " * ").replace("/", " / ")
                            }<BR>" +
                            "<BR> </div>"
                )
            )
        }
        return data
    }

    fun getAudioDataFactoryCallback(): AudioDataFactoryCallback {
        return object : AudioDataFactoryCallback {
            override fun onStart() {
                dialog?.show("开始处理……")
            }

            override fun onProcess(current: Int, num: Int) {
                dialog?.show("共 $num 个音频，正在处理第 $current 个……")
            }

            override fun onCancel() {
                dialog?.dismiss()
            }

            override fun onComplete() {
                dialog?.dismiss()
                showH5Log(getLogFile())
            }
        }
    }

    open fun getAudioDataProcessInterface(): DebugAudioDataFactory {
        return DebugAudioDataFactory(getAudioDataFactoryCallback())
    }
}
