package com.bihe0832.android.lib.download;

import com.bihe0832.android.lib.log.ZLog;

public class DownloadPartInfo {

    private int mPartID;
    private long mDownloadID;
    private String mRealDownloadURL;
    private String mFinalFileName;
    private long mPartStart;
    private long mPartEnd;
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

    public String getRealDownloadURL() {
        return mRealDownloadURL;
    }

    public void setRealDownloadURL(String mDownloadURL) {
        this.mRealDownloadURL = mDownloadURL;
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
                + ", mDownloadURL='" + mRealDownloadURL + '\''
                + ", mFinalFileName='" + mFinalFileName + '\''
                + ", mPartStart=" + mPartStart
                + ", mPartEnd=" + mPartEnd
                + ", mPartFinished=" + mPartFinished
                + ", mPartFinishedBefore=" + mPartFinishedBefore
                + ", mPartStatus=" + mPartStatus
                + '}';
    }
}
