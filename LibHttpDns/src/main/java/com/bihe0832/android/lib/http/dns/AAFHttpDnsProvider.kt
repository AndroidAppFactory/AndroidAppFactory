package com.bihe0832.android.lib.http.dns

import java.net.InetAddress

/**
 * HTTPDNS 提供者接口
 *
 * 业务方可实现此接口，接入自定义 HTTPDNS 服务（如腾讯云 HTTPDNS、阿里云 HTTPDNS 等）。
 * 实现类通过 [AAFHttpDnsManager.init] 注入全局管理器。
 *
 * @author zixie code@bihe0832.com
 * Created on 2026-06-29
 */
interface AAFHttpDnsProvider {

    /** 提供者名称，用于日志标识 */
    val name: String

    /**
     * 解析域名对应的 IP 地址列表
     *
     * @param hostname 待解析的域名
     * @return 解析到的 IP 地址列表，失败时返回 null
     */
    fun lookup(hostname: String): List<InetAddress>?

    /**
     * 批量预解析域名
     *
     * 默认空实现，子类可覆写实现提前预热 DNS 缓存。
     *
     * @param hostnames 待预解析的域名列表
     */
    fun preload(hostnames: List<String>) {}
}
