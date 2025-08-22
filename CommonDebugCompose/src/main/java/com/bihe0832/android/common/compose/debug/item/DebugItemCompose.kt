package com.bihe0832.android.common.compose.debug.item

import android.app.Activity
import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.bihe0832.android.common.compose.debug.DebugComposeItemManager
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterAction

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
            isLittle = false,
            click = { ZixieContext.showToast("DebugItem 单击") },
            doubleClick = { ZixieContext.showToast("DebugItem 双击") },
            longClick = { ZixieContext.showToast("DebugItem 长按") })
        DebugItem(text = "<strong>DebugItem 完整样式</strong>",
            bgColor = Color.Green,
            textColor = Color.Red,
            isLittle = true,
            click = { ZixieContext.showToast("DebugItem 单击") },
            doubleClick = { ZixieContext.showToast("DebugItem 双击") },
            longClick = { ZixieContext.showToast("DebugItem 长按") })
        DebugItem(text = "DebugItem 完整样式",
            Color.Red,
            click = { ZixieContext.showToast("DebugItem 单击") })
        DebugItem(text = "DebugItem 完整样式", click = { ZixieContext.showToast("DebugItem 单击") })
        DebugTips(text = "DebugTips 完整样式", Color.Black, Color.White)
        RouterItem(content = "zixie://main")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DebugItem(
    text: String,
    bgColor: Color,
    textColor: Color,
    isLittle: Boolean,
    click: ((context: Context) -> Unit)?,
    doubleClick: ((context: Context) -> Unit)?,
    longClick: ((context: Context) -> Unit)?
) {
    val context = LocalContext.current
    return Column {
        Text(
            color = textColor,
            fontSize = if (isLittle) {
                10.sp
            } else {
                12.sp
            },
            text = AnnotatedString.fromHtml(text),
            modifier = Modifier
                .combinedClickable(onClick = {
                    click?.invoke(context)
                }, onDoubleClick = {
                    doubleClick?.invoke(context)
                }, onLongClick = {
                    longClick?.invoke(context)
                })
                .background(bgColor)
                .padding(
                    horizontal = 16.dp, vertical = if (isLittle) {
                        10.dp
                    } else {
                        14.dp
                    }
                )
                .fillMaxWidth()
        )
        HorizontalDivider()
    }
}

@Composable
fun DebugItem(text: String, bgColor: Color, textColor: Color, click: (context: Context) -> Unit) {
    return DebugItem(text, bgColor, textColor, isLittle = false, click, null, null)
}

@Composable
fun DebugItem(text: String, color: Color, click: (context: Context) -> Unit) {
    DebugItem(text, MaterialTheme.colorScheme.surface, color, isLittle = false, click, null, null)
}

@Composable
fun DebugItem(text: String, click: (context: Context) -> Unit) {
    DebugItem(text, MaterialTheme.colorScheme.onSurface, click)
}

@Composable
fun DebugComposeItem(text: String, key: String, composable: @Composable () -> Unit) {
    DebugItem(text) {
        DebugComposeItemManager.register(key, composable)
        DebugUtilsV2.startComposeActivity(ZixieContext.applicationContext!!, text, key)
    }
}

@Composable
fun DebugComposeFragmentItem(text: String, fragmentName: Class<out Fragment>) {
    DebugItem(text) {
        DebugUtilsV2.startFragmentActivity(
            ZixieContext.applicationContext!!,
            text,
            fragmentName.name
        )
    }
}

@Composable
fun DebugComposeActivityItem(text: String, activity: Class<out Activity>) {
    DebugItem(text) {
        DebugUtilsV2.startActivityWithException(it, activity)
    }
}

@Composable
fun DebugTips(text: String, bgColor: Color, textColor: Color) {
    DebugItem(text, bgColor, textColor) { }
}

@Composable
fun DebugTips(text: String) {
    DebugTips(
        text, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary
    )
}

@Composable
fun DebugTips(text: String, click: (context: Context) -> Unit) {
    DebugItem(
        text, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary, click
    )
}

@Composable
fun Debuginfo(text: String) {
    DebugItem(text) { context ->
        DebugUtilsV2.showInfo(context, "应用调试信息", text)
    }
}


@Composable
fun LittleDebugTips(
    content: String,
    click: ((context: Context) -> Unit)? = null,
    longClick: ((context: Context) -> Unit)? = null
) {
    DebugItem(
        text = content,
        bgColor = MaterialTheme.colorScheme.secondary,
        textColor = MaterialTheme.colorScheme.onSecondary,
        isLittle = true,
        click = click,
        doubleClick = null,
        longClick = longClick
    )
}

@Composable
fun LittleDebugItem(
    content: String,
    click: ((context: Context) -> Unit)? = null,
    longClick: ((context: Context) -> Unit)? = null
) {
    DebugItem(
        text = content,
        bgColor = MaterialTheme.colorScheme.surface,
        textColor = MaterialTheme.colorScheme.onSurface,
        isLittle = true,
        click = click,
        doubleClick = null,
        longClick = longClick
    )
}

@Composable
fun RouterItem(content: String) {
    LittleDebugItem(content = content, click = { context ->
        RouterAction.openFinalURL(content)
    }, longClick = { context ->
        DebugUtilsV2.showInfo(context, "复制并分享路由地址", content)
    })
}