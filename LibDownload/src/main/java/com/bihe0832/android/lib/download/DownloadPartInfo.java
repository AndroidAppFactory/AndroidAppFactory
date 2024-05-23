package com.bihe0832.android.lib.download;

import com.bihe0832.android.lib.log.ZLog;

public class DownloadPartInfo {

    private int mDownloadType = DownloadItem.TYPE_FILE;

    private long mDownloadID;

    private int mPartNo;

    private String mRealDownloadURL;
    private String mFinalFileName;
    private long mPartRangeStart;
    private long mPartLocalStart;
    private long mPartLength;
    private long mPartFinished;
    private long mPartFinishedBefore;
    private int mPartStatus;

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

    @Override
    public String toString() {
        return "DownloadPartInfo{" + "mPartID=" + mPartNo
                + ", mDownloadID=" + mDownloadID
                + ", mDownloadType=" + mDownloadType
                + ", mDownloadURL='" + mRealDownloadURL + '\''
                + ", mFinalFileName='" + mFinalFileName + '\'' + ", mPartRangeStart=" + mPartRangeStart
                + ", mPartLocalStart=" + mPartLocalStart + ", mPartLength=" + mPartLength
                + ", mPartFinished=" + mPartFinished
                + ", mPartFinishedBefore=" + mPartFinishedBefore
                + ", mPartStatus=" + mPartStatus
                + '}';
    }
}
