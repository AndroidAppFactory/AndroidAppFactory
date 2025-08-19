package com.bihe0832.android.common.compose.debug.module.audio

import androidx.lifecycle.viewModelScope
import com.bihe0832.android.common.compose.common.CommonActionViewModel
import com.bihe0832.android.common.compose.debug.module.audio.process.SearchFileUtils
import com.bihe0832.android.lib.file.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/8/1.
 * Description: Description
 *
 */

data class AudioData(
    var filePath: String = "",
    var recogniseResult: String? = null,
    var amplitude: String = "最大振幅：未知"
)

data class DebugAudioListUiState(
    val autoPlay: Boolean = true,
    val folder: String = "",
)

class DebugAudioListViewModel : CommonActionViewModel() {

    private val _audioListState: MutableStateFlow<DebugAudioListUiState> =
        MutableStateFlow(DebugAudioListUiState())
    val audioListUiState: StateFlow<DebugAudioListUiState> = _audioListState

    private val _deletedAudio = MutableStateFlow<Set<String>>(emptySet())
    private val _recogniseText = MutableStateFlow<Set<AudioData>>(emptySet())
    private val _data = MutableStateFlow<List<AudioData>>(emptyList())


    val dataList: StateFlow<List<AudioData>> =
        combine(_data, _deletedAudio, _recogniseText) { audioList, deleteSet, recogniseSet ->
            audioList.map { audioItem ->
                // 更新识别结果
                recogniseSet.find { it.filePath == audioItem.filePath }?.let { data ->
                    audioItem.copy(recogniseResult = data.recogniseResult)
                } ?: audioItem // 直接返回原对象，避免冗余拷贝
            }.filter { audioItem ->
                !deleteSet.contains(audioItem.filePath) // 过滤已删除项
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // 确保初始值合法
        )


    fun changeFolder(folder: String) {
        _audioListState.value = _audioListState.value.copy(
            folder = folder
        )
        refresh()
    }

    fun togglePlay() {
        _audioListState.value = _audioListState.value.copy(
            autoPlay = !_audioListState.value.autoPlay
        )
    }

    override fun refresh() {
        fetchData()
    }

    open fun filterFile(filePath: String): Boolean {
        return FileUtils.checkFileExist(filePath)
    }

    override fun fetchData() {
        try {
            val folder = _audioListState.value.folder

            val file = File(folder).let {
                if (it.isFile) {
                    it.parentFile
                } else {
                    it
                }
            }
            val data =
                SearchFileUtils.search(file, arrayOf(".wav")).filter { filterFile(it.absolutePath) }
                    .sortedByDescending { it.lastModified() }
                    .map { AudioData().apply { this.filePath = it.absolutePath } }
            _data.value = data
            loadFinished()
        } catch (e: Exception) {
            // 可以添加错误处理或日志记录
            e.printStackTrace()
        }
    }

    fun deleteAudio(audio: AudioData) {
        viewModelScope.launch {
            try {
                val currentDeletedIds = _deletedAudio.value.toMutableSet()
                currentDeletedIds.add(audio.filePath)
                _deletedAudio.value = currentDeletedIds
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}