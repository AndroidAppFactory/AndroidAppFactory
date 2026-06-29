package com.bihe0832.android.base.compose.debug.request

import android.net.Uri
import android.text.TextUtils
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.bihe0832.android.base.compose.debug.httpdns.AliDnsProvider
import com.bihe0832.android.base.compose.debug.httpdns.DnspodProvider
import com.bihe0832.android.base.compose.debug.request.advanced.TestResponse
import com.bihe0832.android.base.compose.debug.request.basic.BasicPostRequest
import com.bihe0832.android.base.compose.debug.request.okhttp.debugOKHttp1
import com.bihe0832.android.base.compose.debug.request.okhttp.debugOKHttp2
import com.bihe0832.android.common.compose.debug.DebugBaseComposeActivityWithDrawer
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.http.advanced.HttpAdvancedResponseHandler
import com.bihe0832.android.lib.http.common.HTTPServer
import com.bihe0832.android.lib.http.common.HttpResponseHandler
import com.bihe0832.android.lib.http.common.core.BaseConnection
import com.bihe0832.android.lib.http.common.core.FileInfo
import com.bihe0832.android.lib.http.common.core.HttpBasicRequest
import com.bihe0832.android.lib.http.dns.AAFDnsConfig
import com.bihe0832.android.lib.http.dns.AAFHttpDnsManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.utils.encrypt.compression.GzipUtils
import org.json.JSONObject
import java.io.File
import java.net.InetAddress
import java.net.URLDecoder


class DebugHttpActivity : DebugBaseComposeActivityWithDrawer() {

    private val _resultText = mutableStateOf("Result:")
    val resultText: State<String> = _resultText

    fun showResult(text: String) {
        _resultText.value = text
    }

