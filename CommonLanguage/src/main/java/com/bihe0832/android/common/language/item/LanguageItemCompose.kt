package com.bihe0832.android.common.language.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
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
fun LanguageItemCompose(
    title: String,
    event: (() -> Unit)? = null,
    isSelected: Boolean = false,
    showDivider: Boolean = true,
) {
    Column(modifier = Modifier.wrapContentHeight()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable {
                    event?.invoke()
                }, verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            // 1. 标题文本（左侧）
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            // 2. 选择状态图标（右侧）
            if (isSelected) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_select_fill),
                    contentDescription = "",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
        if (showDivider) {

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}


// 预览函数
@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun AudioItemComposePreview() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LanguageItemCompose(title = "简体中文", isSelected = true, showDivider = true)
    }
}