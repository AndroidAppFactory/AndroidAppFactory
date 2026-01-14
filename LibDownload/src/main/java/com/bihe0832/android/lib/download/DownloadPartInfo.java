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
                + ", mPartStatus=" + mPartStatus + ", mProtocol=" + mProtocol + '}';
    }
}
