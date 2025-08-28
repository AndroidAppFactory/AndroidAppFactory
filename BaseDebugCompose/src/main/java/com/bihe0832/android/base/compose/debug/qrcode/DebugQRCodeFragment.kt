package com.bihe0832.android.base.compose.debug.qrcode

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bihe0832.android.common.compose.debug.module.DebugCommonComposeFragment
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.qrcode.QrcodeUtils
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode
import com.bihe0832.android.framework.router.shareByQrcode

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/8/22.
 * Description: Description
 *
 */
class DebugQRCodeFragment : DebugCommonComposeFragment() {
    val qrCodeState = QrCodeViewModel()
    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                LaunchedEffect(Unit) {
                    qrCodeState.setInputText("https://cdn.bihe0832.com/")
                }
                QrCodeView()
            }
        }
    }

    @Preview
    @Composable
    fun QrCodeView() {
        Column(modifier = Modifier.fillMaxSize()) {
            TextField(value = qrCodeState.inputText, onValueChange = {
                qrCodeState.setInputText(it)
            }, label = {
                Text(
                    modifier = Modifier.padding(4.dp), text = "输入二维码内容"
                )
            }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.weight(1.0f, true)) {
                qrCodeState.qrCodeImage?.asImageBitmap()?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "二维码",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 结果文本
            Text(
                text = qrCodeState.resultText, style = TextStyle(
                    color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold
                ), modifier = Modifier.padding(16.dp)
            )

            // 按钮组
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        qrCodeState.generateQRCode(qrCodeState.inputText)
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(4.dp), text = "点击刷新二维码数据"
                    )
                }

                Button(
                    onClick = {
                        QrcodeUtils.openQrScan(
                            this@DebugQRCodeFragment, true, true, true, true
                        )
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(4.dp), text = "点击唤起相机扫描"
                    )
                }

                Button(
                    onClick = {
                        QrcodeUtils.openQrScanAndParse(true, true, false, false)
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(4.dp), text = "点击唤起相机扫描并识别"
                    )
                }

                Button(
                    onClick = {
                        shareByQrcode(qrCodeState.inputText)
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(4.dp), text = "使用二维码分享数据"
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ZixieActivityRequestCode.QRCODE_SCAN -> {
                    data?.getStringExtra(ZixieActivityRequestCode.INTENT_EXTRA_KEY_QR_SCAN)?.let {
                        qrCodeState.setInputText(it)
                    }
                }
            }
        }
    }
}