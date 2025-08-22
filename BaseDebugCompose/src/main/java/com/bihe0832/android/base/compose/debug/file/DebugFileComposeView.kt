package com.bihe0832.android.base.compose.debug.file

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.debug.item.DebugComposeActivityItem
import com.bihe0832.android.common.compose.debug.item.DebugComposeFragmentItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.module.device.storage.DebugCurrentStorageActivity
import com.bihe0832.android.common.compose.debug.module.device.storage.DebugStorageActivity
import com.bihe0832.android.common.compose.debug.ui.DebugContent

internal const val LOG_TAG = "DebugFileComposeView"

@Preview
@Composable
fun DebugFileComposeView() {
    DebugContent {
        DebugItem("文件及文件夹操作") { testFolder() }
        DebugItem("文本查看器") { testEdit() }
        DebugComposeActivityItem(
            "<font color ='#3AC8EF'><b>查看当前应用的存储占用情况</b></font>",
            DebugCurrentStorageActivity::class.java
        )
        DebugComposeActivityItem("查看制定目录的文件大小", DebugStorageActivity::class.java)

        DebugItem("Assets 操作") { testAssets(it) }
        DebugItem("文件长度测试") { testFileLength() }
        DebugItem("文件MD5") { testMD5() }
        DebugComposeFragmentItem("文件选择", DebugFileFragment::class.java)

        DebugItem("ZIP测试") { testZIP() }
        DebugItem("Sqlite测试") { testDB() }
        DebugItem("数据压缩解压") { testZLib() }
        DebugItem("数据分片与合并") { testSegment() }
        DebugItem("文件内容读写") { testReadAndWrite() }
        DebugItem("读取共享文件内容") { share() }
        DebugItem("创建指定大小文件") { createFile() }
        DebugItem("修改文件指定位置内容") { modifyFile(true) }
        DebugItem("文件指定位置插入内容") { modifyFile(false) }
    }
}