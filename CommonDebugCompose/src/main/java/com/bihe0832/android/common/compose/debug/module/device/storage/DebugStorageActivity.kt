/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.common.compose.debug.module.device.storage

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.compose.debug.item.LittleDebugItem
import com.bihe0832.android.common.compose.debug.item.LittleDebugTips
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.ui.activity.CommonContent
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.file.select.FileSelectTools

class DebugStorageActivity : DebugCurrentStorageActivity() {

    private var folder = AAFFileWrapper.getFolder()
    private val viewModel: StorageViewModel = StorageViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        folder = intent.getStringExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_URL)
            ?: AAFFileWrapper.getFolder()
    }

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                val context = LocalContext.current
                val storageUiState by viewModel.storageUiState.collectAsState()
                val appSizeInfo = appSize(context)
                val state = viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.changeFolder(folder)
                }

                LaunchedEffect(storageUiState) {
                    ZixieContext.showToast(appSizeInfo)
                }
                CommonContent(
                    viewModel = viewModel,
                    state = state,
                    dataSize = storageUiState.dataList.size
                ) {
                    DebugContent {
                        Row {
                            Box(modifier = Modifier.weight(1f)) {
                                LittleDebugTips("<b>查看当前应用的目录</b>", {
                                    startActivityWithException(DebugCurrentStorageActivity::class.java)
                                })
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                LittleDebugTips(
                                    "<b>点击切换要查看的文件目录</b>",
                                    {
                                        FileSelectTools.openFileSelect(
                                            this@DebugStorageActivity,
                                            AAFFileWrapper.getFolder()
                                        )
                                    })
                            }
                        }
                        if (!TextUtils.isEmpty(folder)) {
                            LittleDebugItem("当前目录：$folder") {}
                        }
                        LittleDebugTips(appSizeInfo)
                        StorageDataList(viewModel, storageUiState)
                    }
                }
            }
        }
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