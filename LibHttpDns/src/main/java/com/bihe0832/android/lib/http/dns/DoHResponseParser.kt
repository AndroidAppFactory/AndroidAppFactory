package com.bihe0832.android.lib.http.dns

import com.bihe0832.android.lib.log.ZLog
import org.json.JSONArray
import org.json.JSONObject
import java.net.InetAddress

/**
 * DoH (DNS over HTTPS) JSON 响应解析器
 *
 * 解析符合 Google DNS-over-HTTPS JSON API 规范的响应，提取 A 记录（IPv4）的 IP 地址。
 *
 * 响应格式示例：
 * ```json
 * {
 *   "Status": 0,
 *   "Answer": [
 *     {"name": "example.com", "type": 1, "TTL": 600, "data": "93.184.216.34"}
 *   ]
 * }
 * ```
 *
 * - Status == 0 表示解析成功
 * - Answer[].type == 1 表示 A 记录（IPv4）
 * - Answer[].data 为解析到的 IP 地址字符串
 *
 * @author zixie code@bihe0832.com
 * Created on 2026-06-29
 */
object DoHResponseParser {

    private const val TAG = "DoHResponseParser"

    /**
     * 解析 DoH JSON 响应，提取 A 记录的 IP 地址列表
     *
     * @param jsonBody DoH API 返回的 JSON 字符串
     * @param providerName DNS 提供者名称，用于日志标识
     * @return IP 地址列表，解析失败或无结果返回 null
     */
    fun parse(jsonBody: String, providerName: String): List<InetAddress>? {
        return try {
            val json = JSONObject(jsonBody)
            val status = json.optInt("Status", -1)
            if (status != 0) {
                ZLog.w(TAG, "[$providerName] DoH 响应 Status=$status，非成功状态")
                return null
            }

            val answer: JSONArray = json.optJSONArray("Answer") ?: run {
                ZLog.d(TAG, "[$providerName] DoH 响应无 Answer 字段")
                return null
            }

            val result = mutableListOf<InetAddress>()
            for (i in 0 until answer.length()) {
                val item = answer.getJSONObject(i)
                val type = item.optInt("type", -1)
                if (type == 1) {  // A 记录
                    val data = item.optString("data", "")
                    if (data.isNotEmpty()) {
                        try {
                            result.add(InetAddress.getByName(data))
                        } catch (e: Exception) {
                            ZLog.w(TAG, "[$providerName] DoH 解析 IP 失败: $data, ${e.message}")
                        }
                    }
                }
            }

            if (result.isEmpty()) {
                ZLog.d(TAG, "[$providerName] DoH 响应无 A 记录")
                null
            } else {
                ZLog.d(TAG, "[$providerName] DoH 解析成功，共 ${result.size} 个 A 记录: ${result.map { it.hostAddress }}")
                result
            }
        } catch (e: Exception) {
            ZLog.e(TAG, "[$providerName] DoH JSON 解析异常: ${e.message}")
            null
        }
    }
}
