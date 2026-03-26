package com.bihe0832.android.common.message.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.lib.utils.time.DateUtil
import java.text.SimpleDateFormat
import java.util.Locale
import com.bihe0832.android.lib.aaf.res.R as ResR

/**
 * 消息列表项 Compose 组件
 *
 * 对应 View 体系中的 MessageItemHolder + message_item_layout.xml。
 * 包含：红点（未读标记）、消息图标、标题、时间、右箭头。
 * 支持左滑删除（SwipeToDismissBox）。
 *
 * @param messageInfoItem 消息数据
 * @param onItemClick 点击消息回调
 * @param onDelete 删除消息回调
 *
 * @author zixie code@bihe0832.com
 * Created on 2026/3/26.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageItemCompose(
    messageInfoItem: MessageInfoItem,
    onItemClick: (MessageInfoItem) -> Unit = {},
    onDelete: (MessageInfoItem) -> Unit = {},
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete(messageInfoItem)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            // 左滑露出的删除背景
            val backgroundColor by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Color.Red
                } else {
                    Color.Transparent
                },
                label = "deleteBackground"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "删除",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    ) {
        // 消息内容区域
        MessageItemContent(
            messageInfoItem = messageInfoItem,
            onItemClick = onItemClick
        )
    }
}

/**
 * 消息内容区域（不含滑动删除）
 */
@Composable
private fun MessageItemContent(
    messageInfoItem: MessageInfoItem,
    onItemClick: (MessageInfoItem) -> Unit = {},
) {
    val titleColor = MaterialTheme.colorScheme.onSurface
    val timeColor = MaterialTheme.colorScheme.onSurfaceVariant
    val bgColor = MaterialTheme.colorScheme.surface
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onItemClick(messageInfoItem) }
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 红点（未读标记）
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (!messageInfoItem.hasRead()) Color.Red else Color.Transparent
                    )
            )

            // 消息图标
            Icon(
                imageVector = ImageVector.vectorResource(ResR.drawable.icon_message),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(18.dp),
                tint = titleColor
            )

            // 标题
            Text(
                text = messageInfoItem.title ?: "",
                color = titleColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .weight(1f)
            )

            // 时间
            Text(
                text = formatMessageTime(messageInfoItem.createDate),
                color = timeColor,
                fontSize = 10.sp,
            )

            // 右箭头
            Icon(
                imageVector = ImageVector.vectorResource(ResR.drawable.icon_right_go),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(16.dp),
                tint = titleColor
            )
        }

        // 底部分割线
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.BottomCenter),
            color = dividerColor
        )
    }
}

/**
 * 格式化消息时间
 *
 * 将 "yyyyMMddHHmm" 格式的时间字符串转为 "yyyy-MM-dd HH:mm" 格式
 */
private fun formatMessageTime(createDate: String?): String {
    if (createDate.isNullOrEmpty()) return ""
    return try {
        val time =
            SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault()).parse(createDate)?.time ?: 0L
        DateUtil.getDateEN(time, "yyyy-MM-dd HH:mm")
    } catch (e: Exception) {
        createDate
    }
}

// ==================== 预览函数 ====================

/**
 * 预览：未读消息
 */
@Preview(name = "消息项 - 未读", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewMessageItemUnread() {
    val item = MessageInfoItem().apply {
        title = "这是一条未读的消息通知"
        createDate = "202603261200"
        setHasRead(false)
    }
    MessageItemContent(messageInfoItem = item)
}

/**
 * 预览：已读消息
 */
@Preview(name = "消息项 - 已读", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewMessageItemRead() {
    val item = MessageInfoItem().apply {
        title = "这是一条已读的消息通知"
        createDate = "202603251800"
        setHasRead(true)
    }
    MessageItemContent(messageInfoItem = item)
}

/**
 * 预览：长标题消息
 */
@Preview(name = "消息项 - 长标题", showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewMessageItemLongTitle() {
    val item = MessageInfoItem().apply {
        title = "这是一条非常非常非常非常非常非常非常非常非常非常长的消息标题，应该被截断显示"
        createDate = "202603241000"
        setHasRead(false)
    }
    MessageItemContent(messageInfoItem = item)
}
