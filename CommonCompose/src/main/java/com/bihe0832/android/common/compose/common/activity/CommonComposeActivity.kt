package com.bihe0832.android.common.compose.common.activity

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bihe0832.android.common.compose.R
import com.bihe0832.android.common.compose.base.BaseComposeActivity
import com.bihe0832.android.common.compose.common.CommonActionViewModel
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.ui.EmptyView
import com.bihe0832.android.common.compose.ui.activity.CommonActivityToolbarView
import com.bihe0832.android.common.compose.ui.activity.CommonContent
import java.util.Locale

open class CommonComposeActivity : BaseComposeActivity() {

    open fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content(currentLanguage: Locale) {
                val viewModel: CommonActionViewModel = viewModel()
                val state = viewModel.uiState.collectAsStateWithLifecycle()
                CommonContent(
                    viewModel = viewModel, state = state, dataSize = 0
                ) {
                    EmptyView(message = "空白页面", colorP = MaterialTheme.colorScheme.surface)
                }
            }
        }
    }

    @Composable
    open fun getTitleName(): String {
        return stringResource(R.string.app_name)
    }

    override fun getActivityContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content(currentLanguage: Locale) {
                handleEffect(LocalContext.current)
                getToolBarRender(getContentRender()).Content(currentLanguage)
            }
        }
    }

    open fun getToolBarRender(contentRender: RenderState): RenderState {
        return object : RenderState {
            @Composable
            override fun Content(currentLanguage: Locale) {
                handleEffect(LocalContext.current)
                CommonActivityToolbarView(getTitleName(), content = {
                    contentRender.Content(currentLanguage)
                })
            }
        }
    }


    protected fun handleEffect(context: Context) {

    }

    @Preview
    @Composable
    open fun ActivityRootContentRenderPreview() {
        getActivityRootContentRender().Content(Locale.CHINESE)
    }

}

