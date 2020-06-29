package com.bihe0832.android.http.common;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpURLConnection封装基类，网络请求，设置请求协议头、发送请求
 *
 * Created by hardyshi on 16/11/22.
 */
public abstract class BaseConnection {

    private  static final String LOG_TAG = "bihe0832 REQUEST";
    protected static final String HTTP_REQ_PROPERTY_CHARSET = "Accept-Charset";
    protected static final String HTTP_REQ_VALUE_CHARSET = "UTF-8";
    protected static final String HTTP_REQ_PROPERTY_CONTENT_TYPE = "Content-Type";
    protected static final String HTTP_REQ_VALUE_CONTENT_TYPE = "application/x-www-form-urlencoded";
    protected static final String HTTP_REQ_PROPERTY_CONTENT_LENGTH = "Content-Length";
    protected static final String HTTP_REQ_METHOD_GET = "GET";
    protected static final String HTTP_REQ_METHOD_POST = "POST";
    protected static final String HTTP_REQ_COOKIE = "Cookie";

    /**
     * 建立连接的超时时间
     */
    protected static final int CONNECT_TIMEOUT = 5 * 1000;
    /**
     * 建立到资源的连接后从 input 流读入时的超时时间
     */
    protected static final int DEFAULT_READ_TIMEOUT = 10 * 1000;

    public BaseConnection() {

    }

    private void setURLConnectionCommonPara(){
        HttpURLConnection connection = getURLConnection();
        if(null == connection){
            return;
        }
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
        connection.setUseCaches(false);
        connection.setRequestProperty(HTTP_REQ_PROPERTY_CHARSET, HTTP_REQ_VALUE_CHARSET);
        connection.setRequestProperty(HTTP_REQ_PROPERTY_CONTENT_TYPE, HTTP_REQ_VALUE_CONTENT_TYPE);
    }

    private void setURLConnectionCookie(HashMap<String,String> cookieInfo){
        HttpURLConnection connection = getURLConnection();
        if(null == connection){
            return;
        }
        String cookieString = connection.getRequestProperty(HTTP_REQ_COOKIE);
        if(!TextUtils.isEmpty(cookieString)){
            cookieString = cookieString + ";";
        }else{
            cookieString = "";
        }
        for (Map.Entry<String, String> entry : cookieInfo.entrySet()) {
            if(TextUtils.isEmpty(entry.getKey()) || TextUtils.isEmpty(entry.getValue())){
                Log.d(LOG_TAG,"cookie inf is bad");
            }else{
                cookieString = cookieString + entry.getKey() + HttpBasicRequest.HTTP_REQ_ENTITY_MERGE + entry.getValue() + ";";
            }
        }
        connection.setRequestProperty(HTTP_REQ_COOKIE,cookieString);
    }

    public String doRequest(HttpBasicRequest request){
        if(null == getURLConnection()){
            Log.e(LOG_TAG,"URLConnection is null");
            return "";
        }
        setURLConnectionCommonPara();
        //检查cookie
        if(null != request.cookieInfo && request.cookieInfo.size() > 0){
            setURLConnectionCookie(request.cookieInfo);
        }

        if(null == request.data){
            return doGetRequest();
        }else{
            return doPostRequest(request.data);
        }
    }

    protected String doGetRequest(){
        String result = "";
        InputStream is = null;
        BufferedReader br = null;
        try {
            HttpURLConnection connection = getURLConnection();
            if(null == connection){
                return "";
            }
            connection.setRequestMethod(HTTP_REQ_METHOD_GET);
            is = connection.getInputStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            is.close();
            result = os.toString(HTTP_REQ_VALUE_CHARSET);
        }catch (javax.net.ssl.SSLHandshakeException ee){
            Log.e(LOG_TAG, "javax.net.ssl.SSLPeerUnverifiedException");
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
            return result;
        }
    }

    protected String doPostRequest(byte[] data){
        BufferedReader br = null;
        InputStream inptStream = null;
        OutputStream outputStream = null;
        try {
            HttpURLConnection connection = getURLConnection();
            if(null == connection){
                return "";
            }
            connection.setRequestMethod(HTTP_REQ_METHOD_POST);
            connection.setRequestProperty(HTTP_REQ_PROPERTY_CONTENT_LENGTH, String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            outputStream = connection.getOutputStream();
            outputStream.write(data);

            int response = connection.getResponseCode();            //获得服务器的响应码
            if(response == HttpURLConnection.HTTP_OK) {
                inptStream = connection.getInputStream();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int len;
                while((len = inptStream.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                inptStream.close();
                return os.toString(HTTP_REQ_VALUE_CHARSET);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (inptStream != null) {
                    inptStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
    public String getResponseMessage(){

        HttpURLConnection connection = getURLConnection();
        if(null == connection){
            return "";
        }else{
            try {
                return getURLConnection().getResponseMessage();
            }catch (IOException e){
                e.printStackTrace();
                return "";
            }
        }
    }
    public int getResponseCode(){
        HttpURLConnection connection = getURLConnection();
        if(null == connection){
            return -1;
        }else{
            try {
                return getURLConnection().getResponseCode();
            }catch (IOException e){
                e.printStackTrace();
                return -1;
            }
        }
    }

    protected abstract HttpURLConnection getURLConnection();

}
