/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.common.compose.debug.module.audio

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bihe0832.android.common.compose.common.CommonViewSideEffect
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.module.audio.process.AudioDataFactoryCallback
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.ui.activity.CommonContent
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.router.showH5File
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import java.io.File

class DebugAudioListWithProcessActivity : DebugAudioListActivity() {
    private var mDebugAudioDataFactory: DebugAudioDataFactory? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDebugAudioDataFactory = getAudioDataProcessInterface()

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
                        DebugTips(
                            "<font color ='#3AC8EF'><b>批量处理当前目录音频并记录</b></font>"
                        ) {
                            LoggerFile.initFile(
                                getLogFile(),
                                LoggerFile.getH5LogHeader("<title>本地音频批量处理结果展示</title>\n") + "<div><font color='#3AC8EF'>文件目录：</font><BR>" + "${
                                    folder.replace(packageName, " * ").replace("/", " / ")
                                }<BR>" + "<BR> </div>" + LoggerFile.getH5Content(),
                                true
                            )
                            mDebugAudioDataFactory?.processAudioList(
                                getLogFile(),
                                folder,
                                dataList.value.map { File(it.filePath) })
                        }
                        AudioList(viewModel, dataList)
                    }
                }
            }
        }
    }

    override fun handleEffect(context: Context) {
        lifecycleScope.launchWhenStarted {
            viewModel.effect.collect {
                when (it) {
                    CommonViewSideEffect.ClickLoading -> {
                        DialogUtils.showConfirmDialog(
                            this@DebugAudioListWithProcessActivity,
                            "要停止此处音频处理么？",
                            true,
                            object : OnDialogListener {
                                override fun onPositiveClick() {
                                    viewModel.hideLoading()
                                    mDebugAudioDataFactory?.forceStop()
                                }

                                override fun onNegativeClick() {

                                }

                                override fun onCancel() {

                                }


                            })
                    }
                }
            }
        }
    }

    open fun getAudioDataProcessInterface(): DebugAudioDataFactory {
        return DebugAudioDataFactory(getAudioDataFactoryCallback())
    }

    fun getAudioDataFactoryCallback(): AudioDataFactoryCallback {
        return object : AudioDataFactoryCallback {
            override fun onStart() {
                viewModel.setState {
                    copy(
                        isLoading = true,
                        isRefreshLoading = false,
                        loadingMsg = "开始处理……"
                    )
                }
            }

            override fun onProcess(current: Int, num: Int) {
                viewModel.setState {
                    copy(
                        isLoading = true,
                        isRefreshLoading = false,
                        loadingMsg = "共 $num 个音频，正在处理第 $current 个……"
                    )
                }
            }

            override fun onCancel() {
                viewModel.setState { copy(isLoading = false) }
            }

            override fun onComplete() {
                viewModel.setState { copy(isLoading = false) }
                showH5File(getLogFile())
            }
        }
    }

    open fun getLogFile(): String {
        return LoggerFile.getZixieFileLogPathByModule(
            "audio_process", ZixieContext.getLogFolder(), LoggerFile.TYPE_HTML
        )
    }


    override fun itemClickAction(data: AudioData, play: Boolean) {
        if (play) {
            playAudioData(data)
        }
    }
}