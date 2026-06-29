package com.bihe0832.android.base.compose.debug.httpdns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bihe0832.android.common.compose.debug.DebugBaseComposeActivityWithDrawer
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.lib.http.dns.AAFDnsConfig
import com.bihe0832.android.lib.http.dns.AAFHttpDnsManager
import com.bihe0832.android.lib.http.dns.AAFHttpDnsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

/**
 * HTTPDNS 测试入口
 *
 * 测试三个 DNS 解析源的效果：
 * - AliDNS（阿里公共 DNS DoH）
 * - DNSPod（腾讯 DNSPod DoH）
 * - 系统 DNS（作为对比基准）
 *
 * @author zixie code@bihe0832.com
 * Created on 2026-06-29
 */
class HttpDnsTestActivity : DebugBaseComposeActivityWithDrawer() {

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                HttpDnsTestView()
            }
        }
    }

    @Composable
    private fun HttpDnsTestView() {
        var hostname by remember { mutableStateOf("www.qq.com") }
        var resultText by remember { mutableStateOf("点击按钮开始测试") }
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "HTTPDNS 测试",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = hostname,
                onValueChange = { hostname = it },
                label = { Text("域名") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            testLookup("AliDNS", hostname, { AliDnsProvider() }) { resultText = it }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("AliDNS") }

                Button(
                    onClick = {
                        scope.launch {
                            testLookup("DNSPod", hostname, { DnspodProvider() }) { resultText = it }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("DNSPod") }

                Button(
                    onClick = {
                        scope.launch {
                            testSystemDns(hostname) { resultText = it }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("系统 DNS") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    private suspend fun testLookup(
        name: String,
        hostname: String,
        providerFactory: () -> AAFHttpDnsProvider,
        onResult: (String) -> Unit
    ) {
        if (hostname.isEmpty()) {
            onResult("请输入域名")
            return
        }

        val start = System.currentTimeMillis()
        val text = try {
            withContext(Dispatchers.IO) {
                val provider = providerFactory()
                AAFHttpDnsManager.init(provider, AAFDnsConfig(cacheExpireMs = 0))
                val result = AAFHttpDnsManager.lookup(hostname)
                val elapsed = System.currentTimeMillis() - start
                buildString {
                    appendLine("[$name]  耗时: ${elapsed}ms")
                    appendLine()
                    if (result.isNullOrEmpty()) {
                        appendLine("❌ 解析失败")
                    } else {
                        appendLine("✅ 解析成功，共 ${result.size} 个 IP：")
                        result.forEach { ip ->
                            appendLine("  → ${ip.hostAddress}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - start
            buildString {
                appendLine("[$name]  耗时: ${elapsed}ms")
                appendLine()
                appendLine("❌ 异常: ${e.message}")
            }
        }
        onResult(text)
    }

    private suspend fun testSystemDns(hostname: String, onResult: (String) -> Unit) {
        if (hostname.isEmpty()) {
            onResult("请输入域名")
            return
        }

        val start = System.currentTimeMillis()
        val text = try {
            withContext(Dispatchers.IO) {
                val result = InetAddress.getAllByName(hostname)
                val elapsed = System.currentTimeMillis() - start
                buildString {
                    appendLine("[系统 DNS]  耗时: ${elapsed}ms")
                    appendLine()
                    appendLine("✅ 解析成功，共 ${result.size} 个 IP：")
                    result.forEach { ip ->
                        appendLine("  → ${ip.hostAddress}")
                    }
                }
            }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - start
            buildString {
                appendLine("[系统 DNS]  耗时: ${elapsed}ms")
                appendLine()
                appendLine("❌ 异常: ${e.message}")
            }
        }
        onResult(text)
    }
}