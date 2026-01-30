/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/28 下午3:13
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/28 下午12:06
 *
 */

package com.bihe0832.android.lib.okhttp.wrapper.interceptor.data;

import android.os.SystemClock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求耗时记录
 */
public class RequestTraceTimeRecord implements Serializable {

    public static String EVENT_CALL_START = "callStart";
    public static String EVENT_CALL_END = "callEnd";
    public static String EVENT_DNS_START = "dnsStart";
    public static String EVENT_DNS_END = "dnsEnd";
    public static String EVENT_CONNECT_START = "connectStart";
    public static String EVENT_SECURE_CONNECT_START = "secureConnectStart";
    public static String EVENT_SECURE_CONNECT_END = "secureConnectEnd";
    public static String EVENT_CONNECT_END = "connectEnd";
    public static String EVENT_REQUEST_HEADERS_START = "requestHeadersStart";
    public static String EVENT_REQUEST_HEADERS_END = "requestHeadersEnd";
    public static String EVENT_REQUEST_BODY_START = "requestBodyStart";
    public static String EVENT_REQUEST_BODY_END = "requestBodyEnd";

    public static String EVENT_RESPONSE_HEADERS_START = "responseHeadersStart";
    public static String EVENT_RESPONSE_HEADERS_END = "responseHeadersEnd";
    public static String EVENT_RESPONSE_BODY_START = "responseBodyStart";
    public static String EVENT_RESPONSE_BODY_END = "responseBodyEnd";
    public static String TRACE_NAME_TOTAL = "Total Time";
    public static String TRACE_NAME_DNS = "DNS";
    public static String TRACE_NAME_SECURE_CONNECT = "Secure Connect";
    public static String TRACE_NAME_CONNECT = "Connect";
    public static String TRACE_NAME_REQUEST_HEADERS = "Request Headers";
    public static String TRACE_NAME_REQUEST_BODY = "Request Body";
    public static String TRACE_NAME_RESPONSE_HEADERS = "Response Headers";
    public static String TRACE_NAME_RESPONSE_BODY = "Response Body";

    private String mTraceRequestId = "";
    private String mContentRequestId = "";
    private String mUrl = "";
    private String mErrorMsg = "";  // 错误信息，在 callFailed 时设置
    private final Map<String, Long> mNetworkEventTimeMap = new HashMap<>();
    private final Map<String, Long> mTraceDataItemList = new HashMap<>();

    public String getTraceRequestId() {
        return mTraceRequestId;
    }

    public String getContentRequestId() {
        return mContentRequestId;
    }

    public void setContentRequestId(String mContentRequestId) {
        this.mContentRequestId = mContentRequestId;
    }

    public void setTraceRequestId(String mRequestId) {
        this.mTraceRequestId = mRequestId;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public String getErrorMsg() {
        return mErrorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.mErrorMsg = errorMsg;
    }

    public Map<String, Long> getNetworkEventTimeMap() {
        return mNetworkEventTimeMap;
    }

    public Map<String, Long> getTraceItemList() {
        return mTraceDataItemList;
    }

    public void saveEvent(String eventName) {
        mNetworkEventTimeMap.put(eventName, SystemClock.elapsedRealtime());
    }

    public void generateTraceData() {
        mTraceDataItemList.clear();
        mTraceDataItemList.put(RequestTraceTimeRecord.TRACE_NAME_TOTAL, getEventCostTime(RequestTraceTimeRecord.EVENT_CALL_START, RequestTraceTimeRecord.EVENT_CALL_END));
        mTraceDataItemList.put(RequestTraceTimeRecord.TRACE_NAME_DNS, getEventCostTime(RequestTraceTimeRecord.EVENT_DNS_START, RequestTraceTimeRecord.EVENT_DNS_END));
        mTraceDataItemList.put(RequestTraceTimeRecord.TRACE_NAME_SECURE_CONNECT, getEventCostTime(RequestTraceTimeRecord.EVENT_SECURE_CONNECT_START, RequestTraceTimeRecord.EVENT_SECURE_CONNECT_END));
        mTraceDataItemList.put(RequestTraceTimeRecord.TRACE_NAME_CONNECT, getEventCostTime(RequestTraceTimeRecord.EVENT_CONNECT_START, RequestTraceTimeRecord.EVENT_CONNECT_END));
        mTraceDataItemList.put(RequestTraceTimeRecord.TRACE_NAME_REQUEST_HEADERS, getEventCostTime(RequestTraceTimeRecord.EVENT_REQUEST_HEADERS_START, RequestTraceTimeRecord.EVENT_REQUEST_HEADERS_END));
        mTraceDataItemList.put(RequestTraceTimeRecord.TRACE_NAME_REQUEST_BODY, getEventCostTime(RequestTraceTimeRecord.EVENT_REQUEST_BODY_START, RequestTraceTimeRecord.EVENT_REQUEST_BODY_END));
        mTraceDataItemList.put(RequestTraceTimeRecord.TRACE_NAME_RESPONSE_HEADERS, getEventCostTime(RequestTraceTimeRecord.EVENT_RESPONSE_HEADERS_START, RequestTraceTimeRecord.EVENT_RESPONSE_HEADERS_END));
        mTraceDataItemList.put(RequestTraceTimeRecord.TRACE_NAME_RESPONSE_BODY, getEventCostTime(RequestTraceTimeRecord.EVENT_RESPONSE_BODY_START, RequestTraceTimeRecord.EVENT_RESPONSE_BODY_END));
    }

    public long getEventCostTime(String startName, String endName) {
        if (!mNetworkEventTimeMap.containsKey(startName) || !mNetworkEventTimeMap.containsKey(endName)) {
            return 0;
        }
        Long endTime = mNetworkEventTimeMap.get(endName);
        Long start = mNetworkEventTimeMap.get(startName);
        long result = endTime - start;
        return result;
    }
}
