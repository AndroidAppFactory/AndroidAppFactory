package com.bihe0832.android.common.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/18.
 * Description: Description
 *
 */
@Preview
@Composable
fun ErrorView(
    message: String = stringResource(R.string.com_bihe0832_load_failed),
    colorP: Color = colorResource(R.color.windowBackground),
    textSize: Dp = 14.dp,
    textColor: Color = colorResource(R.color.textColorSecondary),
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorP)
            .clickable { onRetry?.invoke() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.icon_info),
            contentDescription = "错误提示",
            tint = Color.LightGray,
            modifier = Modifier.size(textSize * 2)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = message,
            color = textColor,
            fontWeight = FontWeight.Black,
            fontSize = dpToSp(textSize),
            textAlign = TextAlign.Center
        )
    }
}
