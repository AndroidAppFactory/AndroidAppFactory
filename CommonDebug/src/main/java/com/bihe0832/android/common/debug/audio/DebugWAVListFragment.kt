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
import com.bihe0832.android.common.debug.item.DebugTipsData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.lib.audio.player.block.AudioPLayerManager
import java.io.File

open class DebugWAVListFragment : DebugEnvFragment() {
    val TAG = this.javaClass.simpleName
    private val mAudioPLayerManager by lazy { AudioPLayerManager() }
    private var folder = AAFFileWrapper.getMediaTempFolder()
    private var isPlay = false
    override fun getDataList(): ArrayList<CardBaseModule> {

        val data = ArrayList<CardBaseModule>().apply {
            val tips = "1. <b><font color='#3AC8EF'>点击</font>图标</b>，播放音频，<b><font color='#3AC8EF'>点击</font>标题和内容</b>，${
                        if (isPlay) {
                            "播放音频并识别音频内容"
                        } else {
                            "识别音频内容"
                        }
                    }<BR>" +
                    "2. <b><font color='#3AC8EF'>长按</font>图标</b>，可以删除音频，<b><font color='#3AC8EF'>长按</font>标题和内容</b>，可以发送音频"

            add(DebugTipsData(tips))
            add(
                DebugItemData(
                    "<font color ='#3AC8EF'><b>点击切换识别时是否播放，当前：$isPlay</b></font>"
                ) {
                    isPlay = !isPlay
                    mListLiveData.refresh()
                }
            )
            add(
                DebugItemData(
                    "<font color ='#3AC8EF'><b>点击切换要查看的音频目录</b></font>"
                ) { FileSelectTools.openFileSelect(this@DebugWAVListFragment, folder) }
            )
        }
        File(folder).let {
            if (it.isFile) {
                it.parentFile
            } else {
                it
            }
        }.let { file ->
            SearchFileUtils.search(file, arrayOf(".wav")).sortedByDescending { it.lastModified() }.forEach { item ->
                AudioData(item.absolutePath).apply {
//                    palyAndRecognise(this, false)
                }.let {
                    data.add(it)
                }
            }
        }
        return data
    }

    override fun getDataLiveData(): CommonListLiveData {
        return mListLiveData
    }

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


    override fun initView(view: View) {
        super.initView(view)
        mAdapter.apply {
            setOnItemChildClickListener { baseQuickAdapter, view, i ->
                (baseQuickAdapter.getItem(i) as AudioData?)?.let { audioData ->
                    when (view.id) {
                        R.id.audio_title, R.id.audio_desc -> {
                            palyAndRecognise(audioData, isPlay)
                            mAdapter.notifyItemChanged(i)
                        }

                        R.id.audio_icon -> {
                            mAudioPLayerManager.play(audioData.filePath)
                        }
                    }
                }
            }
            setOnItemChildLongClickListener { baseQuickAdapter, view, i ->
                (baseQuickAdapter.getItem(i) as AudioData?)?.let { audioData ->
                    when (view.id) {
                        R.id.audio_icon -> {
                            DialogUtils.showConfirmDialog(
                                activity!!,
                                "删除文件",
                                "确认要删除" + FileUtils.getFileName(audioData.filePath) + "么？",
                                true,
                                object : OnDialogListener {
                                    override fun onPositiveClick() {
                                        FileUtils.deleteFile(audioData.filePath)
                                        mAdapter.remove(i)
                                        mAdapter.notifyItemRemoved(i)
                                    }

                                    override fun onNegativeClick() {

                                    }

                                    override fun onCancel() {

                                    }
                                }
                            )

                        }

                        R.id.audio_title, R.id.audio_desc -> {
                            FileUtils.sendFile(activity!!, audioData.filePath)
                        }

                        else -> {}
                    }

                }
                return@setOnItemChildLongClickListener false
            }
        }
    }


    open fun palyAndRecognise(data: AudioData, play: Boolean) {
        if (play) {
            mAudioPLayerManager.play(data.filePath)
        }
        Log.i(TAG, data.toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FileSelectTools.FILE_CHOOSER && resultCode == RESULT_OK) {
            data?.extras?.getString(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, "")?.let { filePath ->
                folder = filePath
                mListLiveData.refresh()
            }
        }
    }
}
