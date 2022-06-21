package com.bihe0832.android.base.debug.request

import android.net.Uri
import android.os.Bundle
import com.bihe0832.android.app.api.AAFNetWorkApi
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.base.debug.request.advanced.AdvancedGetRequest
import com.bihe0832.android.base.debug.request.advanced.AdvancedPostRequest
import com.bihe0832.android.base.debug.request.advanced.TestResponse
import com.bihe0832.android.base.debug.request.basic.BasicPostRequest
import com.bihe0832.android.base.debug.request.okhttp.debugOKHttp
import com.bihe0832.android.common.debug.base.BaseDebugActivity
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.http.advanced.HttpAdvancedRequest
import com.bihe0832.android.lib.http.common.HTTPServer
import com.bihe0832.android.lib.http.common.HttpResponseHandler
import com.bihe0832.android.lib.http.common.core.BaseConnection
import com.bihe0832.android.lib.http.common.core.FileInfo
import com.bihe0832.android.lib.http.common.core.HttpBasicRequest
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.router.annotation.Module
import kotlinx.android.synthetic.main.activity_http_test.*
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.File
import java.net.URLDecoder

const val ROUTRT_NAME_TEST_HTTP = "testhttp"

@Module(ROUTRT_NAME_TEST_HTTP)
class DebugHttpActivity : BaseDebugActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_http_test)

        common_toolbar.setNavigationOnClickListener { onBackPressed() }
        getBasic.setOnClickListener { sendGetBasicRequest() }

        postBasic.setOnClickListener {
//            sendPostBasicRequest()
            debugOKHttp()
        }

        getAdvanced.setOnClickListener { sendGetAdvancedRequest() }

        postAdvanced.setOnClickListener { sendPostAdvancedRequest() }

        postFile.setOnClickListener {
            var filePath = "/sdcard/shumei.txt"

            var b = HashMap<String, String>().apply {
                put("fsdf", "fsdf")
            }


            var files = mutableListOf<FileInfo>()
            files.add(
                    FileInfo(
                            Uri.fromFile(File(filePath)),
                            "media",
                            BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_OCTET_STREAM
                    )
            )

            HTTPServer.getInstance().doFileUpload(
                    this,
                    "https://qyapi.weixin.qq.com/cgi-bin/webhook/upload_media?key=XXXX&type=file&debug=1",
                    b,
                    files
            ).let {
                ZLog.d(HTTPServer.LOG_TAG, "restult $it")
                runOnUiThread { result.text = it }
            }
        }

        testGzip.setOnClickListener {
            HTTPServer.getInstance()
                    .doRequestSync("http://dldir1.qq.com/INO/poster/FeHelper-20220321114751.json.gzip")
                    .let {
                        showResult("同步请求结果：$it")
                    }

        }
        clearResult.setOnClickListener { result.text = "" }
    }

    private fun showResult(tips: String) {
        runOnUiThread { result.text = tips }
    }

    fun testURL() {
        mutableListOf<String>().apply {
            add("https://www.qq.com 1")
            add("https://www.google.com/search 1?q=android+studio+%E5%BF%AB%E9%80%9F%E6%B7%BB%E5%8A%A0+%E4%BD%9C%E8%80%85&newwindow=1&sxsrf=ALiCzsazI_7umVmoDpgf7Kqm3Zlmv8IfnQ%3A1653400792696&ei=2OSMYu-RKrWFr7wP372bqAw&ved=0ahUKEwjv3a33pfj3AhW1wosBHd_eBsUQ4dUDCA4&uact=5&oq=android+studio+%E5%BF%AB%E9%80%9F%E6%B7%BB%E5%8A%A0+%E4%BD%9C%E8%80%85&gs_lcp=Cgdnd3Mtd2l6EAMyBQghEKABOgcIABBHELADOgQIIxAnOgQIABBDOgUIABCABDoFCAAQywE6BAgAEB46BggAEB4QBEoECEEYAEoECEYYAFDrA1i-wxBgksUQaA9wAXgBgAGZAogB4BKSAQYyMS40LjGYAQCgAQHIAQrAAQE&sclient=gws-wiz")
            add("https%3A%2F%2Fsupport.qq.com%2Fproduct%2F290858 1")
        }.forEach {
            ZLog.d("----------------------------")
            ZLog.d("Source: ${it}")
            ZLog.d("Zixie: ${URLUtils.encode(it)}")
            ZLog.d("Zixie: ${URLDecoder.decode(URLUtils.encode(it))})")
            ZLog.d("Zixie: ${URLDecoder.decode(URLUtils.encode(it)).equals(it)}")
            ZLog.d("----------------------------")

        }
    }

    private fun sendGetBasicRequest() {
        var result = paraEditText.text?.toString()
        if (result?.length ?: 0 > 0) {
//            val handle = TestBasicResponseHandler()
//            val request = BasicGetRequest(result, handle)
//            HTTPServer.getInstance().doRequest(request)

            HTTPServer.getInstance()
                    .doRequestSync("https://microdemo.bihe0832.com/AndroidHTTP/get.php?para=" + result)
                    .let {
                        showResult("同步请求结果：$it")
                    }
        } else {
            showResult("请在输入框输入请求内容！")
        }
    }

    private fun sendGetAdvancedRequest() {
        var result = paraEditText.text?.toString()
        if (result?.length ?: 0 > 0) {

            HTTPServer.getInstance().doRequestAsync(object : HttpAdvancedRequest<TestResponse>() {
                var res = object : AAFDataCallback<TestResponse>() {
                    override fun onSuccess(result: TestResponse?) {
                        showResult(result.toString())
                    }

                    override fun onError(statusCode: Int, msg: String) {
                        showResult("HTTP状态码：\n\t$statusCode \n 网络请求内容：\n\t$msg")
                    }
                }

                override fun getUrl(): String {
                    val builder = StringBuilder()
                    builder.append(Constants.PARA_PARA + HttpBasicRequest.HTTP_REQ_ENTITY_MERGE + result)
                    return Constants.HTTP_DOMAIN + Constants.PATH_GET + "?" + builder.toString()
                }

                override fun getAdvancedResponseHandler(): AAFDataCallback<*> {
                    return res
                }
            })

            AdvancedGetRequest(result, TestAdvancedResponseHandler()).let {
                HTTPServer.getInstance().doRequestAsync(it)
            }
        } else {
            showResult("请在输入框输入请求内容！")
        }
    }

    private fun sendPostBasicRequest() {
        var result = paraEditText.text?.toString()
        if (result?.length ?: 0 > 0) {
            val handle = TestBasicResponseHandler()
            val request = BasicPostRequest(result, handle)
            HTTPServer.getInstance().doRequestAsync(request)
        } else {
            showResult("请在输入框输入请求内容！")

        }
    }

    private fun sendPostAdvancedRequest() {
        var result = paraEditText.text?.toString()

        if (result?.length ?: 0 > 0) {

            HTTPServer.getInstance().doRequestAsync(object : HttpAdvancedRequest<TestResponse>() {
                init {
                    try {
                        this.data =
                                (Constants.PARA_PARA + HttpBasicRequest.HTTP_REQ_ENTITY_MERGE + result).toByteArray(
                                        charset("UTF-8")
                                )

                        HashMap<String, String?>().apply {
                            put(Constants.PARA_PARA, result ?: "")
                            put("sdfdsf", "dfd")
                        }.let {
                            this.data = getFormData(it)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                var res = object : AAFDataCallback<TestResponse>() {
                    override fun onSuccess(response: TestResponse?) {
                        showResult(response.toString())
                    }

                    override fun onError(statusCode: Int, msg: String) {
                        showResult("HTTP状态码：\n\t$statusCode \n 网络请求内容：\n\t$msg")
                    }
                }

                override fun getUrl(): String {
                    return Constants.HTTP_DOMAIN + Constants.PATH_POST
                }

                override fun getAdvancedResponseHandler(): AAFDataCallback<*> {
                    return res
                }
            })

            AdvancedPostRequest(result, TestAdvancedResponseHandler()).let {
                HTTPServer.getInstance().doRequestAsync(it)
            }
        } else {
            showResult("请在输入框输入请求内容！")
        }
    }

    private inner class TestBasicResponseHandler : HttpResponseHandler {

        override fun onResponse(statusCode: Int, response: String) {
            showResult(
                    "HTTP状态码：\n\t" + statusCode + " \n " +
                            "网络请求内容：\n\t" + response
            )
        }
    }

    private inner class TestAdvancedResponseHandler :
            AAFDataCallback<TestResponse>() {
        override fun onSuccess(response: TestResponse?) {
            showResult(response.toString())
        }

        override fun onError(statusCode: Int, response: String) {
            showResult("HTTP状态码：\n\t$statusCode \n 网络请求内容：\n\t$response")
        }
    }
}
