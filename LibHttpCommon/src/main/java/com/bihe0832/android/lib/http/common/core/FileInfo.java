package com.bihe0832.android.lib.http.common.core;

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
    private long fileLength = 0L;
    private String fileName = "";

    private String fileDataType = "";

    public FileInfo(String filePath, String key, String fileType) {
        this.file = new File(filePath);
        this.keyName = key;
        this.fileDataType = fileType;
        this.fileLength = this.file.length();
        this.fileName = this.file.getName();
    }


    public FileInfo(Uri uri, String key, String fileType, String fileName, long fileLength) {
        this.fileUri = uri;
        this.keyName = key;
        this.fileDataType = fileType;
        this.fileName = fileName;
        this.fileLength = fileLength;
    }

    public File getFile() {
        return file;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public String getRequesetData(String uuid) {
        if (null == file && fileUri == null) {
            ZLog.e(LOG_TAG, "getFormDataString bad file path and uri");
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(BaseConnection.HTTP_REQ_ENTITY_PREFIX).append(uuid)
                .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                /**
                 * 这里重点注意： name里面的值为服务端需要的key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的 比如:abc.png
                 *
                 * 此处的ContentType不同于 请求头 中Content-Type
                 */
                .append(BaseConnection.HTTP_REQ_PROPERTY_CONTENT_DISPOSITION).append(": ").append("form-data")
                .append(BaseConnection.HTTP_REQ_ENTITY_END)
                .append("name").append(HTTP_REQ_ENTITY_MERGE).append("\"").append(keyName).append("\"")
                .append(BaseConnection.HTTP_REQ_ENTITY_END)
                .append("filename").append(HTTP_REQ_ENTITY_MERGE).append("\"").append(fileName).append("\"")
                .append(BaseConnection.HTTP_REQ_ENTITY_END)
                .append("filelength").append(HTTP_REQ_ENTITY_MERGE).append(fileLength)
                .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END)
                .append(BaseConnection.HTTP_REQ_ENTITY_LINE_END);
        String result = stringBuffer.toString();
        ZLog.w(LOG_TAG, "getFormDataString 000 : \n" + result + " ");
        ZLog.w(LOG_TAG, "getFormDataString 000 : \n" + result.length() + " ");

        return result;
    }
}
