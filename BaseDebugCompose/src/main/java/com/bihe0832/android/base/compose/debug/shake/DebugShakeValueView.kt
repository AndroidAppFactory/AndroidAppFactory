package com.bihe0832.android.base.compose.debug.shake

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
fun DebugShakeValueView(speed: Double = 12330.023, onStart: (() -> Unit)? = null, onStop: (() -> Unit)? = null) {
    Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(32.dp)
                .align(Alignment.Center)) {
            Box(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()) {
                Image(modifier = Modifier
                        .width(150.dp)
                        .align(Alignment.Center), painter = painterResource(id = R.mipmap.icon), contentDescription = "")
            }
            Text(modifier = Modifier.padding(top = 80.dp), text = "点击「开始摇动」后摇动手机，摇动结束点击「结束摇动」展示刚才摇动的最大值", fontSize = 16.sp)

            Text(modifier = Modifier.padding(top = 32.dp), text = "实时数值：" + speed.toInt() / 100 + "，对应速度：" + speed, fontSize = 16.sp, fontWeight = FontWeight.Bold)

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
                    Text(text = "开始摇动", modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.Center), color = MaterialTheme.colorScheme.onSecondary, fontSize = 12.sp)
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
                    Text(text = "结束摇动", modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.Center), color = MaterialTheme.colorScheme.onSecondary, fontSize = 12.sp)
                }

            }

        }
    }
}