package com.bihe0832.android.lib.download.core

import com.bihe0832.android.lib.download.DownloadErrorCode
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

/**
 * 下载异常分析工具类
 *
 * 根据异常类型和上下文信息，返回精确的内部错误码，
 * 用于判断是否可恢复重试。对外回调时会收敛为旧版本错误码。
 *
 * @author zixie code@bihe0832.com
 * Created on 2025-02-03
 */
object DownloadExceptionAnalyzer {

    // ==================== 内部错误码（不对外暴露） ====================
    // 网络相关错误（可恢复）
    private const val INTERNAL_ERR_NETWORK_TIMEOUT = -101           // 网络超时
    private const val INTERNAL_ERR_NETWORK_UNREACHABLE = -102       // 网络不可达
    private const val INTERNAL_ERR_NETWORK_CONNECTION_RESET = -103  // 连接被重置
    private const val INTERNAL_ERR_NETWORK_CONNECTION_REFUSED = -104 // 连接被拒绝
    private const val INTERNAL_ERR_NETWORK_SSL_HANDSHAKE = -105     // SSL握手失败
    private const val INTERNAL_ERR_NETWORK_IO_EXCEPTION = -106      // 其他网络IO异常

    // HTTP 响应错误
    private const val INTERNAL_ERR_HTTP_SERVER_ERROR = -150         // 服务器错误(5xx) - 可重试
    private const val INTERNAL_ERR_HTTP_TOO_MANY_REQUESTS = -151    // 请求过多(429) - 可重试
    private const val INTERNAL_ERR_HTTP_REQUEST_TIMEOUT = -152      // 请求超时(408) - 可重试
    private const val INTERNAL_ERR_HTTP_SERVICE_UNAVAILABLE = -153  // 服务不可用(503) - 可重试
    private const val INTERNAL_ERR_HTTP_CLIENT_ERROR = -160         // 客户端错误(4xx) - 不可重试
    private const val INTERNAL_ERR_HTTP_NOT_FOUND = -161            // 资源不存在(404)
    private const val INTERNAL_ERR_HTTP_FORBIDDEN = -162            // 禁止访问(403)
    private const val INTERNAL_ERR_HTTP_UNAUTHORIZED = -163         // 未授权(401)
    private const val INTERNAL_ERR_HTTP_OTHER = -169                // 其他HTTP错误

    // 本地 IO 错误（不可恢复）
    private const val INTERNAL_ERR_LOCAL_DISK_FULL = -200           // 磁盘空间不足
    private const val INTERNAL_ERR_LOCAL_PERMISSION_DENIED = -201   // 权限不足
    private const val INTERNAL_ERR_LOCAL_FILE_IO = -202             // 文件读写错误
    private const val INTERNAL_ERR_LOCAL_FILE_NOT_FOUND = -203      // 文件不存在
    private const val INTERNAL_ERR_LOCAL_FILE_LOCKED = -204         // 文件被占用
    private const val INTERNAL_ERR_LOCAL_OTHER = -209               // 其他本地错误

    /**
     * 分析异常并返回精确的内部错误码
     *
     * @param throwable 捕获的异常
     * @param httpStatusCode HTTP 状态码（可选，用于 HTTP 响应错误分析）
     * @return 对应的内部错误码
     */
    @JvmStatic
    @JvmOverloads
    fun analyzeException(throwable: Throwable?, httpStatusCode: Int = 0): Int {
        if (throwable == null) {
            return if (httpStatusCode > 0) {
                analyzeHttpStatusCode(httpStatusCode)
            } else {
                DownloadErrorCode.ERR_DOWNLOAD_EXCEPTION
            }
        }

        // 1. 首先检查网络相关异常
        val networkError = analyzeNetworkException(throwable)
        if (networkError != null) {
            return networkError
        }

        // 2. 检查本地 IO 异常
        val localError = analyzeLocalException(throwable)
        if (localError != null) {
            return localError
        }

        // 3. 如果有 HTTP 状态码，分析 HTTP 响应错误
        if (httpStatusCode > 0) {
            return analyzeHttpStatusCode(httpStatusCode)
        }

        // 4. 兜底：返回通用下载异常
        return DownloadErrorCode.ERR_DOWNLOAD_EXCEPTION
    }

