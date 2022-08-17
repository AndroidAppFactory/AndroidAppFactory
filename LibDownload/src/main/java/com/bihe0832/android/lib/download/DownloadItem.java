package com.bihe0832.android.lib.download;


import android.text.TextUtils;
import android.util.Log;

import com.bihe0832.android.lib.download.wrapper.DownloadUtils;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.bihe0832.android.lib.utils.MathUtils;

import java.io.Serializable;

import kotlin.jvm.Synchronized;


/**
 * 下载信息结构体
 * <p>
 * 除 downloadURL ，其余都非必填，默认不支持分片下载，每次下载都会强制重新下载
 *
 * @author zixie code@bihe0832.com Created on 2020/6/3.
 */
public class DownloadItem implements Serializable {

    public static final String TAG = "Download";

    // 下载URL，必填
    private String downloadURL = "";
    // 文件MD5，非必填
    private String fileMD5 = "";
    // 文件SHA256，非必填
    private String fileSHA256 = "";
    // 如果本地有同名文件是否重新下载，非必填
    private boolean forceDownloadNew = false;
    // 如果本地有同名文件但是下载完判断MD5不一致，是否自动删除，非必填
    private boolean forceDeleteBad = true;
    //下载结束是否自动拉起安装，非必填
    private boolean autoInstall = false;
    //下载过程是否展示到通知栏，非必填
    private boolean notificationVisibility = false;
    // 当前的下载状态，实时同步，不填
    private int status = DownloadStatus.STATUS_DOWNLOAD_PAUSED;
    // 4G下是否下载，非必填
    private boolean downloadWhenUseMobile = false;
    // 任务添加后是否自动下载，非必填
    private boolean downloadWhenAdd = true;
    // 是否支持分片下载
    private boolean canDownloadByPart = false;
    //内部下载回调
    private transient DownloadListener mDownloadListener = null;
    // 下载信息描述，非必填
    private String downloadDesc = "";
    // 下载内容标题，非必填
    private String downloadTitle = "";
    // 下载时指定的本地目录，建议不填
    private String fileNameWithPath = "";
    // 下载结束以后，文件的实际地址，不填
    private String finalFilePath = "";
    // 扩展信息，会一路透传到该下载相关的所有事件，包括安装
    private String actionKey = "";
    // 扩展信息，会一路透传到该下载相关的所有事件，包括安装
    private String extraInfo = "";
    // 下载应用的包名
    private String packageName = "";
    // 当前下载应用的version
    private long versionCode = 0;
    // 下载显示的Icon，非必填
    private String downloadIcon = "";
    // 下载时，临时的中间文件，不填
    private String tempFilePath = "";
    // 累积已经下载完的文件长度，不填
    private long finishedLength = 0;
    // 之前已经下载完的文件长度，不填
    private long finishedLengthBefore = 0;
    // 文件总长度，不填
    private long fileLength = 0;
    // 最终实际下载的URL，不填
    private String realURL = "";
    // 实时下载速度，不填
    private transient long lastSpeed = 0;
    //开始下载的时间，不填
    private transient long startTime = 0;
    //最后暂停时间，不填
    private transient long pauseTime = 0;


    public boolean canDownloadByPart() {
        return canDownloadByPart;
    }

    public void setCanDownloadByPart(boolean canDownloadByPart) {
        this.canDownloadByPart = canDownloadByPart;
    }

    public void setNotificationVisibility(boolean visibility) {
        notificationVisibility = visibility;
    }

    public boolean notificationVisibility() {
        return notificationVisibility;
    }

    public DownloadListener getDownloadListener() {
        return mDownloadListener;
    }

    public void setDownloadListener(DownloadListener listener) {
        mDownloadListener = listener;
    }

    // 平均下载速度
    public int getAverageSpeed() {
        long finished = finishedLength - finishedLengthBefore;
        if (finished < 0) {
            return 0;
        } else {
            return (int) (finished * 1.0f * 1000 / (System.currentTimeMillis() - startTime));
        }
    }

    //下载ID,一个任务的唯一标示
    public long getDownloadID() {
        return DownloadUtils.getDownloadIDByURL(downloadURL);
    }

    public boolean isForceDeleteBad() {
        return forceDeleteBad;
    }

    public void setForceDeleteBad(boolean forceDeleteBad) {
        this.forceDeleteBad = forceDeleteBad;
    }

    public String getDownloadDesc() {
        return downloadDesc;
    }

    public void setDownloadDesc(String desc) {
        downloadDesc = desc;
    }

    // 下载进度
    public String getProcessDesc() {
        return MathUtils.getFormatPercentDesc(getProcess());
    }

    //下载进度
    public float getProcess() {
        return MathUtils.getFormatPercent(finishedLength, fileLength, 4);
    }

    public boolean isDownloadWhenUseMobile() {
        return downloadWhenUseMobile;
    }

    public void setDownloadWhenUseMobile(boolean downloadWhenUseMobile) {
        this.downloadWhenUseMobile = downloadWhenUseMobile;
    }

    public String getRealURL() {
        return realURL;
    }

    public void setRealURL(String realURL) {
        this.realURL = realURL;
    }

    public boolean isDownloadWhenAdd() {
        return downloadWhenAdd;
    }

