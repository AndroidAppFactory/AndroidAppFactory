package com.bihe0832.android.base.test.request

import android.os.Bundle
import com.bihe0832.android.base.test.R
import com.bihe0832.android.base.test.request.advanced.AdvancedGetRequest
import com.bihe0832.android.base.test.request.advanced.AdvancedPostRequest
import com.bihe0832.android.base.test.request.advanced.TestResponse
import com.bihe0832.android.base.test.request.basic.BasicPostRequest
import com.bihe0832.android.common.test.base.BaseTestActivity
import com.bihe0832.android.lib.http.advanced.HttpAdvancedRequest
import com.bihe0832.android.lib.http.common.HTTPServer
import com.bihe0832.android.lib.http.common.HttpResponseHandler
import com.bihe0832.android.lib.http.common.core.BaseConnection
import com.bihe0832.android.lib.http.common.core.HttpBasicRequest
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.router.annotation.Module
import kotlinx.android.synthetic.main.activity_http_test.*

const val ROUTRT_NAME_TEST_HTTP = "testhttp"

@Module(ROUTRT_NAME_TEST_HTTP)
class TestHttpActivity : BaseTestActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_http_test)

        common_toolbar.setNavigationOnClickListener { onBackPressedSupport() }
        getBasic.setOnClickListener { sendGetBasicRequest() }

        postBasic.setOnClickListener { sendPostBasicRequest() }

        getAdvanced.setOnClickListener { sendGetAdvancedRequest() }

        postAdvanced.setOnClickListener { sendPostAdvancedRequest() }

        postFile.setOnClickListener {
            var filePath = "/sdcard/shumei.txt"

            var b = HashMap<String, String>().apply {
                put("fsdf", "fsdf")
            }

            HTTPServer.getInstance().doFileUpload(
                    "https://qyapi.weixin.qq.com/cgi-bin/webhook/upload_media?key=XXXX&type=file&debug=1", b, filePath,
                    "media",
                    BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_OCTET_STREAM
            ).let {
                ZLog.d(HTTPServer.LOG_TAG, "restult $it")
                runOnUiThread { result.text = it }
            }
        }

        clearResult.setOnClickListener { result.text = "" }
    }

    private fun showResult(tips: String) {
        runOnUiThread { result.text = tips }
    }

    private fun sendGetBasicRequest() {
        var result = paraEditText.text?.toString()
        if (result?.length ?: 0 > 0) {
//            val handle = TestBasicResponseHandler()
//            val request = BasicGetRequest(result, handle)
//            HTTPServer.getInstance().doRequest(request)

            HTTPServer.getInstance().doRequestSync("https://microdemo.bihe0832.com/AndroidHTTP/get.php?para=" + result).let {
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
                var res = object : AdvancedResponseHandler<TestResponse> {
                    override fun onRequestSuccess(response: TestResponse) {
                        showResult(response.toString())
                    }

                    override fun onRequestFailure(statusCode: Int, response: String?) {
                        showResult("HTTP状态码：\n\t$statusCode \n 网络请求内容：\n\t$response")
                    }
                }

                override fun getUrl(): String {
                    val builder = StringBuilder()
                    builder.append(Constants.PARA_PARA + HttpBasicRequest.HTTP_REQ_ENTITY_MERGE + result)
                    return Constants.HTTP_DOMAIN + Constants.PATH_GET + "?" + builder.toString()
                }

                override fun getAdvancedResponseHandler(): AdvancedResponseHandler<*> {
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
                        this.data = (Constants.PARA_PARA + HttpBasicRequest.HTTP_REQ_ENTITY_MERGE + result).toByteArray(charset("UTF-8"))

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

                var res = object : AdvancedResponseHandler<TestResponse> {
                    override fun onRequestSuccess(response: TestResponse) {
                        showResult(response.toString())
                    }

                    override fun onRequestFailure(statusCode: Int, response: String?) {
                        showResult("HTTP状态码：\n\t$statusCode \n 网络请求内容：\n\t$response")
                    }
                }

                override fun getUrl(): String {
                    return Constants.HTTP_DOMAIN + Constants.PATH_POST
                }

                override fun getAdvancedResponseHandler(): AdvancedResponseHandler<*> {
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
            showResult("HTTP状态码：\n\t" + statusCode + " \n " +
                    "网络请求内容：\n\t" + response)
        }
    }

    private inner class TestAdvancedResponseHandler : HttpAdvancedRequest.AdvancedResponseHandler<TestResponse> {
        override fun onRequestSuccess(response: TestResponse) {
            showResult(response.toString())
        }

        override fun onRequestFailure(statusCode: Int, response: String?) {
            showResult("HTTP状态码：\n\t$statusCode \n 网络请求内容：\n\t$response")
        }
    }
}