    /**
     * 分析网络相关异常
     *
     * @return 网络错误码，如果不是网络异常则返回 null
     */
    private fun analyzeNetworkException(throwable: Throwable): Int? {
        return when (throwable) {
            // 超时异常
            is SocketTimeoutException -> INTERNAL_ERR_NETWORK_TIMEOUT
            is InterruptedIOException -> {
                // InterruptedIOException 的子类 SocketTimeoutException 已在上面处理
                // 其他 InterruptedIOException 也视为超时
                INTERNAL_ERR_NETWORK_TIMEOUT
            }

            // 网络不可达
            is UnknownHostException -> INTERNAL_ERR_NETWORK_UNREACHABLE
            is NoRouteToHostException -> INTERNAL_ERR_NETWORK_UNREACHABLE

            // 连接异常
            is ConnectException -> {
                val message = throwable.message?.lowercase() ?: ""
                when {
                    message.contains("refused") -> INTERNAL_ERR_NETWORK_CONNECTION_REFUSED
                    message.contains("reset") -> INTERNAL_ERR_NETWORK_CONNECTION_RESET
                    message.contains("timeout") -> INTERNAL_ERR_NETWORK_TIMEOUT
                    else -> INTERNAL_ERR_NETWORK_CONNECTION_REFUSED
                }
            }

            // Socket 异常
            is SocketException -> {
                val message = throwable.message?.lowercase() ?: ""
                when {
                    message.contains("reset") -> INTERNAL_ERR_NETWORK_CONNECTION_RESET
                    message.contains("broken pipe") -> INTERNAL_ERR_NETWORK_CONNECTION_RESET
                    message.contains("connection abort") -> INTERNAL_ERR_NETWORK_CONNECTION_RESET
                    else -> INTERNAL_ERR_NETWORK_IO_EXCEPTION
                }
            }

            // SSL/TLS 异常
            is SSLHandshakeException -> INTERNAL_ERR_NETWORK_SSL_HANDSHAKE
            is SSLException -> {
                val message = throwable.message?.lowercase() ?: ""
                when {
                    message.contains("handshake") -> INTERNAL_ERR_NETWORK_SSL_HANDSHAKE
                    message.contains("certificate") -> INTERNAL_ERR_NETWORK_SSL_HANDSHAKE
                    else -> INTERNAL_ERR_NETWORK_IO_EXCEPTION
                }
            }

            // 通用 IOException - 需要进一步分析
            is IOException -> {
                val message = throwable.message?.lowercase() ?: ""
                when {
                    // 网络相关关键词
                    message.contains("network") -> INTERNAL_ERR_NETWORK_IO_EXCEPTION
                    message.contains("connection") -> INTERNAL_ERR_NETWORK_CONNECTION_RESET
                    message.contains("timeout") -> INTERNAL_ERR_NETWORK_TIMEOUT
                    message.contains("reset") -> INTERNAL_ERR_NETWORK_CONNECTION_RESET
                    message.contains("broken pipe") -> INTERNAL_ERR_NETWORK_CONNECTION_RESET
                    message.contains("socket") -> INTERNAL_ERR_NETWORK_IO_EXCEPTION
                    message.contains("host") -> INTERNAL_ERR_NETWORK_UNREACHABLE
                    message.contains("unreachable") -> INTERNAL_ERR_NETWORK_UNREACHABLE

                    // 本地 IO 相关关键词（交给 analyzeLocalException 处理）
                    message.contains("space") -> null
                    message.contains("permission") -> null
                    message.contains("denied") -> null
                    message.contains("disk") -> null
                    message.contains("locked") -> null

                    // 默认不认定为网络异常
                    else -> null
                }
            }

            else -> null
        }
    }

