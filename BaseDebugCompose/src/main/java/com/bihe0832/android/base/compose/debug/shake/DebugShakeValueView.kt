package com.bihe0832.android.base.compose.debug.shake

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.base.compose.debug.R

@Preview
@Composable
fun DebugShakeValueView(
    speed: String = "",
    onStart: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(32.dp)
                .align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Image(
                    modifier = Modifier
                        .width(150.dp)
                        .align(Alignment.Center),
                    painter = painterResource(id = R.mipmap.icon),
                    contentDescription = ""
                )
            }
            Text(
                modifier = Modifier.padding(top = 80.dp),
                text = "点击「开始摇动」后摇动手机，摇动结束点击「结束摇动」展示刚才摇动的最大值",
                fontSize = 16.sp
            )

            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = speed,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(modifier = Modifier.padding(top = 32.dp)) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .weight(1.0f)
                        .clickable {
                            onStart?.invoke()
                        },
                ) {
                    Text(
                        text = "开始摇动",
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .weight(1.0f)
                        .clickable {
                            onStop?.invoke()
                        },
                ) {
                    Text(
                        text = "结束摇动",
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            }

        }
    }
}