    fun clearResult() {
        _resultText.value = "Result:"
    }

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                GetHttpView()
            }
        }
    }

    @Composable
    fun GetHttpView() {
        var inputText by remember { mutableStateOf("test") }
        val resultText by resultText

        val buttonList = listOf(
            "使用OkHttp库发送POST请求1" to {
                debugOKHttp1()
            }, "使用OkHttp库发送POST请求2" to {
                debugOKHttp2()
            },
            "使用Basic库发送GET请求" to {
                sendGetBasicRequest(inputText)
            },
            "使用Basic库发送POST请求" to {
                sendPostBasicRequest(inputText)
            },
            "使用Advanced库发送GET请求" to {
                sendGetAdvancedRequest(inputText)
            },
            "使用Advanced发送POST请求" to {
                sendPostAdvancedRequest(inputText)
            },
            "上传文件" to {
                uploadFile()
            },
            "Gzip测试" to {
                testGzip()
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                )
        ) {
            // 标题文本
            Text(
                text = "网络请求测试：",
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 30.dp)
            )

            // 输入框
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("请输入测试文字") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            )

            // 动态生成按钮
            buttonList.forEach { (text, onClick) ->
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 10.dp)
                        .padding(top = 8.dp)
                ) {
                    Text(text)
                }
            }

            // 清空结果按钮
            Button(
                onClick = { clearResult() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 10.dp)
                    .padding(top = 8.dp)
            ) {
                Text("清空网络请求结果")
            }

            // DNS 测试标题
            Text(
                text = "DNS 解析测试（选择模式后，上方网络请求自动走该 DNS）：",
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .padding(top = 30.dp, bottom = 10.dp)
            )

            // DNS 模式选择
            var dnsModeName by remember { mutableStateOf("无 DNS") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Button(
                    onClick = {
                        dnsModeName = "无 DNS"
                        AAFHttpDnsManager.reset()
                        showResult("[DNS 模式] 已切换为系统 DNS")
                    },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) { Text("无 DNS") }
                Button(
                    onClick = {
                        dnsModeName = "AliDNS"
                        setDnsMode(AliDnsProvider())
                        showResult("[DNS 模式] 已切换为 AliDNS，上方请求将使用 AliDNS 解析")
                    },
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) { Text("AliDNS") }
                Button(
                    onClick = {
                        dnsModeName = "DNSPod"
                        setDnsMode(DnspodProvider())
                        showResult("[DNS 模式] 已切换为 DNSPod，上方请求将使用 DNSPod 解析")
                    },
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) { Text("DNSPod") }
            }

            // 当前 DNS 模式提示
            Text(
                text = "当前 DNS: $dnsModeName",
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .padding(top = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            // DNS 域名输入 + 解析测试
            var dnsHostname by remember { mutableStateOf("www.qq.com") }
            OutlinedTextField(
                value = dnsHostname,
                onValueChange = { dnsHostname = it },
                label = { Text("DNS 域名") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = { testDnsLookup(dnsHostname) },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) { Text("解析测试") }
                Button(
                    onClick = { testSystemDnsLookup(dnsHostname) },
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) { Text("系统 DNS(对比)") }
            }

            // 结果显示区域
            Text(
                text = resultText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(top = 30.dp),
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 1.25.em
            )
        }

    }

    fun testGzip() {
        HTTPServer.getInstance()
            .doRequest("http://dldir1.qq.com/INO/poster/FeHelper-20220321114751.json.gzip")
            .let {
                showResult("同步请求结果：${GzipUtils.uncompressToString(it)}")
            }
    }

    // ──────────────────────── DNS 管理 ────────────────────────

    /**
     * 设置全局 DNS 模式（供上方 HTTP 请求自动使用）
     *
     * 通过 [AAFHttpDnsManager.init] 注入 Provider 后，
     * [HTTPServer.getConnection] 内部会调用 [AAFHttpDnsManager.resolveUrlForConnection]，
     * 所有 Basic/Advanced/Gzip/Upload 请求自动走 HTTPDNS。
     * OkHttp 客户端同样会通过 [AAFOkHttpDns] 适配器使用 HTTPDNS。
     */
    private fun setDnsMode(provider: com.bihe0832.android.lib.http.dns.AAFHttpDnsProvider) {
        AAFHttpDnsManager.init(provider, AAFDnsConfig(cacheExpireMs = 0))
    }

    /**
     * DNS 解析测试 — 使用当前选择的 DNS 模式解析域名
     */
    fun testDnsLookup(dnsHostname: String) {
        if (dnsHostname.isEmpty()) {
            showResult("请输入域名")
            return
        }
        Thread {
            val start = System.currentTimeMillis()
            try {
                val result = AAFHttpDnsManager.lookup(dnsHostname)
                val elapsed = System.currentTimeMillis() - start
                runOnUiThread {
                    showResult(buildString {
                        appendLine("[DNS 解析]  耗时: ${elapsed}ms")
                        appendLine()
                        if (result.isNullOrEmpty()) {
                            appendLine("❌ 解析失败")
                        } else {
                            appendLine("✅ 解析成功，共 ${result.size} 个 IP：")
                            result.forEach { ip -> appendLine("  → ${ip.hostAddress}") }
                        }
                    })
                }
            } catch (e: Exception) {
                val elapsed = System.currentTimeMillis() - start
                runOnUiThread {
                    showResult(buildString {
                        appendLine("[DNS 解析]  耗时: ${elapsed}ms")
                        appendLine()
                        appendLine("❌ 异常: ${e.message}")
                    })
                }
            }
        }.start()
    }

    /**
     * 直接用系统 DNS 解析（不走 HTTPDNS），用于对比测试
     */
    fun testSystemDnsLookup(dnsHostname: String) {
        if (dnsHostname.isEmpty()) {
            showResult("请输入域名")
            return
        }
        Thread {
            val start = System.currentTimeMillis()
            try {
                val result = InetAddress.getAllByName(dnsHostname)
                val elapsed = System.currentTimeMillis() - start
                runOnUiThread {
                    showResult(buildString {
                        appendLine("[系统 DNS(对比)]  耗时: ${elapsed}ms")
                        appendLine()
                        appendLine("✅ 解析成功，共 ${result.size} 个 IP：")
                        result.forEach { ip -> appendLine("  → ${ip.hostAddress}") }
                    })
                }
            } catch (e: Exception) {
                val elapsed = System.currentTimeMillis() - start
                runOnUiThread {
                    showResult(buildString {
                        appendLine("[系统 DNS(对比)]  耗时: ${elapsed}ms")
                        appendLine()
                        appendLine("❌ 异常: ${e.message}")
                    })
                }
            }
        }.start()
    }

    fun uploadFile() {
        val file = File(AAFFileWrapper.getTempFolder() + "a.text")
        file.createNewFile()
        FileUtils.writeToFile(file.absolutePath, "fsdfsdfsd", false)

        var b = HashMap<String, String>().apply {
            put("fsdf1", "fsdf1")
            put("fsdf2", "fsdf2")
        }.let {
//                HTTPServer.getFormDataString(it)
            JSONObject(it as Map<*, *>?).toString()
        }

        var files = mutableListOf<FileInfo>()
        files.add(
            FileInfo(
                Uri.fromFile(file),
                "media",
                BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_OCTET_STREAM,
                file.name,
                file.length()
            ),
        )

        HTTPServer.getInstance().doFileUpload(
            this,
            "https://qyapi.weixin.qq.com/cgi-bin/webhook/upload_media?key=XXXX0&type=file&debug=1",
            b,
            files,
            BaseConnection.HTTP_REQ_VALUE_CHARSET_UTF8
        ).let {
            ZLog.d(HTTPServer.LOG_TAG, "result $it")
            showResult(it)
        }
    }

    fun testURL() {
        mutableListOf<String>().apply {
            add("https://www.qq.com 1")
            add("https://www.google.com/search 1?q=android+studio+%E5%BF%AB%E9%80%9F%E6%B7%BB%E5%8A%A0+%E4%BD%9C%E8%80%85&newwindow=1&sxsrf=ALiCzsazI_7umVmoDpgf7Kqm3Zlmv8IfnQ%3A1653400792696&ei=2OSMYu-RKrWFr7wP372bqAw&ved=0ahUKEwjv3a33pfj3AhW1wosBHd_eBsUQ4dUDCA4&uact=5&oq=android+studio+%E5%BF%AB%E9%80%9F%E6%B7%BB%E5%8A%A0+%E4%BD%9C%E8%80%85&gs_lcp=Cgdnd3Mtd2l6EAMyBQghEKABOgcIABBHELADOgQIIxAnOgQIABBDOgUIABCABDoFCAAQywE6BAgAEB46BggAEB4QBEoECEEYAEoECEYYAFDrA1i-wxBgksUQaA9wAXgBgAGZAogB4BKSAQYyMS40LjGYAQCgAQHIAQrAAQE&sclient=gws-wiz")
            add("https%3A%2F%2Fsupport.qq.com%2Fproduct%2F290858 1")
        }.forEach {
            ZLog.d("----------------------------")
            ZLog.d("Source: $it")
            ZLog.d("Zixie: ${URLUtils.encode(it)}")
            ZLog.d("Zixie: ${URLDecoder.decode(URLUtils.encode(it))})")
            ZLog.d("Zixie: ${URLDecoder.decode(URLUtils.encode(it)).equals(it)}")
            ZLog.d("----------------------------")
        }
    }

    private fun sendGetBasicRequest(result: String) {
        if (result.isNotEmpty()) {
//            val handle = TestBasicResponseHandler()
//            val request = BasicGetRequest(result, handle)
//            HTTPServer.getInstance().doRequest(request)

            HTTPServer.getInstance()
                .doRequest(Constants.HTTP_DOMAIN + Constants.PATH_GET + "?para=" + result)
                .let {
                    showResult("同步请求结果：$it")
                }
        } else {
            showResult("请在输入框输入请求内容！")
        }
    }

    private fun sendGetAdvancedRequest(result: String) {
        if (result.isNotEmpty()) {
            HTTPServer.getInstance().doRequest(
                object : HttpBasicRequest() {

                    override fun getUrl(): String {
                        val builder = StringBuilder()
                        builder.append(Constants.PARA_PARA + HttpBasicRequest.HTTP_REQ_ENTITY_MERGE + result)
                        return Constants.HTTP_DOMAIN + Constants.PATH_GET + "?" + builder.toString()
                    }
                },
                object : HttpAdvancedResponseHandler<TestResponse>() {
                    override fun onSuccess(result: TestResponse?) {
                        showResult(result.toString())
                    }

                    override fun onError(statusCode: Int, msg: String) {
                        showResult("HTTP状态码：\n\t$statusCode \n 网络请求内容：\n\t$msg")
                    }
                }, null, BaseConnection.HTTP_REQ_VALUE_CHARSET_UTF8
            )
        } else {
            showResult("请在输入框输入请求内容！")
        }
    }

    private fun sendPostBasicRequest(result: String) {
        if (result.isNotEmpty()) {
            val handle = TestBasicResponseHandler()
            val request = BasicPostRequest(result)
            HTTPServer.getInstance()
                .doRequest(request, handle, null, BaseConnection.HTTP_REQ_VALUE_CHARSET_UTF8)
        } else {
            showResult("请在输入框输入请求内容！")
        }
    }

    private fun sendPostAdvancedRequest(result: String) {
        if (!TextUtils.isEmpty(result)) {
            HTTPServer.getInstance().doRequest(
                object : HttpBasicRequest() {
                    init {
                        try {
//                            this.data =
//                                (Constants.PARA_PARA + HttpBasicRequest.HTTP_REQ_ENTITY_MERGE + result).toByteArray(
//                                    charset("UTF-8"),
//                                )
                            val taskArr = ArrayList<String>()
                            taskArr.add("111")

                            HashMap<String, String?>().apply {
                                put(Constants.PARA_PARA, result)
                                put("sdfdsf", "dfd")
                                put("ewewe", JsonHelper.toJson(taskArr).toString())
                            }.let {
                                this.data = getFormData(it)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun getUrl(): String {
                        return Constants.HTTP_DOMAIN + Constants.PATH_POST
                    }
                },
                object : HttpAdvancedResponseHandler<TestResponse>() {
                    override fun onSuccess(response: TestResponse?) {
                        showResult(response.toString())
                    }

                    override fun onError(statusCode: Int, msg: String) {
                        showResult("HTTP状态码：\n\t$statusCode \n 网络请求内容：\n\t$msg")
                    }
                }, null, BaseConnection.HTTP_REQ_VALUE_CHARSET_UTF8
            )
        } else {
            showResult("请在输入框输入请求内容！")
        }
    }

    private inner class TestBasicResponseHandler : HttpResponseHandler {

        override fun onResponse(statusCode: Int, response: String) {
            showResult(
                "HTTP状态码：\n\t" + statusCode + " \n " +
                        "网络请求内容：\n\t" + response,
            )
        }
    }
}
