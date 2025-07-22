package com.bihe0832.android.base.compose.debug.list

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.bihe0832.android.common.compose.common.CommonActionViewModel
import com.bihe0832.android.common.compose.state.LanguageItem
import com.bihe0832.android.common.compose.state.MultiLanguageState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/21.
 * Description: Description
 *
 */
class DebugOnceListViewModel : CommonActionViewModel() {
    private var index = 0

    private val _languageList = mutableStateListOf<LanguageItem>()
    val languageList: List<LanguageItem> get() = _languageList

    private fun refreshLanguages() {
        viewModelScope.launch {
            delay(5000) // 模拟网络延迟
            index++
            _languageList.clear()
            if (index % 2 == 0) {
                MultiLanguageState.getSupportLanguageList().let {
                    for (i in 1..8) {
                        _languageList.addAll(it)
                    }
                }
                loadFinished()
            } else {
                _languageList.clear()
                loadError("没有拿到任何语言文件")
            }

        }

    }

    override fun refresh() {
        refreshLanguages()
    }

    override fun fetchData() {
        refreshLanguages()
    }


}