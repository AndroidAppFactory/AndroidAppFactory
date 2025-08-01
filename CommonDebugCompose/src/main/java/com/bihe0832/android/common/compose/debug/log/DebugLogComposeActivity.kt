package com.bihe0832.android.common.compose.debug.log;

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bihe0832.android.common.compose.common.activity.CommonComposeActivity
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.log.item.ItemOnClickListener
import com.bihe0832.android.common.compose.debug.log.item.LogItem
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.framework.router.RouterInterrupt
import com.bihe0832.android.framework.router.showFileContent
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.router.annotation.Module

@Module(RouterConstants.MODULE_NAME_SHOW_LOG_LIST)
open class DebugLogComposeActivity : CommonComposeActivity() {
    internal var isView = true

    @Composable
    override fun getTitleName(): String {
        return "日志查看"
    }

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                LogComposeView()
            }
        }
    }

    open fun getLogList(): List<LogInfo> {
        return mutableListOf<LogInfo>().apply {
            for (i in 1..10) {
                add(LogInfo("路由跳转", RouterInterrupt.getRouterLogPath()))
            }
        }
    }

    data class LogInfo(
        val title: String = "",
        val path: String = "",
        val sort: Boolean = false,
        val showLine: Boolean = true,
        val showAction: Boolean = true,
        val clickAction: ItemOnClickListener? = null
    )

    @Preview
    @Composable
    fun LogComposeView() {
        Column{
            LogCommonList()
            LogItemList()
        }
    }

    @Composable
    open fun LogCommonList() {
        DebugTips("通用日志工具")
        GetLogPathItem(LoggerFile.getZixieFileLogPathByModule("*"))
        Row {
            Box(modifier = Modifier.weight(1f)) {
                GetSendLogItem(this@DebugLogComposeActivity, ZixieContext.getLogFolder())
            }
            Box(modifier = Modifier.weight(1f)) {
                GetOpenLogItem(this@DebugLogComposeActivity, ZixieContext.getLogFolder())
            }
        }
    }


    @Composable
    open fun LogItemList() {
        DebugTips("基础通用日志")
        val dataList = getLogList()
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(dataList) { item ->
                LogItem(
                    item.title,
                    item.path,
                    item.sort,
                    item.showLine,
                    item.showAction,
                    item.clickAction
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FileSelectTools.FILE_CHOOSER && resultCode == RESULT_OK) {
            data?.extras?.getString(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, "")?.let { filePath ->
                if (isView) {
                    showFileContent(filePath, isReversed = false, showLine = true, showNum = 2000)
                } else {
                    FileUtils.sendFile(this, filePath).let {
                        if (!it) {
                            ZixieContext.showToast("分享文件:$filePath 失败")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DebugLogComposeActivity.GetLogPathItem(path: String) {
    DebugItem("日志路径：<BR><small>${path}</small>") {}
}

@Composable
fun DebugLogComposeActivity.GetSendLogItem(activity: Activity, path: String) {
    DebugItem("选择并发送日志") {
        isView = false
        FileSelectTools.openFileSelect(activity, path)
    }
}

@Composable
fun DebugLogComposeActivity.GetOpenLogItem(activity: Activity, path: String) {
    return DebugItem("选择并查看日志") {
        isView = true
        FileSelectTools.openFileSelect(activity, path)
    }
}
