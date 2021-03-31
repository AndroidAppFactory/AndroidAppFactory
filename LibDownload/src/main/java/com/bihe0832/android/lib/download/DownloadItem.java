package com.bihe0832.android.lib.download;


import android.text.TextUtils;
import android.util.Log;
import com.bihe0832.android.lib.utils.ConvertUtils;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import kotlin.jvm.Synchronized;


/**
 * 下载信息结构体
 *
 * 除 downloadURL ，其余都非必填，默认不支持分片下载，每次下载都会强制重新下载
 *
 * @author hardyshi code@bihe0832.com Created on 2020/6/3.
 */
public class DownloadItem implements Serializable {

    public static final String TAG = "Download";

    // 下载URL，必填
    private String downloadURL = "";
    // 文件MD5，非必填
    private String fileMD5 = "";
    // 如果本地有同名文件是否重新下载，非必填
    private boolean forceDownloadNew = false;
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
    private DownloadListener mDownloadListener = null;
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
    // 实时下载速度，不填
    private long lastSpeed = 0;
    //开始下载的时间，不填
    private long startTime = 0;
    //最后暂停时间，不填
    private long pauseTime = 0;

    public boolean canDownloadByPart() {
        return canDownloadByPart;
    }

    public void setCanDownloadByPart(boolean canDownloadByPart) {
        this.canDownloadByPart = canDownloadByPart;
    }

    public void setNotificationVisibility(boolean visibility){
        notificationVisibility = visibility;
    }

    public boolean notificationVisibility(){
        return notificationVisibility;
    }
    public DownloadListener getDownloadListener() {
        return mDownloadListener;
    }

    public void setDownloadListener(DownloadListener listener) {
        mDownloadListener = listener;
    }

    public static long getDownloadIDByURL(String url) {
        return ConvertUtils.getUnsignedInt(url.hashCode());
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
        return getDownloadIDByURL(downloadURL);
    }

    // 下载进度
    public String getProcessDesc() {
        NumberFormat mProgressPercentFormat = NumberFormat.getPercentInstance();
        double percent = 0;
        if (fileLength > 0) {
            percent = ((double) finishedLength) / fileLength;
        }
        if (percent < 0.01) {
            percent = 0.01;
        }
        return mProgressPercentFormat.format(percent);
    }

    public String getDownloadDesc() {
        return downloadDesc;
    }

    public void setDownloadDesc(String desc) {
        downloadDesc = desc;
    }

    //下载进度
    public float getProcess() {
        double percent = 0;
        if (fileLength > 0) {
            percent = ((double) finishedLength) / fileLength;
        }
        if (percent < 0.01) {
            percent = 0.01;
        }
        BigDecimal bd = new BigDecimal(percent);
        double d1 = bd.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
        return (float) d1;
    }

    public boolean isDownloadWhenUseMobile() {
        return downloadWhenUseMobile;
    }

    public void setDownloadWhenUseMobile(boolean downloadWhenUseMobile) {
        this.downloadWhenUseMobile = downloadWhenUseMobile;
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

    public void setFileMD5(String fileMD5) {
        this.fileMD5 = fileMD5;
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
        if(forceDownloadNew){
            return true;
        }else {
            if(canDownloadByPart()){
                return false;
            }else {
                return true;
            }
        }
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
        if (lastSpeed > 0) {
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
