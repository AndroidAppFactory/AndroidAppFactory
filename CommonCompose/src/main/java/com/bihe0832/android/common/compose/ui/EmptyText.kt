package com.bihe0832.android.common.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bihe0832.android.framework.R

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/2.
 * Description: Description
 *
 */

@Composable
fun EmptyText(desc: String, colorP: Color, onClick: (() -> Unit)? = null) {
    Box(

        modifier = Modifier
            .fillMaxSize()
            .background(colorP)
            .clickable {
                if (onClick != null) {
                    onClick()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            text = desc,
            color = colorResource(R.color.textColorPrimary),
            fontWeight = FontWeight.Black,
            fontSize = dpToSp(16.dp),
            textAlign = TextAlign.Center
        )
    }
}
