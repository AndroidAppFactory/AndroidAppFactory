package com.bihe0832.android.common.list.compose.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bihe0832.android.common.compose.ui.utils.dpToSp
import com.bihe0832.android.common.list.compose.R

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/18.
 * Description: Description
 *
 */
@Preview
@Composable
fun ListMoreDataEmptyView(
    message: String = stringResource(R.string.com_bihe0832_load_more_end),
    colorP: Color = colorResource(R.color.windowBackground),
    textSize: Dp = 12.dp,
    textColor: Color = colorResource(R.color.textColorPrimary)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorP),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            text = message,
            color = textColor,
            fontWeight = FontWeight.Black,
            fontSize = dpToSp(textSize),
            textAlign = TextAlign.Center
        )
    }


}