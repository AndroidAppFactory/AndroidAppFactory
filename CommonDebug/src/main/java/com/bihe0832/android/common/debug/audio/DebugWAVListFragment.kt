/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.common.debug.audio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bihe0832.android.common.debug.R
import com.bihe0832.android.common.debug.audio.card.AudioData
import com.bihe0832.android.common.file.preview.ContentItemData
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.item.getTipsItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.lib.audio.player.block.AudioPLayerManager
import java.io.File

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/1/10.
 * Description: 查看本地的音频文件，支持播放音频
 *
 */

open class DebugWAVListFragment : DebugEnvFragment() {
    val TAG = this.javaClass.simpleName
    protected val mAudioPLayerManager by lazy { AudioPLayerManager() }
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

    override fun onDestroy() {
        super.onDestroy()
        mAudioPLayerManager.stopAll(true)
    }

    override fun parseBundle(bundle: Bundle, isOnCreate: Boolean) {
        super.parseBundle(bundle, isOnCreate)
        bundle.getString(RouterConstants.INTENT_EXTRA_KEY_WEB_URL, "").let {
            if (it.isNotBlank()) {
                folder = it
            }
        }
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

    open fun getTips(): ContentItemData? {
        val tips =
            "1. <b><font color='#3AC8EF'>点击</font>图标</b>，播放音频，<b><font color='#3AC8EF'>点击</font>标题和内容</b>，${
                if (isPlay) {
                    "播放音频并处理音频内容"
                } else {
                    "处理音频内容"
                }
            }<BR>" + "2. <b><font color='#3AC8EF'>长按</font>图标</b>，可以删除音频，<b><font color='#3AC8EF'>长按</font>标题和内容</b>，可以发送音频"

        return getTipsItem(tips)
    }

    fun getChangeFolderItem(): ContentItemData {
        return getDebugItem(
            "<font color ='#3AC8EF'><b>点击切换查看的音频目录</b></font>"
        ) { FileSelectTools.openFileSelect(this@DebugWAVListFragment, folder) }
    }

    fun getChangeAutoPlayItem(): ContentItemData {
        return getDebugItem(
            "<font color ='#3AC8EF'><b>点击切换识别时是否播放，当前：$isPlay</b></font>"
        ) {
            isPlay = !isPlay
            mListLiveData.refresh()
        }
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

    open fun getHeader(): ArrayList<CardBaseModule> {
        val data = ArrayList<CardBaseModule>().apply {
            getTips()?.let {
                add(it)
            }
            add(getChangeFolderItem())
            add(getChangeAutoPlayItem())
        }
        return data
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

    fun playAudioData(data: AudioData) {
        if (mAudioPLayerManager.isRunning) {
            mAudioPLayerManager.stopAll(true)
        }
        mAudioPLayerManager.play(data.filePath)
    }
}
