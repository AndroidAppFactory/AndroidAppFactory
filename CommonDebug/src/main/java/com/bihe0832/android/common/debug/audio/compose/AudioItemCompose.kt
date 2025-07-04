package com.bihe0832.android.common.debug.audio.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.common.compose.R

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/6/27.
 * Description: Description
 *
 */

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
            .padding(vertical = 8.dp)
            .padding(start = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 描述
        Text(text = description,
            color = colorResource(id = R.color.textColorSecondary),
            fontSize = 12.sp,
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable {
                    event?.invoke(AudioItemEvent.IconClick)
                })

    }
}

// 预览函数
@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun AudioItemComposePreview() {
    AudioItemCompose(
        title = "XXXX.wav",
        description = "1.32 MB",
        showRecognizeText = true,
        recognizeText = "已识别"
    )
}