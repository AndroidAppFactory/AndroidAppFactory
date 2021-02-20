package com.bihe0832.android.lib.download;

import com.bihe0832.android.lib.log.ZLog;

public class DownloadPartInfo {

    private int mPartID;
    private long mDownloadID;
    private String mDownloadURL;
    private String mFinalFileName;
    private long mPartStart;
    private long mPartEnd;
    private long mPartFinished;
    private long mPartFinishedBefore;
    private int mPartStatus;
    // 是否支持分片下载
    private boolean canDownloadByPart = false;

    public DownloadPartInfo() {
    }

    public void setCanDownloadByPart(boolean canDownloadByPart) {
        this.canDownloadByPart = canDownloadByPart;
    }

    public boolean canDownloadByPart() {
        return canDownloadByPart;
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
        return mDownloadID + "-" + mPartID;
    }

    public int getPartStatus() {
        return mPartStatus;
    }

    public void setPartStatus(int mPartStatus) {
        ZLog.d(DownloadItem.TAG, mDownloadID + "-" + mPartID + " status change , before: " + this.mPartStatus + " after : "
                + mPartStatus);
//        if(this.mPartStatus != mPartStatus){
//            reportDownloadPartStatusChange(this, this.mPartStatus, mPartStatus);
//        }
        this.mPartStatus = mPartStatus;
    }

    public int getPartID() {
        return mPartID;
    }

    public void setPartID(int mPartID) {
        this.mPartID = mPartID;
    }

    public String getDownloadURL() {
        return mDownloadURL;
    }

    public void setDownloadURL(String mDownloadURL) {
        this.mDownloadURL = mDownloadURL;
    }

    public long getPartStart() {
        return mPartStart;
    }

    public void setPartStart(long mPartStart) {
        this.mPartStart = mPartStart;
    }

    public long getPartEnd() {
        return mPartEnd;
    }

    public void setPartEnd(long mPartEnd) {
        this.mPartEnd = mPartEnd;
    }

    public long getPartFinished() {
        return mPartFinished;
    }

    public void setPartFinished(long mPartFinished) {
        this.mPartFinished = mPartFinished;
    }

    @Override
    public String toString() {
        return "DownloadPartInfo{"
                + "mPartID=" + mPartID
                + ", mDownloadID=" + mDownloadID
                + ", mDownloadURL='" + mDownloadURL + '\''
                + ", mFinalFileName='" + mFinalFileName + '\''
                + ", mPartStart=" + mPartStart
                + ", mPartEnd=" + mPartEnd
                + ", mPartFinished=" + mPartFinished
                + ", mPartFinishedBefore=" + mPartFinishedBefore
                + ", mPartStatus=" + mPartStatus
                + '}';
    }
}
