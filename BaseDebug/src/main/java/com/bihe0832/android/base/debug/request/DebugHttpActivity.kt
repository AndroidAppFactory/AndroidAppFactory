package com.bihe0832.android.base.debug.request

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.base.debug.request.advanced.TestResponse
import com.bihe0832.android.base.debug.request.basic.BasicPostRequest
import com.bihe0832.android.base.debug.request.okhttp.debugOKHttp
import com.bihe0832.android.common.debug.base.BaseDebugActivity
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


class DebugHttpActivity : BaseDebugActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_http_test)

        findViewById<Toolbar>(R.id.common_toolbar).setNavigationOnClickListener { onBackPressed() }

        findViewById<View>(R.id.postOkHttp).setOnClickListener {
            debugOKHttp()
        }

        findViewById<View>(R.id.getBasic).setOnClickListener { sendGetBasicRequest() }

        findViewById<View>(R.id.postBasic).setOnClickListener {
            sendPostBasicRequest()
        }

        findViewById<View>(R.id.getAdvanced).setOnClickListener { sendGetAdvancedRequest() }

        findViewById<View>(R.id.postAdvanced).setOnClickListener { sendPostAdvancedRequest() }

        findViewById<View>(R.id.postFile).setOnClickListener {

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

        findViewById<View>(R.id.testGzip).setOnClickListener {
            HTTPServer.getInstance()
                .doRequest("http://dldir1.qq.com/INO/poster/FeHelper-20220321114751.json.gzip")
                .let {
                    showResult("同步请求结果：${GzipUtils.uncompressToString(it)}")
                }
        }
        findViewById<View>(R.id.clearResult).setOnClickListener { showResult("") }
    }

    private fun showResult(tips: String) {
        runOnUiThread { findViewById<TextView>(R.id.result).text = tips }
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

    private fun sendGetBasicRequest() {
        var result = findViewById<EditText>(R.id.paraEditText).text?.toString()
        if (result?.length ?: 0 > 0) {
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

    private fun sendGetAdvancedRequest() {
        var result = findViewById<EditText>(R.id.paraEditText).text?.toString()
        if (result?.length ?: 0 > 0) {
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

    private fun sendPostBasicRequest() {
        var result = findViewById<EditText>(R.id.paraEditText).text?.toString()
        if (result?.length ?: 0 > 0) {
            val handle = TestBasicResponseHandler()
            val request = BasicPostRequest(result)
            HTTPServer.getInstance()
                .doRequest(request, handle, null, BaseConnection.HTTP_REQ_VALUE_CHARSET_UTF8)
        } else {
            showResult("请在输入框输入请求内容！")
        }
    }

    private fun sendPostAdvancedRequest() {
        val result = findViewById<EditText>(R.id.paraEditText).text?.toString()
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
                                put(Constants.PARA_PARA, result ?: "")
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
