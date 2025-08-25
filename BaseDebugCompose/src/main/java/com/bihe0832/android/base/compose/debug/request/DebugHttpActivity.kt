package com.bihe0832.android.base.compose.debug.request

import android.net.Uri
import android.text.TextUtils
import androidx.compose.foundation.layout.Column
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
import com.bihe0832.android.base.compose.debug.request.advanced.TestResponse
import com.bihe0832.android.base.compose.debug.request.basic.BasicPostRequest
import com.bihe0832.android.base.compose.debug.request.okhttp.debugOKHttp
import com.bihe0832.android.common.compose.debug.DebugBaseComposeActivity
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
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.utils.encrypt.compression.GzipUtils
import org.json.JSONObject
import java.io.File
import java.net.URLDecoder


class DebugHttpActivity : DebugBaseComposeActivity() {

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
            "使用OkHttp库发送POST请求" to {
                debugOKHttp()
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
