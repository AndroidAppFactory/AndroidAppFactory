package com.bihe0832.android.common.list.compose

import androidx.activity.viewModels
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.bihe0832.android.common.compose.common.CommonActionEvent
import com.bihe0832.android.common.compose.common.activity.CommonComposeActivity
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.list.compose.mvi.CommonListEffect
import com.bihe0832.android.common.list.compose.mvi.CommonListViewModel

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/3.
 * Description: Description
 *
 */

open class CommonComposeListActivity : CommonComposeActivity() {

    protected val mCommonListViewModel by viewModels<CommonListViewModel>()

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                handleCommonListViewEffect()
                CommonRefreshList(mCommonListViewModel) {
                    LazyColumn {
                        items(100) { index ->
                            Text("Item $index")
                        }
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    override fun ActivityRootContentRenderPreview() {
        getActivityRootContentRender().Content()
    }

    protected fun handleCommonListViewEffect() {
        lifecycleScope.launchWhenStarted {
            mCommonListViewModel.effect.collect {
                when (it) {
                    is CommonListEffect.Loading -> {
                        mCommonActionViewModel.sendEvent(CommonActionEvent.SimpleLoadingStart)
                    }

                    is CommonListEffect.LoadingSuccess -> {
                        mCommonActionViewModel.sendEvent(CommonActionEvent.LoadingFinished)
                    }

                    else -> {

                    }
                }
            }
        }
    }
}