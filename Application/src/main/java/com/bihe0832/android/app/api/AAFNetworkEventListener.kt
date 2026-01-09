package com.bihe0832.android.app.api

import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.RequestRecord
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.event.AAFOkHttpNetworkEventListener

/**
 * AAF 网络事件监听器
 *
 * 继承自 AAFOkHttpNetworkEventListener，用于监听 OkHttp 网络请求事件
 * 支持请求追踪和日志记录功能
 *
 * @param enableTrace 是否启用请求追踪
 * @param enableLog 是否启用日志记录
 * @param listener 外部事件监听器
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/6/28.
 */
class AAFNetworkEventListener(
    enableTrace: Boolean, enableLog: Boolean = false, listener: okhttp3.EventListener?
) : AAFOkHttpNetworkEventListener(enableTrace, enableLog, listener) {

    /**
     * 记录请求日志
     *
     * 当启用日志记录时，将请求记录写入服务器日志文件
     *
     * @param record 请求记录数据
     */
    override fun logRequest(record: RequestRecord?) {
        if (enableLog) {
            AAFLoggerFile.logServer("$record")
        }
    }
}
