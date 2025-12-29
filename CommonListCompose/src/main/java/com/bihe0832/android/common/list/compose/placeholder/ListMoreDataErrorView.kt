package com.bihe0832.android.common.list.compose.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bihe0832.android.common.compose.R
import com.bihe0832.android.common.compose.ui.utils.dpToSp
import com.bihe0832.android.lib.aaf.res.R as ResR

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/18.
 * Description: Description
 *
 */
@Preview
@Composable
fun ListMoreDataErrorView(
    message: String = stringResource(ResR.string.com_bihe0832_load_failed),
    colorP: Color = colorResource(ResR.color.windowBackground),
    textSize: Dp = 12.dp,
    textColor: Color = colorResource(ResR.color.textColorSecondary),
    onRetry: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorP)
            .clickable { onRetry?.invoke() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(ResR.drawable.icon_info),
            contentDescription = "错误",
            tint = textColor,
            modifier = Modifier.size(textSize * 1.5f)
        )
        Text(
            text = message,
            color = textColor,
            fontWeight = FontWeight.Black,
            fontSize = dpToSp(textSize),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
    }
}
