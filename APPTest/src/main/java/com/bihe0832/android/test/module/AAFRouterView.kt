package com.bihe0832.android.test.module

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.debug.module.router.DebugRouterComposeActivity
import com.bihe0832.android.common.compose.debug.module.router.GetRouterView
import com.bihe0832.android.lib.request.URLUtils

@Preview
@Composable
fun AAFRouterView() {
    GetRouterView(getRouterList())
}

fun getRouterList(): List<String> {
    return mutableListOf(
        "zixie://test",
        "zixie://about",
        "zixie://zshare?url=https%3A%2F%2Fblog.bihe0832.com",
        "zixie://zfeedback?url=https%3A%2F%2Fsupport.qq.com%2Fproduct%2F290858",
        "zixie://ztbsfeedback?url=https%3A%2F%2Fsupport.qq.com%2Fproduct%2F290858",
        "zixie://zweb?url=https%3A%2F%2Fblog.bihe0832.com",
        "zixie://zfeedback?url=" + URLUtils.encode("https://support.qq.com/embed/phone/290858/large/"),
        "zixie://ztbsfeedback?url=" + URLUtils.encode("https://support.qq.com/embed/phone/290858/large/"),
        "zixie://zweb?url=" + URLUtils.encode("https://play.google.com/store/apps/details?id=com.pubg.newstate")
    )
}