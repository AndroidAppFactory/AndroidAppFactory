package com.bihe0832.android.common.compose.debug.module.device.storage


import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bihe0832.android.common.compose.debug.DebugBaseComposeActivity
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.compose.debug.item.LittleDebugItem
import com.bihe0832.android.common.compose.debug.item.LittleDebugTips
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.ui.activity.CommonContent
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.utils.apk.AppStorageUtil
import java.io.File

open class DebugCurrentStorageActivity : DebugBaseComposeActivity() {

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                val context = LocalContext.current
                val viewModel: DebugStorageViewModel = viewModel()
                val storageUiState by viewModel.storageUiState.collectAsState()
                val state = viewModel.uiState.collectAsStateWithLifecycle()

                val appSizeInfo = appSize(context)
                LaunchedEffect(storageUiState) {
                    ZixieContext.showToast(appSizeInfo)
                }
                CommonContent(
                    viewModel = viewModel,
                    state = state,
                    dataSize = storageUiState.dataList.size
                ) {
                    DebugContent {
                        LittleDebugTips(appSizeInfo)
                        StorageDataList(viewModel, storageUiState)
                    }
                }
            }
        }
    }

    @Composable
    fun StorageDataList(
        viewModel: DebugStorageViewModel, uiState: DebugStorageUiState
    ) {
        StorageControlPanel(viewModel, uiState)
        StorageDataItems(uiState)
    }

    fun appSize(context: Context): String {
        return buildString {
            append(
                "当前应用共使用：<b>${
                    FileUtils.getFileLength(
                        AppStorageUtil.getCurrentAppSize(
                            context
                        )
                    )
                }</b>"
            )
            append(
                "，其中APK占用：<b>${
                    FileUtils.getFileLength(
                        AppStorageUtil.getCurrentApplicationSize(
                            context
                        )
                    )
                }</b>"
            )
            append(
                "，内部存储占用：<b>${
                    FileUtils.getFileLength(
                        AppStorageUtil.getCurrentAppDataSize(
                            context
                        )
                    )
                }</b>"
            )
            append(
                "，外部存储占用：<b>${
                    FileUtils.getFileLength(
                        AppStorageUtil.getCurrentAppExternalDirSize(
                            context
                        )
                    )
                }</b>"
            )
        }
    }

    @Composable
    private fun StorageControlPanel(
        viewModel: DebugStorageViewModel, uiState: DebugStorageUiState
    ) {
        Column {
            LittleDebugTips("<b>点击切换文件夹是否按大小排列，当前：${uiState.needSort}</b>", {
                viewModel.toggleNeedSort()
            }, null)
            LittleDebugTips("<b>点击切换是否展示文件大小，当前：${uiState.showFile}</b>", {
                viewModel.toggleShowFile()
            }, null)
            LittleDebugTips("<b>点击切换是否仅展示文件大小，当前：${uiState.onlyFile}</b>", {
                viewModel.toggleOnlyFile()
            }, null)
        }
    }

    @Composable
    private fun StorageDataItems(uiState: DebugStorageUiState) {
        val context = LocalContext.current
        uiState.dataList.forEach { entry ->
            if (!uiState.onlyFile || File(entry.key).isFile) {
                FolderInfoItem(context, entry)
            }
        }
    }

    @Composable
    private fun FolderInfoItem(context: Context, entry: Map.Entry<String, Long>) {
        val itemContent = remember(entry.key, context.packageName) {
            "${
                entry.key.replace(context.packageName, "包名")
            } ：<b>${FileUtils.getFileLength(entry.value)}</b>"
        }
        LittleDebugItem(
            content = itemContent, click = { showInfoWithHTML("应用调试信息", itemContent) }, null
        )
    }
}