    /**
     * 分析本地 IO 异常
     *
     * @return 本地错误码，如果不是本地异常则返回 null
     */
    private fun analyzeLocalException(throwable: Throwable): Int? {
        return when (throwable) {
            is FileNotFoundException -> INTERNAL_ERR_LOCAL_FILE_NOT_FOUND

            is SecurityException -> INTERNAL_ERR_LOCAL_PERMISSION_DENIED

            is IOException -> {
                val message = throwable.message?.lowercase() ?: ""
                when {
                    // 磁盘空间不足
                    message.contains("no space") -> INTERNAL_ERR_LOCAL_DISK_FULL
                    message.contains("disk full") -> INTERNAL_ERR_LOCAL_DISK_FULL
                    message.contains("not enough space") -> INTERNAL_ERR_LOCAL_DISK_FULL
                    message.contains("enospc") -> INTERNAL_ERR_LOCAL_DISK_FULL
                    message.contains("out of space") -> INTERNAL_ERR_LOCAL_DISK_FULL

                    // 权限问题
                    message.contains("permission denied") -> INTERNAL_ERR_LOCAL_PERMISSION_DENIED
                    message.contains("access denied") -> INTERNAL_ERR_LOCAL_PERMISSION_DENIED
                    message.contains("eacces") -> INTERNAL_ERR_LOCAL_PERMISSION_DENIED

                    // 文件被占用
                    message.contains("locked") -> INTERNAL_ERR_LOCAL_FILE_LOCKED
                    message.contains("in use") -> INTERNAL_ERR_LOCAL_FILE_LOCKED
                    message.contains("sharing violation") -> INTERNAL_ERR_LOCAL_FILE_LOCKED

                    // 文件不存在
                    message.contains("no such file") -> INTERNAL_ERR_LOCAL_FILE_NOT_FOUND
                    message.contains("file not found") -> INTERNAL_ERR_LOCAL_FILE_NOT_FOUND
                    message.contains("enoent") -> INTERNAL_ERR_LOCAL_FILE_NOT_FOUND

                    // 其他文件 IO 错误
                    message.contains("read-only") -> INTERNAL_ERR_LOCAL_FILE_IO
                    message.contains("i/o error") -> INTERNAL_ERR_LOCAL_FILE_IO

                    // 不是本地错误
                    else -> null
                }
            }

            else -> null
        }
    }

    /**
     * 分析 HTTP 状态码
     *
     * @param statusCode HTTP 响应状态码
     * @return 对应的内部错误码
     */
    @JvmStatic
    fun analyzeHttpStatusCode(statusCode: Int): Int {
        return when (statusCode) {
            // 2xx 成功
            in 200..299 -> DownloadErrorCode.SUCC

            // 4xx 客户端错误
            400 -> INTERNAL_ERR_HTTP_CLIENT_ERROR
            401 -> INTERNAL_ERR_HTTP_UNAUTHORIZED
            403 -> INTERNAL_ERR_HTTP_FORBIDDEN
            404 -> INTERNAL_ERR_HTTP_NOT_FOUND
            408 -> INTERNAL_ERR_HTTP_REQUEST_TIMEOUT
            429 -> INTERNAL_ERR_HTTP_TOO_MANY_REQUESTS
            in 400..499 -> INTERNAL_ERR_HTTP_CLIENT_ERROR

            // 5xx 服务器错误
            500 -> INTERNAL_ERR_HTTP_SERVER_ERROR
            502 -> INTERNAL_ERR_HTTP_SERVER_ERROR
            503 -> INTERNAL_ERR_HTTP_SERVICE_UNAVAILABLE
            504 -> INTERNAL_ERR_HTTP_SERVER_ERROR
            in 500..599 -> INTERNAL_ERR_HTTP_SERVER_ERROR

            // 其他
            else -> INTERNAL_ERR_HTTP_OTHER
        }
    }

