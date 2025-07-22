package com.bihe0832.android.base.compose.debug.list.model

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.bihe0832.android.common.compose.ui.utils.VerticalSpacer
import com.bihe0832.android.common.list.compose.CommonComposeListActivity

/**
 * @author zixie code@bihe0832.com Created on 2025/2/17. Description: Description
 */
class DebugPageListActivity : CommonComposeListActivity<DataItem>() {

    private val model = DebugPageListViewModel()

    @Composable
    override fun getLazyPagingItems(): LazyPagingItems<DataItem> {
        return model.dataList.collectAsLazyPagingItems()
    }

    @Composable
    override fun GetComposeItem(index: Int, item: DataItem) {
        Column {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "$index name: ${item.title}",
                fontSize = 16.sp,
                textAlign = TextAlign.Start
            )
            VerticalSpacer(18)
        }
    }
}
