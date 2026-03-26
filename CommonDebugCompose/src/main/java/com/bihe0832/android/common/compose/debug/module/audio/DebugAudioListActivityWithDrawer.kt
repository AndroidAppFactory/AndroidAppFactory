/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.common.compose.debug.module.audio

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bihe0832.android.common.compose.debug.DebugBaseComposeActivity
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.item.LittleDebugTips
import com.bihe0832.android.common.compose.debug.module.audio.item.AudioItemCompose
import com.bihe0832.android.common.compose.debug.module.audio.item.AudioItemEvent
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.ui.activity.CommonContent
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.audio.wav.WaveFileReader
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.FileUtils.getFileLength
import com.bihe0832.android.lib.file.FileUtils.getFileName
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.utils.time.DateUtil
import com.bihe0832.lib.audio.player.block.AudioPLayerManager
import java.io.File

open class DebugAudioListActivity : DebugBaseComposeActivity() {

    protected var folder = AAFFileWrapper.getMediaTempFolder()
    protected val viewModel: DebugAudioListViewModel = DebugAudioListViewModel()
    protected val mAudioPLayerManager by lazy { AudioPLayerManager() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        folder = intent.getStringExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_URL)
            ?: AAFFileWrapper.getMediaTempFolder()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAudioPLayerManager.stopAll(true)
    }

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                val state = viewModel.uiState.collectAsStateWithLifecycle()
                val dataList = viewModel.dataList.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.changeFolder(folder)
                }
                CommonContent(
                    viewModel = viewModel, state = state, dataSize = dataList.value.size
                ) {
                    DebugContent {
                        AudioHeader()
                        AudioList(viewModel, dataList)
                    }
                }
            }
        }
    }

    @Composable
    fun AudioHeader() {
        val uiState by viewModel.audioListUiState.collectAsStateWithLifecycle()
        DebugTips(
            "1. <b><font color='#3AC8EF'>图标：<b>长按</b>，删除音频，</font>点击</b>播放音频<BR>" +
                    "2. <b><font color='#3AC8EF'>标题和内容：<b>长按</b>发送音频，</font>点击</b>，${
                        if (uiState.autoPlay) {
                            "播放音频并处理内容"
                        } else {
                            "处理音频内容"
                        }
                    } "
        )
        LittleDebugTips(
            content = "<font color ='#3AC8EF'><b>点击切换查看的音频目录</b></font>",
            click = {
                FileSelectTools.openFileSelect(this, folder)
            })
        LittleDebugTips(
            content = "<font color ='#3AC8EF'><b>点击切换识别时是否播放，当前：${uiState.autoPlay}</b></font>",
            click = {
                viewModel.togglePlay()
            })
    }

    @Composable
    protected fun AudioList(viewModel: DebugAudioListViewModel, dataList: State<List<AudioData>>) {
        val uiState by viewModel.audioListUiState.collectAsState()
        dataList.value.forEach { audio ->
            val file = File(audio.filePath)
            val title =
                getFileName(audio.filePath) + "  |  " + DateUtil.getDateEN(file.lastModified())
            val waveFileReader = WaveFileReader(file.absolutePath)
            val desc = if (waveFileReader.isSuccess) {
                "文件大小：" + getFileLength(file.length()) + "，" + waveFileReader.toShowString() + "，" + audio.amplitude
            } else {
                "音频文件异常，解析失败，请检查音频格式"
            }
            HorizontalDivider(
                modifier = Modifier
                    .height(1.dp)
                    .background(Color.Red)
            )
            AudioItemCompose(
                title,
                desc,
                !TextUtils.isEmpty(audio.recogniseResult),
                audio.recogniseResult ?: "",
            ) { audioItemEvent ->
                when (audioItemEvent) {
                    AudioItemEvent.IconClick -> {
                        playAudioData(audio)
                    }

                    AudioItemEvent.IconLongClick -> {
                        DialogUtils.showConfirmDialog(this@DebugAudioListActivity,
                            "删除文件",
                            "确认要删除" + getFileName(audio.filePath) + "么？",
                            true,
                            object : OnDialogListener {
                                override fun onPositiveClick() {
//                                        FileUtils.deleteFile(audio.filePath)
                                    viewModel.deleteAudio(audio)
                                }

                                override fun onNegativeClick() {

                                }

                                override fun onCancel() {

                                }
                            })
                    }

                    AudioItemEvent.ContentClick -> {
                        itemClickAction(audio, uiState.autoPlay)

                    }

                    AudioItemEvent.ContentLongClick -> {
                        FileUtils.sendFile(this@DebugAudioListActivity, audio.filePath)

                    }


                }

            }
        }
    }

    open fun itemClickAction(data: AudioData, play: Boolean) {
        if (play) {
            playAudioData(data)
        }
    }

    open fun playAudioData(data: AudioData) {
        if (mAudioPLayerManager.isRunning) {
            mAudioPLayerManager.stopAll(true)
        }
        mAudioPLayerManager.play(data.filePath)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FileSelectTools.FILE_CHOOSER && resultCode == RESULT_OK) {
            data?.extras?.getString(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, "")?.let { filePath ->
                folder = filePath
                viewModel.changeFolder(folder)
            }
        }
    }
}