package com.bihe0832.android.lib.download;

import com.bihe0832.android.lib.log.ZLog;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Protocol;

public class DownloadPartInfo {

    private int mDownloadType = DownloadItem.TYPE_FILE;

    private long mDownloadID;

    private int mPartNo;

    private String mRealDownloadURL;
    private Map<String,String> requestHeader = null;

    private String mFinalFileName;
    private long mPartRangeStart;
    private long mPartLocalStart;
    private long mPartLength;
    private long mPartFinished;
    private long mPartFinishedBefore;
    private int mPartStatus;
    
    /**
     * 分片失败时的具体错误码
     * <p>当 partStatus 为 STATUS_DOWNLOAD_FAILED 时，此字段记录失败的具体原因。
     * <p>用于任务级汇总时判断是否可以自动重试：
     * <ul>
     *   <li>网络相关错误（超时、连接失败等）→ 可重试</li>
     *   <li>本地错误（存储不足、文件锁定等）→ 不可重试</li>
     * </ul>
     * @see com.bihe0832.android.lib.download.core.DownloadExceptionAnalyzer
     */
    private int mPartErrorCode = 0;
    
    // 实际使用的协议版本，从 DownloadItem 传递过来
    private Protocol mProtocol = Protocol.HTTP_1_1;

    public DownloadPartInfo() {
    }

    public String getFinalFileName() {
        return mFinalFileName;
    }

    public void setFinalFileName(String mFinalFileName) {
        this.mFinalFileName = mFinalFileName;
    }

    public long getPartFinishedBefore() {
        return mPartFinishedBefore;
    }

    public void setPartFinishedBefore(long mPartFinishedBefore) {
        this.mPartFinishedBefore = mPartFinishedBefore;
    }

    public long getDownloadID() {
        return mDownloadID;
    }

    public void setDownloadID(long mDownloadID) {
        this.mDownloadID = mDownloadID;
    }

    public String getDownloadPartID() {
        return mDownloadID + "-" + mPartNo;
    }

    public int getPartStatus() {
        return mPartStatus;
    }

    public void setPartStatus(int mPartStatus) {
        ZLog.d(DownloadItem.TAG,
                mDownloadID + "-" + mPartNo + " status change , before: " + this.mPartStatus + " after : "
                        + mPartStatus);
//        if(this.mPartStatus != mPartStatus){
//            reportDownloadPartStatusChange(this, this.mPartStatus, mPartStatus);
//        }
        this.mPartStatus = mPartStatus;
    }

    /**
     * 获取分片失败的具体错误码
     * 
     * @return 错误码，0 表示无错误或未设置
     */
    public int getPartErrorCode() {
        return mPartErrorCode;
    }

    /**
     * 设置分片失败的具体错误码
     * 
     * @param errorCode 具体的错误码
     */
    public void setPartErrorCode(int errorCode) {
        this.mPartErrorCode = errorCode;
    }

    public int getPartID() {
        return mPartNo;
    }

    public void setPartID(int mPartID) {
        this.mPartNo = mPartID;
    }

    public void setDownloadType(int mDownloadType) {
        this.mDownloadType = mDownloadType;
    }

    public String getRealDownloadURL() {
        return mRealDownloadURL;
    }

    public void setRealDownloadURL(String mDownloadURL) {
        this.mRealDownloadURL = mDownloadURL;
    }

    public long getPartFinished() {
        return mPartFinished;
    }

    public void setPartFinished(long mPartFinished) {
        this.mPartFinished = mPartFinished;
    }

    public long getPartLength() {
        return mPartLength;
    }

    public void setPartLength(long mPartLength) {
        this.mPartLength = mPartLength;
    }

    public long getPartLocalStart() {
        return mPartLocalStart;
    }

    public void setPartLocalStart(long mPartLocalStart) {
        this.mPartLocalStart = mPartLocalStart;
    }

    public long getPartRangeStart() {
        return mPartRangeStart;
    }

    public void setPartRangeStart(long mPartRangeStart) {
        this.mPartRangeStart = mPartRangeStart;
    }

    public long getPartRangeEnd() {
        if (mPartLength > 0) {
            return mPartRangeStart + mPartLength - 1;
        } else {
            return 0;
        }
    }

    public Map<String, String> getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(Map<String, String> requestHeader) {
        this.requestHeader = requestHeader;
    }

    public Protocol getProtocol() {
        return mProtocol;
    }

    public void setProtocol(Protocol protocol) {
        if (protocol != null) {
            this.mProtocol = protocol;
        }
    }

    /**
     * 判断是否使用 HTTP/2 协议
     * 
     * @return true 使用 HTTP/2，false 使用 HTTP/1.x
     */
    public boolean isHttp2() {
        return mProtocol == Protocol.HTTP_2;
    }

    @Override
    public String toString() {
        return "DownloadPartInfo{" + "mPartID=" + mPartNo + ", mDownloadID=" + mDownloadID
                + ", mDownloadType=" + mDownloadType + ", mDownloadURL='" + mRealDownloadURL
                + ", mPartRangeStart=" + mPartRangeStart + ", mPartLocalStart=" + mPartLocalStart
                + ", mPartLength=" + mPartLength + ", mPartFinishedBefore=" + mPartFinishedBefore
                + ", mPartFinished=" + mPartFinished + ", mFinalFileName='" + mFinalFileName
                + ", mPartStatus=" + mPartStatus + ", mPartErrorCode=" + mPartErrorCode
                + ", mProtocol=" + mProtocol + '}';
    }
}
