package com.bihe0832.android.lib.http.common.core;

import static com.bihe0832.android.lib.http.common.core.BaseConnection.HTTP_REQ_PROPERTY_CONTENT_TYPE;
import static com.bihe0832.android.lib.http.common.core.HttpBasicRequest.HTTP_REQ_ENTITY_MERGE;
import static com.bihe0832.android.lib.http.common.core.HttpBasicRequest.LOG_TAG;

import android.net.Uri;
import com.bihe0832.android.lib.log.ZLog;
import java.io.File;

/**
 * @author zixie code@bihe0832.com Created on 2021/11/18.
 */
public class FileInfo {

    private File file = null;
    private Uri fileUri = null;
    private String keyName = "";
    private String fileDataType = "";

    public FileInfo(String fileName, String key, String fileType) {
        this.file = new File(fileName);
        this.keyName = key;
        this.fileDataType = fileType;
    }


    public FileInfo(Uri uri, String key, String fileType) {
        this.fileUri = uri;
        this.keyName = key;
        this.fileDataType = fileType;
    }

    public File getFile() {
        return file;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public String getRequesetData(String uuid) {
        if (null == file) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(BaseConnection.HTTP_REQ_ENTITY_PREFIX)
                .append(uuid)
                .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                /**
                 * 这里重点注意： name里面的值为服务端需要的key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的 比如:abc.png
                 *
                 * 此处的ContentType不同于 请求头 中Content-Type
                 */
                .append(BaseConnection.HTTP_REQ_PROPERTY_CONTENT_DISPOSITION).append(": ").append("form-data")
                .append(";")
                .append(" name").append(HTTP_REQ_ENTITY_MERGE).append("\"").append(keyName).append("\";")
                .append(" filename").append(HTTP_REQ_ENTITY_MERGE).append("\"").append(file.getName()).append("\";")
                .append(" filelength").append(HTTP_REQ_ENTITY_MERGE).append(file.length())
                .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                .append(HTTP_REQ_PROPERTY_CONTENT_TYPE).append(": ").append(fileDataType)
                .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                .append(BaseConnection.HTTP_REQ_PROPERTY_CONTENT_TRANSFER_ENCODING).append(": 8bit")
                .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END);
        String result = stringBuffer.toString();
        ZLog.e(LOG_TAG, "getFormDataString 000 : \n " + result);
        ZLog.e(LOG_TAG, "getFormDataString 000 : \n " + result.length());

        return stringBuffer.toString();
    }
}
