package com.bihe0832.android.common.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.common.compose.R
import com.bihe0832.android.common.compose.state.aafStringResource

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/18.
 * Description: Description
 *
 */
@Preview
@Composable
fun EmptyView(
    message: String = aafStringResource(R.string.com_bihe0832_loading_empty),
    colorP: Color = Color(0xFF0B0E15),
    iconSize: Int = 100,
    textSize: Int = 16,
    textColor: Color = Color(0xFFFFFFFF),
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorP)

            .clickable {
                if (onClick != null) {
                    onClick()
                }
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.icon_empty),
            contentDescription = "空数据",
            modifier = Modifier.size(iconSize.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = message,
            color = textColor,
            fontWeight = FontWeight(400),
            fontSize = textSize.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height((iconSize * 0.5).dp))
    }
}