package com.bihe0832.android.common.compose.common.activity

import android.content.Context
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.bihe0832.android.common.compose.R
import com.bihe0832.android.common.compose.base.BaseComposeActivity
import com.bihe0832.android.common.compose.common.CommonActionEffect
import com.bihe0832.android.common.compose.common.CommonActionViewModel
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.ui.EmptyText
import com.bihe0832.android.common.compose.ui.activity.CommonActivityToolbarView
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.ui.dialog.impl.LoadingDialog
import java.util.Locale

open class CommonComposeActivity : BaseComposeActivity() {

    protected val mCommonActionViewModel by viewModels<CommonActionViewModel>()

    private var mLoadingDialog: LoadingDialog? = null

    open fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content(currentLanguage: Locale) {
                EmptyText(desc = "空白页面", colorP = MaterialTheme.colorScheme.surface)
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
        lifecycleScope.launchWhenStarted {
            mCommonActionViewModel.effect.collect {
                when (it) {
                    is CommonActionEffect.Loading -> {
                        if (mLoadingDialog == null) {
                            mLoadingDialog = LoadingDialog(context)
                        }
                        mLoadingDialog?.show(it.text)
                    }

                    is CommonActionEffect.LoadingFailed -> {
                        ZixieContext.showDebug("LoadingFailed:${it.errorCode} ${it.errorMsg}")
                        mLoadingDialog?.hide()
                    }

                    is CommonActionEffect.LoadingSuccess -> {
                        mLoadingDialog?.hide()
                    }

                    CommonActionEffect.LoadingFinished -> {
                        mLoadingDialog?.hide()

                    }
                }
            }
        }
    }

    override fun onDestroy() {
        mLoadingDialog?.dismiss()
        if (null != mLoadingDialog) {
            mLoadingDialog = null
        }
        super.onDestroy()
    }

    @Preview
    @Composable
    open fun ActivityRootContentRenderPreview() {
        getActivityRootContentRender().Content(Locale.CHINESE)
    }

}

