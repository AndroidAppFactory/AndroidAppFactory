package com.bihe0832.android.lib.download

/**
 * 下载配置类
 *
 * 提供下载库的配置选项，每个 DownloadManager 实例可以拥有独立配置：
 * - HTTP/2 协议支持开关
 * - 分片策略配置
 * - 性能优化选项
 *
 * @author zixie code@bihe0832.com
 * Created on 2025-01-27
 * Description: 下载库配置管理，支持实例级别配置
 *
 * @since 7.2.9
 */
class DownloadClientConfig {

    /**
     * 是否启用 HTTP/2 支持
     * 
     * 默认开启，如果遇到兼容性问题可以关闭降级到 HTTP/1.1
     */
    var enableHttp2 = true

    /**
     * HTTP/2 环境下的最大分片数
     * 
     * HTTP/2 多路复用允许更多的并发请求，可以增加分片数提升速度
     * 建议范围：8-16
     */
    var http2MaxChunks = 10

    /**
     * HTTP/1.1 环境下的最大分片数
     * 
     * HTTP/1.1 每个分片需要独立 TCP 连接，不宜过多
     * 建议范围：3-5
     */
    var http1MaxChunks = 5

    /**
     * HTTP/2 环境下的最小分片大小（字节）
     * 
     * HTTP/2 流控制高效，可以使用更小的分片
     * 默认：512KB
     */
    var http2MinChunkSize = 1024 * 512

    /**
     * HTTP/1.1 环境下的最小分片大小（字节）
     * 
     * HTTP/1.1 握手成本高，应使用较大分片
     * 默认：1MB
     */
    var http1MinChunkSize = 1024 * 1024

    /**
     * 是否启用协议检测缓存
     * 
     * 开启后会缓存服务器的 HTTP/2 支持情况，避免重复检测
     */
    var enableProtocolCache = true

    /**
     * 是否在日志中输出协议信息
     * 
     * 用于调试和性能分析
     */
    var logProtocolInfo = true

    /**
     * 根据当前配置和协议版本获取最大分片数
     * 
     * @param isHttp2 是否为 HTTP/2 协议
     * @return 最大分片数
     */
    fun getMaxChunks(isHttp2: Boolean): Int {
        return if (isHttp2 && enableHttp2) {
            http2MaxChunks
        } else {
            http1MaxChunks
        }
    }

    /**
     * 根据当前配置和协议版本获取最小分片大小
     * 
     * @param isHttp2 是否为 HTTP/2 协议
     * @return 最小分片大小（字节）
     */
    fun getMinChunkSize(isHttp2: Boolean): Long {
        return if (isHttp2 && enableHttp2) {
            http2MinChunkSize.toLong()
        } else {
            http1MinChunkSize.toLong()
        }
    }

    /**
     * 重置为默认配置
     */
    fun reset() {
        enableHttp2 = true
        http2MaxChunks = 10
        http1MaxChunks = 5
        http2MinChunkSize = 1024 * 512
        http1MinChunkSize = 1024 * 1024
        enableProtocolCache = true
        logProtocolInfo = true
    }

    companion object {
        /**
         * 创建默认配置
         * 
         * @return 使用默认值的 DownloadConfig 实例
         */
        fun createDefault(): DownloadClientConfig {
            return DownloadClientConfig()
        }

        /**
         * 创建 HTTP/2 优化配置
         * 
         * 适用于支持 HTTP/2 的高速网络环境
         * 
         * @return HTTP/2 优化的配置实例
         */
        fun createHttp2Optimized(): DownloadClientConfig {
            return DownloadClientConfig().apply {
                enableHttp2 = true
                http2MaxChunks = 12
                http2MinChunkSize = 1024 * 256  // 256KB
            }
        }

        /**
         * 创建兼容模式配置
         * 
         * 禁用 HTTP/2，使用保守的分片策略
         * 
         * @return 兼容模式配置实例
         */
        fun createCompatible(): DownloadClientConfig {
            return DownloadClientConfig().apply {
                enableHttp2 = false
                http1MaxChunks = 3
                http1MinChunkSize = 1024 * 1024 * 2  // 2MB
            }
        }
    }
}
