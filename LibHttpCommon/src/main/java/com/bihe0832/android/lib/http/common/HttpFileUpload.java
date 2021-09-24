package com.bihe0832.android.lib.http.common;

import static com.bihe0832.android.lib.http.common.BaseConnection.BOUNDARY;
import static com.bihe0832.android.lib.http.common.BaseConnection.HTTP_REQ_PROPERTY_CHARSET;
import static com.bihe0832.android.lib.http.common.BaseConnection.HTTP_REQ_PROPERTY_CONTENT_TYPE;
import static com.bihe0832.android.lib.http.common.BaseConnection.HTTP_REQ_VALUE_CHARSET;
import static com.bihe0832.android.lib.http.common.BaseConnection.LINE_END;
import static com.bihe0832.android.lib.http.common.BaseConnection.PREFIX;
import static com.bihe0832.android.lib.http.common.HttpBasicRequest.HTTP_REQ_ENTITY_MERGE;
import static com.bihe0832.android.lib.http.common.HttpBasicRequest.LOG_TAG;

import android.text.TextUtils;
import com.bihe0832.android.lib.log.ZLog;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hardyshi code@bihe0832.com Created on 8/20/21.
 */
public class HttpFileUpload {

    /**
     * post请求方法
     */
    public static String fileUpload(final BaseConnection baseConnection, final Map<String, String> strParams,
            final File file, final String fileDataType) {

        baseConnection.setURLConnectionCommonPara();
        HashMap<String, String> requestProperty = new HashMap<>();
        requestProperty.put(HTTP_REQ_PROPERTY_CHARSET, HTTP_REQ_VALUE_CHARSET);
        requestProperty.put(HTTP_REQ_PROPERTY_CONTENT_TYPE, BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_FORM);
        baseConnection.setURLConnectionRequestProperty(requestProperty);
        HttpURLConnection urlConnection = baseConnection.getURLConnection();
        DataOutputStream paramDataOutputStream = null;
        InputStream paramInputStream = null;
        InputStream resultInptStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            urlConnection.setRequestMethod(BaseConnection.HTTP_REQ_METHOD_POST);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty(HTTP_REQ_PROPERTY_CONTENT_TYPE,
                    BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_FORM + ";boundary=" + BOUNDARY);

            paramDataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            paramDataOutputStream.writeBytes(getFormDataString(strParams));
            paramDataOutputStream.flush();

            String fileParam = getFileFormDataParams(fileDataType, file.getName(), file.length());
            paramDataOutputStream.writeBytes(fileParam);
            paramDataOutputStream.flush();
            paramInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = paramInputStream.read(buffer)) != -1) {
                paramDataOutputStream.write(buffer, 0, len);
            }
            paramDataOutputStream.writeBytes(LINE_END);
            //请求结束标志
            paramDataOutputStream.writeBytes(PREFIX + BOUNDARY + PREFIX + LINE_END);
            paramDataOutputStream.flush();
            ZLog.e(LOG_TAG, "postResponseCode() = " + urlConnection.getResponseCode());
            int response = urlConnection.getResponseCode();            //获得服务器的响应码
            if (response == HttpURLConnection.HTTP_OK) {
                resultInptStream = urlConnection.getInputStream();
                byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] resultBuffer = new byte[8192];
                int resultLen;
                while ((resultLen = resultInptStream.read(resultBuffer)) != -1) {
                    byteArrayOutputStream.write(resultBuffer, 0, resultLen);
                }
                resultInptStream.close();
                return byteArrayOutputStream.toString(HTTP_REQ_VALUE_CHARSET);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (paramDataOutputStream != null) {
                    paramDataOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (paramInputStream != null) {
                    paramInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (resultInptStream != null) {
                    resultInptStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "";

    }

    private static String getFileFormDataParams(String fileDataType, String filename, long filelength) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(BaseConnection.PREFIX)
                .append(BaseConnection.BOUNDARY)
                .append(BaseConnection.LINE_END)
                /**
                 * 这里重点注意： name里面的值为服务端需要的key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的 比如:abc.png
                 *
                 * 此处的ContentType不同于 请求头 中Content-Type
                 */
                .append(BaseConnection.HTTP_REQ_PROPERTY_CONTENT_DISPOSITION)
                .append(": ").append(BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_FORM).append("; name")
                .append(HTTP_REQ_ENTITY_MERGE).append("\"").append(fileDataType)
                .append("\"; filename").append(HTTP_REQ_ENTITY_MERGE).append("\"").append(filename)
                .append("\"")
                .append("\"; filelength").append(HTTP_REQ_ENTITY_MERGE).append("\"").append(filelength).append("\"")
                .append(BaseConnection.LINE_END)
                .append(HTTP_REQ_PROPERTY_CONTENT_TYPE)
                .append(": ").append(BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_OCTET_STREAM)
                .append(BaseConnection.LINE_END)
                .append(BaseConnection.HTTP_REQ_PROPERTY_CONTENT_TRANSFER_ENCODING).append(": 8bit")
                .append(BaseConnection.LINE_END)
                .append(BaseConnection.LINE_END);
        String result = stringBuffer.toString();
        ZLog.e(LOG_TAG, "getFileFormDataParams = " + result);
        return stringBuffer.toString();
    }

    /**
     * 对post参数进行编码处理
     */
    private static String getFormDataString(Map<String, String> strParams) {
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, String> entry : strParams.entrySet()) {
            if (TextUtils.isEmpty(entry.getKey()) && TextUtils.isEmpty(entry.getValue())) {
                break;
            } else {
                stringBuffer.append(BaseConnection.PREFIX)
                        .append(BaseConnection.BOUNDARY)
                        .append(BaseConnection.LINE_END)
                        .append(BaseConnection.HTTP_REQ_PROPERTY_CONTENT_DISPOSITION)
                        .append(": ").append(BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_FORM).append(";name")
                        .append(HTTP_REQ_ENTITY_MERGE).append("\"").append(entry.getKey())
                        .append("\"")
                        .append(BaseConnection.LINE_END)
                        .append(HTTP_REQ_PROPERTY_CONTENT_TYPE)
                        .append(": ").append(BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_TEXT).append("; ")
                        .append(HTTP_REQ_PROPERTY_CHARSET).append(HTTP_REQ_ENTITY_MERGE)
                        .append(BaseConnection.HTTP_REQ_VALUE_CHARSET).append(BaseConnection.LINE_END)
                        .append(BaseConnection.HTTP_REQ_PROPERTY_CONTENT_TRANSFER_ENCODING).append(": 8bit")
                        .append(BaseConnection.LINE_END)
                        .append(BaseConnection.LINE_END)// 参数头设置完以后需要两个换行，然后才是参数内容
                        .append(entry.getValue())
                        .append(BaseConnection.LINE_END);

            }
        }
        String result = stringBuffer.toString();
        ZLog.e(LOG_TAG, "getFormDataString = " + result);
        return stringBuffer.toString();

    }
}
