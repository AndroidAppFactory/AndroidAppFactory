package com.bihe0832.android.base.compose.debug.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bihe0832.android.app.language.LanguageActivity
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.ui.activity.CommonContent
import java.util.Locale

/**
 * @author zixie code@bihe0832.com Created on 2025/3/27.
 */
class DebugOnceListActivity : LanguageActivity() {

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content(currentLanguage: Locale) {
                val viewModel: DebugOnceListViewModel = viewModel()
                val languageList = rememberUpdatedState(viewModel.languageList)
                val state = viewModel.uiState.collectAsStateWithLifecycle()
                CommonContent(
                    viewModel = viewModel,
                    state = state,
                    dataSize = languageList.value.size
                ) {
                    LazyColumn {
                        items(languageList.value) {
                            getLanguageItemCompose(it, currentLanguage)
                        }
                    }
                }
            }
        }
    }

    @Composable
    override fun getTitleName(): String {
        return super.getTitleName() + "Debug"
    }
}