    /**
     * 判断错误码是否是可恢复的错误（网络恢复后可自动重试）
     *
     * 可恢复错误包括：
     * - 网络相关错误（超时、连接失败、网络不可达等）
     * - HTTP 5xx 服务器错误
     * - HTTP 429 请求过多
     * - HTTP 408 请求超时
     *
     * @param errorCode 错误码（内部错误码）
     * @return true 表示可恢复，false 表示不可恢复
     */
    @JvmStatic
    fun isRecoverableError(errorCode: Int): Boolean {
        return when (errorCode) {
            // 网络相关错误 - 全部可恢复
            INTERNAL_ERR_NETWORK_TIMEOUT,
            INTERNAL_ERR_NETWORK_UNREACHABLE,
            INTERNAL_ERR_NETWORK_CONNECTION_RESET,
            INTERNAL_ERR_NETWORK_CONNECTION_REFUSED,
            INTERNAL_ERR_NETWORK_SSL_HANDSHAKE,
            INTERNAL_ERR_NETWORK_IO_EXCEPTION -> true

            // HTTP 服务器错误 - 可重试
            INTERNAL_ERR_HTTP_SERVER_ERROR,
            INTERNAL_ERR_HTTP_TOO_MANY_REQUESTS,
            INTERNAL_ERR_HTTP_REQUEST_TIMEOUT,
            INTERNAL_ERR_HTTP_SERVICE_UNAVAILABLE -> true

            // 旧版本兼容：获取长度失败通常是网络问题
            DownloadErrorCode.ERR_HTTP_LENGTH_FAILED -> true

            // HTTP 4xx 客户端错误 - 不可恢复
            INTERNAL_ERR_HTTP_CLIENT_ERROR,
            INTERNAL_ERR_HTTP_NOT_FOUND,
            INTERNAL_ERR_HTTP_FORBIDDEN,
            INTERNAL_ERR_HTTP_UNAUTHORIZED -> false

            // 本地错误 - 不可恢复
            INTERNAL_ERR_LOCAL_DISK_FULL,
            INTERNAL_ERR_LOCAL_PERMISSION_DENIED,
            INTERNAL_ERR_LOCAL_FILE_IO,
            INTERNAL_ERR_LOCAL_FILE_NOT_FOUND,
            INTERNAL_ERR_LOCAL_FILE_LOCKED,
            INTERNAL_ERR_LOCAL_OTHER -> false

            // 分片汇总失败 - 不自动重试
            // 分片级别的重试已在 DownloadThread 中处理，这里是汇总结果
            DownloadErrorCode.ERR_DOWNLOAD_PART_EXCEPTION -> false

            // 以下为防御性代码：正常流程不会到达（analyzeException 不会返回这些错误码）
            DownloadErrorCode.ERR_DOWNLOAD_EXCEPTION,
            DownloadErrorCode.ERR_HTTP_EXCEPTION,
            DownloadErrorCode.ERR_DOWNLOAD_PART_START_EXCEPTION,
            DownloadErrorCode.ERR_HTTP_FAILED,
            DownloadErrorCode.ERR_CONTENT_LENGTH_EXCEPTION -> false

            // 其他错误 - 默认不可恢复
            else -> false
        }
    }

    /**
     * 将内部细化错误码转换为对外错误码
     *
     * 内部使用细化错误码进行精确的重试判断，
     * 但对外回调时收敛为旧版本错误码，保持 API 稳定性。
     *
     * 映射规则：
     * - 网络错误 (-101 ~ -106) -> ERR_HTTP_EXCEPTION (-11)
     * - HTTP 服务器错误 (-150 ~ -153) -> ERR_HTTP_FAILED (-5)
     * - HTTP 客户端错误 (-160 ~ -169) -> ERR_HTTP_FAILED (-5)
     * - 本地 IO 错误 (-200 ~ -209) -> ERR_DOWNLOAD_EXCEPTION (-6)
     * - 其他错误码保持不变
     *
     * @param internalErrorCode 内部细化错误码
     * @return 对外错误码
     */
    @JvmStatic
    fun toExternalErrorCode(internalErrorCode: Int): Int {
        return when (internalErrorCode) {
            // 网络相关错误 -> ERR_HTTP_EXCEPTION
            INTERNAL_ERR_NETWORK_TIMEOUT,
            INTERNAL_ERR_NETWORK_UNREACHABLE,
            INTERNAL_ERR_NETWORK_CONNECTION_RESET,
            INTERNAL_ERR_NETWORK_CONNECTION_REFUSED,
            INTERNAL_ERR_NETWORK_SSL_HANDSHAKE,
            INTERNAL_ERR_NETWORK_IO_EXCEPTION -> DownloadErrorCode.ERR_HTTP_EXCEPTION

            // HTTP 服务器错误 -> ERR_HTTP_FAILED
            INTERNAL_ERR_HTTP_SERVER_ERROR,
            INTERNAL_ERR_HTTP_TOO_MANY_REQUESTS,
            INTERNAL_ERR_HTTP_REQUEST_TIMEOUT,
            INTERNAL_ERR_HTTP_SERVICE_UNAVAILABLE -> DownloadErrorCode.ERR_HTTP_FAILED

            // HTTP 客户端错误 -> ERR_HTTP_FAILED
            INTERNAL_ERR_HTTP_CLIENT_ERROR,
            INTERNAL_ERR_HTTP_NOT_FOUND,
            INTERNAL_ERR_HTTP_FORBIDDEN,
            INTERNAL_ERR_HTTP_UNAUTHORIZED,
            INTERNAL_ERR_HTTP_OTHER -> DownloadErrorCode.ERR_HTTP_FAILED

            // 本地 IO 错误 -> ERR_DOWNLOAD_EXCEPTION
            INTERNAL_ERR_LOCAL_DISK_FULL,
            INTERNAL_ERR_LOCAL_PERMISSION_DENIED,
            INTERNAL_ERR_LOCAL_FILE_IO,
            INTERNAL_ERR_LOCAL_FILE_NOT_FOUND,
            INTERNAL_ERR_LOCAL_FILE_LOCKED,
            INTERNAL_ERR_LOCAL_OTHER -> DownloadErrorCode.ERR_DOWNLOAD_EXCEPTION

            // 其他错误码保持不变（包括旧版本错误码）
            else -> internalErrorCode
        }
    }