    public void setDownloadWhenAdd(boolean downloadWhenAdd) {
        this.downloadWhenAdd = downloadWhenAdd;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getDownloadIcon() {
        return downloadIcon;
    }

    public void setDownloadIcon(String downloadIcon) {
        this.downloadIcon = downloadIcon;
    }

    public String getFileMD5() {
        return fileMD5;
    }

    public String getFileSHA256() {
        return fileSHA256;
    }

    public void setFileMD5(String fileMD5) {
        this.fileMD5 = fileMD5;
    }

    public void setFileSHA256(String fileSHA256) {
        this.fileSHA256 = fileSHA256;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTempFilePath() {
        return tempFilePath;
    }

    public void setTempFilePath(String tempFilePath) {
        this.tempFilePath = tempFilePath;
    }

    public String getFileNameWithPath() {
        return fileNameWithPath;
    }

    public void setFileNameWithPath(String fileNameWithPath) {
        this.fileNameWithPath = fileNameWithPath;
    }

    public boolean isForceDownloadNew() {
        //强制重新下载，或者不支持分片，就算是不强制重新下载也要强制重新下载
        return forceDownloadNew;
    }

    public static long getDownloadIDByURL(String url) {
        return ConvertUtils.getUnsignedInt(url.hashCode());
    }

    public void setForceDownloadNew(boolean forceDownloadNew) {
        this.forceDownloadNew = forceDownloadNew;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.status = downloadStatus;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public String getFinalFilePath() {
        return finalFilePath;
    }

    public void setFinalFilePath(String finalFilePath) {
        this.finalFilePath = finalFilePath;
    }

    public long getFinishedLengthBefore() {
        return finishedLengthBefore;
    }

    public void setFinishedLengthBefore(long finishedLengthBefore) {
        this.finishedLengthBefore = finishedLengthBefore;
    }

    public boolean isAutoInstall() {
        return autoInstall;
    }

    public void setAutoInstall(boolean autoInstall) {
        this.autoInstall = autoInstall;
    }

    public long getFinished() {
        return finishedLength;
    }

    @Synchronized
    public void setFinished(long finished) {
        this.finishedLength = finished;
    }

    public long addFinished(long data) {
        return finishedLength = finishedLength + data;
    }

    public long getLastSpeed() {
        return lastSpeed;
    }

    public void setLastSpeed(long lastSpeed) {
        if (lastSpeed >= 0) {
            this.lastSpeed = lastSpeed;
        }
    }

    public long getPauseTime() {
        return pauseTime;
    }

    public void setPause() {
        this.status = DownloadStatus.STATUS_DOWNLOAD_PAUSED;
        this.pauseTime = System.currentTimeMillis();
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public @DownloadStatus
    int getStatus() {
        return status;
    }

    public void setStatus(@DownloadStatus int newStatus) {
        Log.d(TAG, "status change , before: " + this.status + " after : " + newStatus);
        this.status = newStatus;
    }

    public String getDownloadTitle() {
        return downloadTitle;
    }

    public void setDownloadTitle(String downloadTitle) {
        this.downloadTitle = downloadTitle;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(long versionCode) {
        this.versionCode = versionCode;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        if (!TextUtils.isEmpty(extraInfo)) {
            this.extraInfo = extraInfo;
        }
    }

    public String getActionKey() {
        return actionKey;
    }

    public void setActionKey(String actionKey) {
        if (!TextUtils.isEmpty(actionKey)) {
            this.actionKey = actionKey;
        }
    }

    public void update(DownloadItem item) {
        if (item.getDownloadID() == getDownloadID()) {
            this.downloadURL = item.downloadURL;
            this.fileMD5 = item.fileMD5;
            this.fileSHA256 = item.fileSHA256;
            this.forceDownloadNew = item.forceDownloadNew;
            this.autoInstall = item.autoInstall;
            this.notificationVisibility = item.notificationVisibility;
            this.downloadWhenUseMobile = item.downloadWhenUseMobile;
            this.downloadWhenAdd = item.downloadWhenAdd;
            this.canDownloadByPart = item.canDownloadByPart;
            this.mDownloadListener = item.mDownloadListener;
            this.downloadDesc = item.downloadDesc;
            this.downloadTitle = item.downloadTitle;
            this.fileNameWithPath = item.fileNameWithPath;
            this.finalFilePath = item.finalFilePath;
            this.actionKey = item.actionKey;
            this.extraInfo = item.extraInfo;
            this.packageName = item.packageName;
            this.versionCode = item.versionCode;
            this.downloadIcon = item.downloadIcon;
            this.tempFilePath = item.tempFilePath;
        } else {
            Log.e(TAG, "update error , download id is bad ");
        }
    }

    @Override
    public String toString() {
        return "下载资源：{"
                + "downloadDesc='" + downloadDesc + '\''
                + ", downloadTitle='" + downloadTitle + '\''
                + ", actionKey='" + actionKey + '\''
                + ", extraInfo='" + extraInfo + '\''
                + ", packageName='" + packageName + '\''
                + ", versionCode=" + versionCode
                + ", downloadIcon='" + downloadIcon + '\''
                + ", downloadURL='" + downloadURL + '\''
                + ", fileNameWithPath='" + fileNameWithPath + '\''
                + ", finalFilePath='" + finalFilePath + '\''
                + ", tempFilePath='" + tempFilePath + '\''
                + ", fileMD5='" + fileMD5 + '\''
                + ", fileSHA256='" + fileSHA256 + '\''
                + ", forceDownloadNew=" + forceDownloadNew
                + ", finishedLength=" + finishedLength
                + ", finishedLengthBefore=" + finishedLengthBefore
                + ", fileLength=" + fileLength
                + ", lastSpeed=" + lastSpeed
                + ", startTime=" + startTime
                + ", pauseTime=" + pauseTime
                + ", autoInstall=" + autoInstall
                + ", status=" + status
                + ", downloadWhenUseMobile=" + downloadWhenUseMobile
                + ", downloadWhenAdd=" + downloadWhenAdd
                + '}';
    }
}
