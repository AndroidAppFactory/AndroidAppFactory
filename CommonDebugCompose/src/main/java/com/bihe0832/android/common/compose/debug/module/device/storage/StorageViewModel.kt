package com.bihe0832.android.common.compose.debug.module.device.storage

import android.text.TextUtils
import com.bihe0832.android.common.compose.common.CommonActionViewModel
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.utils.apk.AppStorageUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/8/1.
 * Description: Description
 *
 */

data class DebugStorageUiState(
    val needSort: Boolean = true,
    val showFile: Boolean = true,
    val onlyFile: Boolean = true,
    val folder: String = "",
    val dataList: Map<String, Long> = emptyMap()
)

class StorageViewModel : CommonActionViewModel() {

    private val _storageState: MutableStateFlow<DebugStorageUiState> =
        MutableStateFlow(DebugStorageUiState())
    val storageUiState: StateFlow<DebugStorageUiState> = _storageState

    init {
        refresh()
    }

    fun changeFolder(folder: String) {
        _storageState.value = _storageState.value.copy(
            folder = folder
        )
        refresh()
    }

    fun toggleOnlyFile() {
        _storageState.value = _storageState.value.copy(
            onlyFile = !_storageState.value.onlyFile
        )
        refresh()
    }

    fun toggleShowFile() {
        _storageState.value = _storageState.value.copy(
            showFile = !_storageState.value.showFile
        )
        refresh()
    }

    fun toggleNeedSort() {
        _storageState.value = _storageState.value.copy(
            needSort = !_storageState.value.needSort
        )
        refresh()
    }

    override fun refresh() {
        fetchData()
    }

    override fun fetchData() {
        try {
            val context = ZixieContext.applicationContext ?: return
            val data = if (TextUtils.isEmpty(_storageState.value.folder)) {
                AppStorageUtil.getCurrentAppFolderSizeList(
                    context,
                    FileUtils.SPACE_MB.toInt(),
                    _storageState.value.showFile,
                    _storageState.value.needSort
                )
            } else {
                AppStorageUtil.getFolderSizeList(
                    File(_storageState.value.folder),
                    0,
                    _storageState.value.showFile,
                    _storageState.value.needSort
                )
            }
            _storageState.value = _storageState.value.copy(dataList = data)
            loadFinished()
        } catch (e: Exception) {
            // 可以添加错误处理或日志记录
            e.printStackTrace()
        }
    }
}