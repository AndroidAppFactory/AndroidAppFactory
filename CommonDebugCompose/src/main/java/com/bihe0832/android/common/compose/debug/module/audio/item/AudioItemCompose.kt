package com.bihe0832.android.common.compose.debug.module.audio.item

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.lib.aaf.res.R as ResR

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/6/27.
 * Description: Description
 *
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioItemCompose(
    title: String,
    description: String,
    showRecognizeText: Boolean = false,
    recognizeText: String = "",
    event: ((AudioItemEvent) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(ResR.drawable.icon_file_type_audio),
            contentDescription = null,
            modifier = Modifier
                .size(42.dp)
                .combinedClickable(onClick = {
                    event?.invoke(AudioItemEvent.IconClick)
                }, onLongClick = {
                    event?.invoke(AudioItemEvent.IconLongClick)
                }),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .combinedClickable(onClick = {
                    event?.invoke(AudioItemEvent.ContentClick)
                }, onLongClick = {
                    event?.invoke(AudioItemEvent.ContentLongClick)
                }),
        ) {
            Text(
                text = title,
                color = colorResource(id = ResR.color.textColorSecondary),
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically)

            )
            // 描述文本
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                lineHeight = 2.em,
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(top = 2.dp)
            )

            // 识别文本（条件显示）
            if (showRecognizeText) {
                Text(
                    text = recognizeText,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .wrapContentHeight()
                )
            }
        }
    }
}

// 预览函数
@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun AudioItemComposePreview() {
    DebugContent {
        AudioItemCompose(
            title = "XXXX.wav",
            description = "1.32 MB",
            showRecognizeText = true,
            recognizeText = "已识别"
        )
        HorizontalDivider(
            modifier = Modifier
                .height(1.dp)
                .background(Color.Red)
        )
        AudioItemCompose(
            title = "XXXX.wav",
            description = "已识别已识别已识别已识别已识别已识别已识别已识别已识别已识别已识别已识别已识别已识别已识别已识别已识别已识别已识别已识别已识别已识别",
            showRecognizeText = true,
            recognizeText = "已识别"
        )
        HorizontalDivider(
            modifier = Modifier
                .height(1.dp)
                .background(Color.Red)
        )
        AudioItemCompose(
            title = "XXXX.wav",
            description = "1.32 MB",
            showRecognizeText = false,
            recognizeText = ""
        )
    }

}