    /**
     * 获取错误码的可读描述（用于日志）
     *
     * @param errorCode 错误码（内部或外部）
     * @return 错误描述
     */
    @JvmStatic
    fun getErrorDescription(errorCode: Int): String {
        return when (errorCode) {
            // 成功
            DownloadErrorCode.SUCC -> "下载成功"
            DownloadErrorCode.HAS_DOWMLOAD -> "文件已下载"

            // 内部网络错误
            INTERNAL_ERR_NETWORK_TIMEOUT -> "网络超时"
            INTERNAL_ERR_NETWORK_UNREACHABLE -> "网络不可达"
            INTERNAL_ERR_NETWORK_CONNECTION_RESET -> "连接被重置"
            INTERNAL_ERR_NETWORK_CONNECTION_REFUSED -> "连接被拒绝"
            INTERNAL_ERR_NETWORK_SSL_HANDSHAKE -> "SSL握手失败"
            INTERNAL_ERR_NETWORK_IO_EXCEPTION -> "网络IO异常"

            // 内部 HTTP 错误
            INTERNAL_ERR_HTTP_SERVER_ERROR -> "服务器错误(5xx)"
            INTERNAL_ERR_HTTP_TOO_MANY_REQUESTS -> "请求过于频繁(429)"
            INTERNAL_ERR_HTTP_REQUEST_TIMEOUT -> "请求超时(408)"
            INTERNAL_ERR_HTTP_SERVICE_UNAVAILABLE -> "服务不可用(503)"
            INTERNAL_ERR_HTTP_CLIENT_ERROR -> "客户端错误(4xx)"
            INTERNAL_ERR_HTTP_NOT_FOUND -> "资源不存在(404)"
            INTERNAL_ERR_HTTP_FORBIDDEN -> "禁止访问(403)"
            INTERNAL_ERR_HTTP_UNAUTHORIZED -> "未授权(401)"
            INTERNAL_ERR_HTTP_OTHER -> "HTTP错误"

            // 内部本地错误
            INTERNAL_ERR_LOCAL_DISK_FULL -> "磁盘空间不足"
            INTERNAL_ERR_LOCAL_PERMISSION_DENIED -> "权限不足"
            INTERNAL_ERR_LOCAL_FILE_IO -> "文件读写错误"
            INTERNAL_ERR_LOCAL_FILE_NOT_FOUND -> "文件不存在"
            INTERNAL_ERR_LOCAL_FILE_LOCKED -> "文件被占用"
            INTERNAL_ERR_LOCAL_OTHER -> "本地错误"

            // 外部通用错误
            DownloadErrorCode.ERR_BAD_URL -> "下载地址无效"
            DownloadErrorCode.ERR_MD5_BAD -> "文件校验失败"
            DownloadErrorCode.ERR_MAX_RETRY_EXCEEDED -> "超过最大重试次数"
            DownloadErrorCode.ERR_HTTP_FAILED -> "HTTP请求失败"
            DownloadErrorCode.ERR_DOWNLOAD_EXCEPTION -> "下载异常"
            DownloadErrorCode.ERR_HTTP_EXCEPTION -> "HTTP异常"

            else -> "未知错误($errorCode)"
        }
    }
}
