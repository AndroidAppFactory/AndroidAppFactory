package com.bihe0832.android.common.debug.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.framework.ZixieContext

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/4.
 * Description: Description
 *
 */

@Preview
@Composable
fun DebugItemPreView() {
    Column {
        DebugItem(text = "DebugItem 完整样式",
            bgColor = Color.Green,
            textColor = Color.Red,
            click = { ZixieContext.showToast("DebugItem 单击") },
            doubleClick = { ZixieContext.showToast("DebugItem 双击") },
            longClick = { ZixieContext.showToast("DebugItem 长按") }
        )
        DebugItem(text = "DebugItem 完整样式",
            Color.Red,
            click = { ZixieContext.showToast("DebugItem 单击") }
        )
        DebugItem(text = "DebugItem 完整样式",
            click = { ZixieContext.showToast("DebugItem 单击") }
        )
        DebugTips(text = "DebugTips 完整样式", Color.Black, Color.White)
        DebugTips(text = "DebugTips 完整样式")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DebugItem(
    text: String,
    bgColor: Color,
    textColor: Color,
    click: (() -> Unit)?,
    doubleClick: (() -> Unit)?,
    longClick: (() -> Unit)?
) {
    return Column {
        Text(
            text = text,
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        click?.invoke()
                    }, onDoubleClick = doubleClick, onLongClick = longClick
                )
                .background(bgColor)
                .padding(12.dp)
                .fillMaxWidth(),
            color = textColor,
            fontSize = 12.sp
        )
        HorizontalDivider()
    }
}

@Composable
fun DebugItem(text: String, bgColor: Color, textColor: Color, click: () -> Unit) {
    return DebugItem(text, bgColor, textColor, click, null, null)
}

@Composable
fun DebugItem(text: String, color: Color, click: () -> Unit) {
    return DebugItem(text, MaterialTheme.colorScheme.surface, color, click, null, null)
}

@Composable
fun DebugItem(text: String, click: () -> Unit) {
    return DebugItem(text, MaterialTheme.colorScheme.onSurface, click)
}

@Composable
fun DebugTips(text: String, bgColor: Color, textColor: Color) {
    return DebugItem(text, bgColor, textColor) { }
}

@Composable
fun DebugTips(text: String) {
    return DebugTips(
        text, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary
    )
}