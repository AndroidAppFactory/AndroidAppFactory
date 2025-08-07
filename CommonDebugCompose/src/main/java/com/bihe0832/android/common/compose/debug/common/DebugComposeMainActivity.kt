package com.bihe0832.android.common.compose.debug.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.common.compose.debug.DebugBaseComposeActivity
import com.bihe0832.android.common.compose.state.RenderState
import kotlinx.coroutines.launch

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: 在原生页面加载Compose
 *
 */

abstract class DebugComposeMainActivity : DebugBaseComposeActivity() {

    abstract fun getTabs(): List<String>

    abstract fun getDefault(): String

    @Composable
    override fun getNavigationIcon(): ImageVector? {
        return null
    }

    @Composable
    abstract fun GetPageView(page: Int, tab: String)

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                SegmentedButtonWithSwipeScreen()
            }
        }
    }

    @Preview
    @Composable
    fun SegmentedButtonWithSwipeScreen() {
        // 定义分段按钮选项
        val tabs: List<String> = getTabs()
        var selectedTab by remember { mutableStateOf(getDefault()) }
        // 分页器状态（与分段按钮联动）
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        val shape = RoundedCornerShape(0.dp)
        val coroutineScope = rememberCoroutineScope()


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondary),
                space = 0.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val textColor = MaterialTheme.colorScheme.onSecondary
                    SegmentedButton(
                        selected = tab == selectedTab,
                        onClick = {
                            selectedTab = tab
                            // 添加这行：点击时切换页面
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        shape = shape,
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.secondary,
                            inactiveContainerColor = MaterialTheme.colorScheme.secondary,
                            activeContentColor = MaterialTheme.colorScheme.onSecondary,
                            inactiveContentColor = MaterialTheme.colorScheme.onSecondary,
                        ),
                        border = BorderStroke(0.dp, Color.Transparent),
                        icon = {},
                        modifier = Modifier
                            .padding(horizontal = 0.dp, vertical = 0.dp)
                            .drawBehind {
                                // 只有选中时才绘制下边线
                                if (tab == selectedTab) {
                                    val strokeWidth = 4.dp.toPx()
                                    val y = size.height - strokeWidth / 2
                                    drawLine(
                                        color = textColor,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth,
                                        cap = StrokeCap.Round // 设置圆角端点
                                    )
                                }
                            },
                    ) {
                        Text(
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = if (tab == selectedTab) {
                                FontWeight.ExtraBold
                            } else {
                                FontWeight.Normal
                            },
                            textAlign = TextAlign.Center,
                            text = tab,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }


            // 2. 可左右滑动的 HorizontalPager
            HorizontalPager(
                state = pagerState, modifier = Modifier.weight(1f)
            ) { page ->
                // 根据页码显示不同内容
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    GetPageView(page, tabs[page])
                }
            }
        }

        // 监听分页器滑动，同步更新 SegmentedButton
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                selectedTab = tabs[page]
            }
        }
    }

}


