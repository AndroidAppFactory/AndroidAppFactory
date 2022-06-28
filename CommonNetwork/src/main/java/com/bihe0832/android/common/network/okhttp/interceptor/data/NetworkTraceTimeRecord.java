/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/28 下午3:13
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/28 下午12:06
 *
 */

package com.bihe0832.android.common.network.okhttp.interceptor.data;

import android.os.SystemClock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求耗时记录
 */
public class NetworkTraceTimeRecord implements Serializable {

    public static String EVENT_CALL_START = "callStart";
    public static String EVENT_CALL_END = "callEnd";
    public static String EVENT_DNS_START = "dnsStart";
    public static String EVENT_DNS_END = "dnsEnd";
    public static String EVENT_CONNECT_START = "connectStart";
    public static String EVENT_SECURE_CONNECT_START = "secureConnectStart";
    public static String EVENT_SECURE_CONNECT_END = "secureConnectEnd";
    public static String EVENT_CONNECT_END = "connectEnd";
    public static String EVENT_REQUEST_BODY_START = "requestBodyStart";
    public static String EVENT_REQUEST_BODY_END = "requestBodyEnd";
    public static String EVENT_REQUEST_HEADERS_START = "requestHeadersStart";
    public static String EVENT_REQUEST_HEADERS_END = "requestHeadersEnd";
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

    private String mTraceRequestId;
    private String mContentRequestId;
    private String mUrl;
    private Map<String, Long> mNetworkEventTimeMap = new HashMap<>();
    private Map<String, Long> mTtraceDataItemList = new HashMap<>();

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

    public Map<String, Long> getNetworkEventTimeMap() {
        return mNetworkEventTimeMap;
    }

    public Map<String, Long> getTraceItemList() {
        return mTtraceDataItemList;
    }

    public void saveEvent(String eventName) {
        mNetworkEventTimeMap.put(eventName, SystemClock.elapsedRealtime());
    }

    public void generateTraceData() {
        mTtraceDataItemList.clear();
        mTtraceDataItemList.put(NetworkTraceTimeRecord.TRACE_NAME_TOTAL, getEventCostTime(NetworkTraceTimeRecord.EVENT_CALL_START, NetworkTraceTimeRecord.EVENT_CALL_END));
        mTtraceDataItemList.put(NetworkTraceTimeRecord.TRACE_NAME_DNS, getEventCostTime(NetworkTraceTimeRecord.EVENT_DNS_START, NetworkTraceTimeRecord.EVENT_DNS_END));
        mTtraceDataItemList.put(NetworkTraceTimeRecord.TRACE_NAME_SECURE_CONNECT, getEventCostTime(NetworkTraceTimeRecord.EVENT_SECURE_CONNECT_START, NetworkTraceTimeRecord.EVENT_SECURE_CONNECT_END));
        mTtraceDataItemList.put(NetworkTraceTimeRecord.TRACE_NAME_CONNECT, getEventCostTime(NetworkTraceTimeRecord.EVENT_CONNECT_START, NetworkTraceTimeRecord.EVENT_CONNECT_END));
        mTtraceDataItemList.put(NetworkTraceTimeRecord.TRACE_NAME_REQUEST_HEADERS, getEventCostTime(NetworkTraceTimeRecord.EVENT_REQUEST_HEADERS_START, NetworkTraceTimeRecord.EVENT_REQUEST_HEADERS_END));
        mTtraceDataItemList.put(NetworkTraceTimeRecord.TRACE_NAME_REQUEST_BODY, getEventCostTime(NetworkTraceTimeRecord.EVENT_REQUEST_BODY_START, NetworkTraceTimeRecord.EVENT_REQUEST_BODY_END));
        mTtraceDataItemList.put(NetworkTraceTimeRecord.TRACE_NAME_RESPONSE_HEADERS, getEventCostTime(NetworkTraceTimeRecord.EVENT_RESPONSE_HEADERS_START, NetworkTraceTimeRecord.EVENT_RESPONSE_HEADERS_END));
        mTtraceDataItemList.put(NetworkTraceTimeRecord.TRACE_NAME_RESPONSE_BODY, getEventCostTime(NetworkTraceTimeRecord.EVENT_RESPONSE_BODY_START, NetworkTraceTimeRecord.EVENT_RESPONSE_BODY_END));
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
