package com.bihe0832.android.lib.download.wrapper;

import android.content.Context;
import android.text.TextUtils;
import com.bihe0832.android.lib.download.DownloadItem;
import com.bihe0832.android.lib.download.DownloadPauseType;
import com.bihe0832.android.lib.download.core.DownloadTaskList;
import com.bihe0832.android.lib.download.file.DownloadFileManager;
import java.util.List;
import org.jetbrains.annotations.NotNull;


/**
 * AAF 文件下载
 *
 * @author zixie code@bihe0832.com Created on 2020/9/23.
 */

public class DownloadFileUtils {

    public static final String DOWNLOAD_ACTION_KEY_APK = "DownloadAPK";
    public static final String DOWNLOAD_ACTION_KEY_CONFIG = "DownloadConfig";
    public static final String DOWNLOAD_ACTION_KEY_FILE = "DownloadFile";

    /**
     * 初始化
     *
     * @param context Application Context
     * @param maxDownloadNum 同时容许下载的最大数量，如果主要用于大文件下载：建议3个，最大不建议超过5个
     * @param isDebug 是否开启调试模式
     */
    public static void init(Context context, int maxDownloadNum, Boolean isDebug) {
        DownloadFileManager.INSTANCE.init(context, maxDownloadNum, isDebug);
    }

    public static void init(Context context, boolean isDebug) {
        DownloadFileManager.INSTANCE.init(context, isDebug);
    }

    public static void onDestroy() {
        DownloadFileManager.INSTANCE.onDestroy();
    }

    /**
     * 添加一个下载任务
     *
     * @param info 添加任务的信息，除 downloadURL ，其余都非必填，下载本地仅支持传入文件夹，不支持传入下载文件路径，如果是要下载到指定文件，请参考 DownloadTools 二次分封装
     */
    public static void startDownload(Context context, @NotNull DownloadItem info, boolean shouldForceReDownload) {
        if (!DownloadFileManager.INSTANCE.hasInit()) {
            DownloadFileManager.INSTANCE.init(context);
        }
        if (shouldForceReDownload && info.getDownloadPriority() < DownloadItem.FORCE_DOWNLOAD_PRIORITY) {
            info.setDownloadPriority(DownloadItem.FORCE_DOWNLOAD_PRIORITY);
        }
        DownloadFileManager.INSTANCE.addTask(info);
    }

    public static void startDownload(Context context, @NotNull DownloadItem info) {
        if (!info.shouldForceReDownload()) {
            info.setShouldForceReDownload(
                    TextUtils.isEmpty(info.getContentMD5()) && TextUtils.isEmpty(info.getContentSHA256()));
        }
        // 默认所有需要强制重新下载的，都提高下载优先级，else 优先级不变
        startDownload(context, info, info.shouldForceReDownload());
    }

    /**
     * 通过下载地址获取任务信息
     *
     * @param downloadURL 下载地址
     */
    public static DownloadItem getTaskByDownloadURL(@NotNull final String downloadURL) {
        return DownloadTaskList.INSTANCE.getTaskByDownloadURL(downloadURL, "");
    }

    public static long getDownloadIDByURL(String url) {
        return DownloadItem.getDownloadIDByURL(url, "");
    }

    /**
     * 暂停一个下载任务
     *
     * @param downloadID 添加任务的信息
     */
    public static void pauseDownload(@NotNull final long downloadID, boolean canAutoResume) {
        if (canAutoResume) {
            DownloadFileManager.INSTANCE.pauseTask(downloadID, DownloadPauseType.PAUSED_BY_ALL, false);
        } else {
            DownloadFileManager.INSTANCE.pauseTask(downloadID, DownloadPauseType.PAUSED_BY_USER, false);
        }
    }

    /**
     * 恢复一个下载任务
     *
     * @param downloadID 恢复任务的信息
     * @param pauseOnMobile 4G是否暂停下载
     */
    public static void resumeDownload(long downloadID, boolean pauseOnMobile) {
        DownloadFileManager.INSTANCE.resumeTask(downloadID, null, true, pauseOnMobile);
    }

