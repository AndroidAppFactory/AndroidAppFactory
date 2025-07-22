package com.bihe0832.android.common.list.compose

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.bihe0832.android.common.compose.common.activity.CommonComposeActivity
import com.bihe0832.android.common.compose.state.RenderState
import java.util.Locale

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/3.
 * Description: Description
 *
 */

abstract class CommonComposeListActivity<T : Any> : CommonComposeActivity() {

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content(currentLanguage: Locale) {
                val lazyUserItems = getLazyPagingItems()
                CommonRefreshList(enableRefresh = true,
                    enableLoadMore = true,
                    lazyPagingItems = lazyUserItems,
                    itemContent = object : (LazyListScope) -> Unit {
                        override fun invoke(p1: LazyListScope) {
                            p1.items(
                                count = lazyUserItems.itemCount, key = lazyUserItems.itemKey()
                            ) { index ->
                                val item = lazyUserItems[index]
                                if (item != null) {
                                    GetComposeItem(index, item)
                                }
                            }
                        }
                    })
            }
        }
    }

    @Composable
    abstract fun getLazyPagingItems(): LazyPagingItems<T>

    @Composable
    abstract fun GetComposeItem(index: Int, item: T)


    @Preview
    @Composable
    override fun ActivityRootContentRenderPreview() {
        getActivityRootContentRender().Content(Locale.CHINESE)
    }
}