/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.common.debug.audio

import android.content.Intent
import android.util.Log
import android.view.View
import com.bihe0832.android.common.debug.R
import com.bihe0832.android.common.debug.audio.card.AudioData
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.item.getTipsItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.router.showH5Log
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.audio.wav.WaveFileReader
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.FileUtils.getFileLength
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.LoadingDialog
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.lib.audio.player.block.AudioPLayerManager
import java.io.File

open class DebugWAVListFragment : DebugEnvFragment() {
    val TAG = this.javaClass.simpleName
    private val mAudioPLayerManager by lazy { AudioPLayerManager() }
    protected var folder = AAFFileWrapper.getMediaTempFolder()
    private var isPlay = false
    private val mListLiveData = object : CommonListLiveData() {
        override fun initData() {
            ThreadManager.getInstance().start {
                getDataList().let {
                    ThreadManager.getInstance().runOnUIThread {
                        postValue(it)
                    }
                }
            }
        }

        override fun refresh() {
            initData()
        }

        override fun loadMore() {

        }

        override fun hasMore(): Boolean {
            return false
        }

        override fun canRefresh(): Boolean {
            return true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FileSelectTools.FILE_CHOOSER && resultCode == RESULT_OK) {
            data?.extras?.getString(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, "")?.let { filePath ->
                folder = filePath
                mListLiveData.refresh()
            }
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return mListLiveData
    }

    override fun initView(view: View) {
        super.initView(view)
        mAdapter.apply {
            setOnItemChildClickListener { baseQuickAdapter, view, i ->
                (baseQuickAdapter.getItem(i) as AudioData?)?.let { audioData ->
                    when (view.id) {
                        R.id.audio_title, R.id.audio_desc -> {
                            ThreadManager.getInstance().start {
                                itemClickAction(audioData, isPlay)
                                post {
                                    mAdapter.notifyItemChanged(i)
                                }
                            }
                        }

                        R.id.audio_icon -> {
                            audioIconClickAction(audioData)
                        }
                    }
                }
            }
            setOnItemChildLongClickListener { baseQuickAdapter, view, i ->
                (baseQuickAdapter.getItem(i) as AudioData?)?.let { audioData ->
                    when (view.id) {
                        R.id.audio_icon -> {
                            itemLongClickAction(audioData, i)
                        }

                        R.id.audio_title, R.id.audio_desc -> {
                            audioIconLongClickAction(audioData)
                        }

                        else -> {}
                    }
                }
                return@setOnItemChildLongClickListener false
            }
        }
    }

    open fun getTips(): DebugItemData? {
        val tips =
            "1. <b><font color='#3AC8EF'>点击</font>图标</b>，播放音频，<b><font color='#3AC8EF'>点击</font>标题和内容</b>，${
                if (isPlay) {
                    "播放音频并识别音频内容"
                } else {
                    "识别音频内容"
                }
            }<BR>" + "2. <b><font color='#3AC8EF'>长按</font>图标</b>，可以删除音频，<b><font color='#3AC8EF'>长按</font>标题和内容</b>，可以发送音频"

        return getTipsItem(tips)
    }

    fun getChangeFolderItem(): DebugItemData {
        return getDebugItem(
            "<font color ='#3AC8EF'><b>点击切换查看的音频目录</b></font>"
        ) { FileSelectTools.openFileSelect(this@DebugWAVListFragment, folder) }
    }

    fun getChangeAutoPlayItem(): DebugItemData {
        return getDebugItem(
            "<font color ='#3AC8EF'><b>点击切换识别时是否播放，当前：$isPlay</b></font>"
        ) {
            isPlay = !isPlay
            mListLiveData.refresh()
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
                LoggerFile.getH5LogHeader("<title>音频查看</title>\n") + logHeader + LoggerFile.getH5Sort() + LoggerFile.getH5Content(),
                true
            )
            processAudioList(logFile)
        }
    }

    fun playAudioData(data: AudioData) {
        if (mAudioPLayerManager.isRunning) {
            mAudioPLayerManager.stopAll(true)
        }
        mAudioPLayerManager.play(data.filePath)
    }

    open fun getHeader(): ArrayList<CardBaseModule> {
        val data = ArrayList<CardBaseModule>().apply {
            getTips()?.let {
                add(it)
            }
            add(getChangeFolderItem())
            add(getChangeAutoPlayItem())
            add(
                getProcessAudioList(
                    getLogFile(),
                    "<div style=\"width: 100%;\">本地音频批量处理结果：<BR>文件目录：${
                        folder.replace(
                            "/",
                            " / "
                        )
                    } </div>\n"
                )
            )
        }
        return data
    }

    open fun getFileItem(file: File): AudioData {
        return AudioData(file.absolutePath)
    }

    open fun filterFile(filePath: String): Boolean {
        return FileUtils.checkFileExist(filePath)
    }

    open fun getFileList(): List<File> {
        File(folder).let {
            if (it.isFile) {
                it.parentFile
            } else {
                it
            }
        }.let { file ->
            return SearchFileUtils.search(file, arrayOf(".wav"))
                .filter { filterFile(it.absolutePath) }.sortedByDescending { it.lastModified() }

        }
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        val data = ArrayList<CardBaseModule>().apply {
            addAll(getHeader())
        }
        getFileList().forEach { item ->
            data.add(getFileItem(item))
        }
        return data
    }

    open fun itemClickAction(data: AudioData, play: Boolean) {
        if (play) {
            playAudioData(data)
        }
        Log.i(TAG, data.toString())
    }

    open fun audioIconClickAction(data: AudioData) {
        mAudioPLayerManager.play(data.filePath)
    }

    open fun itemLongClickAction(audioData: AudioData, index: Int) {
        DialogUtils.showConfirmDialog(activity!!,
            "删除文件",
            "确认要删除" + FileUtils.getFileName(audioData.filePath) + "么？",
            true,
            object : OnDialogListener {
                override fun onPositiveClick() {
                    FileUtils.deleteFile(audioData.filePath)
                    mAdapter.remove(index)
                    mAdapter.notifyItemRemoved(index)
                }

                override fun onNegativeClick() {

                }

                override fun onCancel() {

                }
            })
    }

    open fun audioIconLongClickAction(audioData: AudioData) {
        FileUtils.sendFile(activity!!, audioData.filePath)
    }

    open fun processAudioList(logFile: String) {
        val dialog = LoadingDialog(activity!!)
        dialog.show("开始处理……")
        ThreadManager.getInstance().start {
            getFileList().let {
                val num = it.size
                it.forEachIndexed { index, file ->
                    ThreadManager.getInstance().runOnUIThread {
                        dialog.show("共 $num 个音频，正在处理第 ${index + 1} 个……")
                    }
                    processAudioData(logFile, file.absolutePath)
                    Thread.sleep(1000L)
                }

                ThreadManager.getInstance().runOnUIThread {
                    dialog.dismiss()
                    showH5Log(getLogFile())
                }
            }
        }
    }

    open fun processAudioData(logFile: String, filePath: String) {
        LoggerFile.logH5(
            logFile, "", LoggerFile.getAudioH5LogData(filePath, "audio/wav")
        )
        val file = File(filePath)
        val waveFileReader = WaveFileReader(filePath)
        LoggerFile.logH5(
            getLogFile(), "", "文件大小：" + if (waveFileReader.isSuccess) {
                val fileLength = "文件大小：" + getFileLength(file.length())
                fileLength + "，" + waveFileReader.toShowString()
            } else {
                "音频文件异常，解析失败，请检查音频格式"
            }
        )
    }
}
