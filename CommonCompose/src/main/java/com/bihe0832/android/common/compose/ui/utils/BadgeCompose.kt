package com.bihe0832.android.common.compose.ui.utils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 通用红点/数字徽章 Compose 组件
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/3/25.
 */

/**
 * 红点/数字徽章组件
 *
 * @param num 数字，0 表示仅显示红点，>0 显示数字，<0 不显示
 * @param maxNum 数字上限，超过时显示 "{maxNum}+"，默认 99
 * @param dotSize 仅红点时的大小，默认 8.dp
 * @param fontSize 数字字号，默认 10.sp，徽章大小会基于字号自动计算
 * @param badgeColor 徽章背景色，默认红色
 * @param textColor 数字文字颜色，默认白色
 */
@Composable
fun BadgeView(
    num: Int,
    maxNum: Int = 99,
    dotSize: Dp = 8.dp,
    fontSize: TextUnit = 10.sp,
    badgeColor: Color = Color.Red,
    textColor: Color = Color.White,
) {
    if (num == 0) {
        // 仅显示红点
        Canvas(modifier = Modifier.size(dotSize)) {
            drawCircle(color = badgeColor)
        }
    } else if (num > 0) {
        // 显示数字徽章
        val displayText = if (num > maxNum) "${maxNum}+" else num.toString()
        // 基于字号自动计算徽章尺寸（字号 + 固定边距）
        val badgeHeight = fontSize.value.dp + 6.dp
        if (displayText.length <= 1) {
            // 单字符：固定正方形 + 圆形，确保是正圆
            Box(
                modifier = Modifier
                    .size(badgeHeight)
                    .background(color = badgeColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayText,
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = TextStyle(
                        lineHeight = fontSize,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    )
                )
            }
        } else {
            // 多字符：固定高度 + 宽度自适应 + 胶囊形
            Box(
                modifier = Modifier
                    .heightIn(min = badgeHeight)
                    .widthIn(min = badgeHeight)
                    .background(color = badgeColor, shape = RoundedCornerShape(50))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayText,
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = TextStyle(
                        lineHeight = fontSize,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    )
                )
            }
        }
    }
}

// ==================== 预览函数 ====================

@Preview(name = "BadgeView - 各种样式", showBackground = true)
@Composable
fun PreviewBadgeView() {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("仅红点：", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            BadgeView(num = 0)
        }
        Spacer(modifier = Modifier.size(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("数字 3：", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            BadgeView(num = 3)
        }
        Spacer(modifier = Modifier.size(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("数字 12：", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            BadgeView(num = 12)
        }
        Spacer(modifier = Modifier.size(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("数字 99：", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            BadgeView(num = 99)
        }
        Spacer(modifier = Modifier.size(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("数字 100（超上限）：", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            BadgeView(num = 100)
        }
        Spacer(modifier = Modifier.size(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("不显示 (-1)：", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            BadgeView(num = -1)
        }
        Spacer(modifier = Modifier.size(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("大字号 5：", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            BadgeView(num = 5, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.size(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("大字号 88：", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            BadgeView(num = 88, fontSize = 14.sp)
        }
    }
}
