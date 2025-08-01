package com.bihe0832.android.common.compose.debug.log.item;

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.log.DebugLogComposeActivity
import com.bihe0832.android.framework.file.AAFFileTools
import com.bihe0832.android.framework.router.showFileContent
import com.bihe0832.android.lib.file.select.FileSelectTools


@Preview
@Composable
fun LogItemPreView() {
    val dataList = mutableListOf<DebugLogComposeActivity.LogInfo>().apply {
        for (i in 1..10) {
            add(DebugLogComposeActivity.LogInfo("路由跳转", ""))
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(dataList) { item ->
            LogItem(item.title, item.path)
        }
    }

}


interface ItemOnClickListener {

    companion object {
        const val TYPE_OPEN: Int = 1
        const val TYPE_SEND: Int = 2
    }

    fun onClick(title: String, path: String, type: Int)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogItem(
    text: String,
    path: String,
    sort: Boolean = false,
    showLine: Boolean = true,
    showAction: Boolean = true,
    click: ItemOnClickListener? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column {
            Text(
                text = AnnotatedString.fromHtml(text),
                modifier = Modifier
                    .combinedClickable(onClick = {
                        if (click != null) {
                            click.onClick(text, path, ItemOnClickListener.TYPE_OPEN)
                        } else {
                            try {
                                showFileContent(path, sort, showLine)
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
                    .padding(top = 6.dp, bottom = 6.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 12.sp
            )
            if (showAction) {
                Row {
                    Box(
                        modifier = Modifier
                            .combinedClickable(onClick = {
                                if (click != null) {
                                    click.onClick(text, path, ItemOnClickListener.TYPE_OPEN)
                                } else {
                                    try {
                                        showFileContent(path, sort, showLine)
                                    } catch (e: java.lang.Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            })
                            .padding(start = 4.dp, end = 2.dp, bottom = 6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                            .weight(1.0f),
                    ) {
                        Text(
                            text = "查看",
                            modifier = Modifier
                                .padding(4.dp)
                                .align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .combinedClickable(onClick = {
                                if (click != null) {
                                    click.onClick(text, path, ItemOnClickListener.TYPE_SEND)
                                } else {
                                    try {
                                        AAFFileTools.sendFile(path)
                                    } catch (e: java.lang.Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            })
                            .padding(start = 2.dp, end = 4.dp, bottom = 6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                            .weight(1.0f),
                    ) {
                        Text(
                            text = "发送",
                            modifier = Modifier
                                .padding(4.dp)
                                .align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

}


@Composable
fun DebugLogComposeActivity.GetLogPathItem(path: String) {
    DebugItem("日志路径：<BR><small>${path}</small>") {}
}

@Composable
fun DebugLogComposeActivity.GetSendLogItem(activity: Activity, path: String) {
    DebugItem("选择并发送日志") {
        isView = false
        FileSelectTools.openFileSelect(activity, path)
    }
}

@Composable
fun DebugLogComposeActivity.GetOpenLogItem(activity: Activity, path: String) {
    return DebugItem("选择并查看日志") {
        isView = true
        FileSelectTools.openFileSelect(activity, path)
    }
}

