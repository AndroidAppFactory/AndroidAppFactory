package com.bihe0832.android.base.compose.debug.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2025/11/4.
 * Description: Description
 *
 */
@Preview
@Composable
fun DebugFlexWithScrollAndExamplePre() {
    // 示例数据
    val jobList = mapOf(
        "开发" to mutableListOf<String>().apply {
            repeat(50) { index ->
                add("开发$index")
            }
        },
        "设计" to mutableListOf<String>().apply {
            repeat(50) { index ->
                add("设计$index")
            }
        },
        "产品" to mutableListOf<String>().apply {
            repeat(50) { index ->
                add("产品$index")
            }
        },
        "测试" to mutableListOf<String>().apply {
            repeat(50) { index ->
                add("测试$index")
            }
        },
        "运维" to mutableListOf<String>().apply {
            repeat(50) { index ->
                add("运维$index")
            }
        },

        )

// 在 UI 中调用
    FlexWithScrollAndExample(jobList = jobList)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlexWithScrollAndExample(jobList: Map<String, List<String>>) {
    val groupedJobs = jobList.toList()  // List<Pair<String, List<String>>>
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    // ✅ 确保这里是你想要的默认分类，且它一定在 groupedJobs 中存在
    var currentScrollCategory by remember { mutableStateOf("产品") }
    var currentTabCategory by remember { mutableStateOf("") }

    // ✅ 关键修复：根据 currentCategory，在页面加载时自动滚动到对应 item
    LaunchedEffect(currentScrollCategory) {
        val index = groupedJobs.indexOfFirst { it.first == currentScrollCategory }
        println("🔍 尝试滚动到分类 '$currentScrollCategory'，索引 = $index")
        if (index != -1) {
            coroutineScope.launch {
                lazyListState.scrollToItem(index)
            }
        } else {
            println("⚠️ 错误：未找到分类 '$currentScrollCategory'，请检查 groupedJobs 是否包含该分类")
        }
    }

    // ✅ 监听滚动位置，用于更新当前高亮分类（用户滚动时）
    val firstVisibleIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    LaunchedEffect(firstVisibleIndex) {
        val category = groupedJobs.getOrNull(firstVisibleIndex)?.first
        if (category != null) {
            currentTabCategory = category
        }
    }

    Column {
        // 顶部横向分类按钮（可点击，用于跳转）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            jobList.keys.forEach { category ->
                Text(
                    text = category,
                    fontSize = 16.sp,
                    color = if (category == currentTabCategory) Color.White else Color(0xCCFFFFFF),
                    modifier = Modifier
                        .clickable {
                            currentScrollCategory = category
                        }
                        .background(
                            color = if (category == currentTabCategory) Color(0xFF327BD4) else Color(0xFF666666)
                        )
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = if (category == currentTabCategory) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // LazyColumn 展示分类内容
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            state = lazyListState
        ) {
            items(groupedJobs) { (category, jobs) ->
                Text(
                    text = category,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF327BD4))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    jobs.forEach { job ->
                        Text(
                            text = job,
                            modifier = Modifier
                                .background(Color.Gray.copy(alpha = 0.3f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}