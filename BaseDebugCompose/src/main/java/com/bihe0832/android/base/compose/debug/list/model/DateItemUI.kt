package com.bihe0832.android.base.compose.debug.list.model

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bihe0832.android.common.compose.ui.utils.VerticalSpacer
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.lib.request.URLUtils

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/24.
 * Description: Description
 *
 */
@Composable
fun DateItemUI(
    title: String,
    url: String,
    desc: String,
    time: String,
    hasRead: Boolean = false,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .weight(1f)
            .align(Alignment.CenterVertically)
            .padding(top = 18.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            .clickable {
                onClick?.invoke()
            }) {
            Row(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = if (URLUtils.isHTTPUrl(url)) {
                        url
                    } else {
                        ResR.drawable.icon_message
                    },
                    placeholder = painterResource(ResR.drawable.icon_message),
                    contentDescription = "",
                    modifier = Modifier.size(24.dp),
                    colorFilter = if (URLUtils.isHTTPUrl(url)) {
                        null
                    } else {
                        ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                // 1. 标题文本（左侧）
                Text(
                    text = title,
                    color = if (hasRead) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            VerticalSpacer(18)
            Text(
                text = desc,
                color = if (hasRead) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                },
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
            )
            VerticalSpacer(8)
            Text(
                text = time, color = if (hasRead) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }, fontSize = 12.sp, modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
            )
        }
        if (onDelete != null) {
            Image(contentDescription = "",
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        onDelete.invoke()
                    }
                    .padding(end = 16.dp)
                    .align(Alignment.CenterVertically) // 关键修改：添加垂直居中
                ,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
                painter = painterResource(ResR.drawable.icon_delete_fill))
        }
    }
}

@Composable
fun DateItemUI(
    item: DataItem,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    DateItemUI(
        item.title ?: "",
        "",
        item.summary ?: "",
        item.publishDate ?: "",
        item.isRead,
        onClick,
        onDelete
    )
}


// 预览函数
@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun AudioItemComposePreview() {
    Column(
        modifier = Modifier.background(Color(0xFF0B0E15))
    ) {
        DateItemUI(title = "消息标题消息标题消息标题消息标题消息标题消息标题消息标题消息标题",
            url = "",
            desc = "消息描述消息描述消息描述消息描述消息描述消息描述消息描述消息描述消息描述",
            time = "2025-06-27  14:32:15",
            onDelete = {})
        VerticalSpacer(8)
        DateItemUI(
            title = "消息标题消息标题消息标题消息标题消息标题消息标题消息标题消息标题",
            url = "",
            desc = "消息描述消息描述消息描述消息描述消息描述消息描述消息描述消息描述消息描述",
            time = "2025-06-27  14:32:15",
            hasRead = true
        )

    }

}