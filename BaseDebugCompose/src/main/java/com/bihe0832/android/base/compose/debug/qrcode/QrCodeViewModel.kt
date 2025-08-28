package com.bihe0832.android.base.compose.debug.qrcode

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bihe0832.android.common.qrcode.QrcodeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/8/28.
 * Description: Description
 *
 */
class QrCodeViewModel : ViewModel() {
    // 状态
    private val _inputText = mutableStateOf("")
    val inputText: String get() = _inputText.value

    // 防抖任务（存储 Job 引用）
    var debounceJob: Job? = null

    private val _qrCodeImage = mutableStateOf<Bitmap?>(null)
    val qrCodeImage: Bitmap? get() = _qrCodeImage.value

    private val _resultText = mutableStateOf("")
    val resultText: String get() = _resultText.value

    // 操作
    fun setInputText(text: String) {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {  // 使用 viewModelScope
            delay(20)
            _inputText.value = text
            generateQRCode(text)
        }

    }

    internal fun generateQRCode(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = QrcodeUtils.createQRCode(text, 400)
                withContext(Main) {
                    _qrCodeImage.value = bitmap
                    decodeQRCode(bitmap)
                }
            } catch (e: Exception) {
                withContext(Main) {
                    _resultText.value = "生成二维码失败：${e.message}"
                }
            }
        }
    }

    internal fun decodeQRCode(bitmap: Bitmap?) {
        bitmap?.let {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val result = QrcodeUtils.decodeQRcode(bitmap)
                    withContext(Main) {
                        _resultText.value =
                            "源数据为：\n$inputText\n解析结果：\n${result?.text ?: "无"}"
                    }
                } catch (e: Exception) {
                    withContext(Main) {
                        _resultText.value = "解码失败：${e.message}"
                    }
                }
            }
        }
    }
}