    /**
     * 删除一个下载任务
     *
     * @param downloadID 删除任务的信息
     * @param deleteFile 删除任务时是否删除相关文件
     */
    public static void deleteTask(long downloadID, boolean deleteFile) {
        DownloadFileManager.INSTANCE.deleteTask(downloadID, true, deleteFile);
    }

    /**
     * 暂停所有下载任务
     */
    public static void pauseAll(boolean pauseMaxPriorityDownload, boolean canAutoResume) {
        if (canAutoResume) {
            DownloadFileManager.INSTANCE.pauseAllTask(DownloadPauseType.PAUSED_BY_ALL, pauseMaxPriorityDownload);
        } else {
            DownloadFileManager.INSTANCE.pauseAllTask(DownloadPauseType.PAUSED_BY_USER, pauseMaxPriorityDownload);
        }
    }

    public static boolean hasPauseAll() {
        return DownloadFileManager.INSTANCE.hasPauseAll();
    }

    /**
     * 暂停所有下载中的任务
     */
    public static void pauseDownloading(boolean pauseMaxPriorityDownload, boolean canAutoResume) {
        if (canAutoResume) {
            DownloadFileManager.INSTANCE.pauseDownloadingTask(DownloadPauseType.PAUSED_BY_ALL,
                    pauseMaxPriorityDownload);
        } else {
            DownloadFileManager.INSTANCE.pauseDownloadingTask(DownloadPauseType.PAUSED_BY_USER,
                    pauseMaxPriorityDownload);
        }
    }

    /**
     * 暂停所有在等待的下载任务
     */
    public static void pauseWaiting(boolean pauseMaxPriorityDownload, boolean canAutoResume) {
        if (canAutoResume) {
            DownloadFileManager.INSTANCE.pauseWaitingTask(DownloadPauseType.PAUSED_BY_ALL, pauseMaxPriorityDownload);
        } else {
            DownloadFileManager.INSTANCE.pauseWaitingTask(DownloadPauseType.PAUSED_BY_USER, pauseMaxPriorityDownload);
        }
    }

    /**
     * 恢复所有下载任务
     *
     * @param pauseOnMobile 4G是否暂停下载
     */

    public static void resumeAll(boolean pauseOnMobile) {
        DownloadFileManager.INSTANCE.resumeAllTask(pauseOnMobile);
    }

    /**
     * 恢复所有下载失败的任务
     *
     * @param pauseOnMobile 4G是否暂停下载
     */
    public static void resumeFailed(boolean pauseOnMobile) {
        DownloadFileManager.INSTANCE.resumeFailedTask(pauseOnMobile);
    }

    /**
     * 恢复所有被暂停的下载任务
     *
     * @param pauseOnMobile 4G是否暂停下载
     */
    public static void resumePause(boolean pauseOnMobile) {
        DownloadFileManager.INSTANCE.resumePauseTask(pauseOnMobile);
    }

    /**
     * 获取任务列表
     *
     * @return 所有下载任务列表
     */
    @NotNull
    public static List<DownloadItem> getAll() {
        return DownloadFileManager.INSTANCE.getAllTask();
    }

    /**
     * 获取已经下载完成的任务
     *
     * @return
     */
    @NotNull
    public static List<DownloadItem> getFinished() {
        return DownloadFileManager.INSTANCE.getFinishedTask();
    }

    /**
     * 获取正在下载的任务
     *
     * @return
     */
    @NotNull
    public static List<DownloadItem> getDownloading() {
        return DownloadFileManager.INSTANCE.getDownloadingTask();
    }

    /**
     * 获取正在等待的下载任务
     *
     * @return
     */
    @NotNull
    public static List<DownloadItem> getWaiting() {
        return DownloadFileManager.INSTANCE.getWaitingTask();
    }
}