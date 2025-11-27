package com.bihe0832.android.lib.download.wrapper;

import android.content.Context;

import com.bihe0832.android.lib.download.DownloadClientConfig;
import com.bihe0832.android.lib.download.DownloadItem;
import com.bihe0832.android.lib.download.DownloadListener;
import com.bihe0832.android.lib.download.DownloadPauseType;
import com.bihe0832.android.lib.download.core.DownloadTaskList;
import com.bihe0832.android.lib.download.range.DownloadRangeManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * AAF Range 下载管理工具类
 * <p>
 * 提供 HTTP Range 请求的下载功能，支持下载文件的指定部分。
 * Range 用法相对比较复杂，因此不建议自行封装 DownloadItem，直接使用本类提供的方法。
 * <p>
 * 核心功能：
 * - 支持 HTTP Range 请求（下载文件的指定范围）
 * - 支持断点续传（指定本地起始位置）
 * - 任务管理（暂停、恢复、删除）
 * - 批量任务管理
 * - 任务列表查询
 * <p>
 * Range 下载参数说明：
 * - start: 服务器端起始位置（Range 请求的 start）
 * - length: 需要下载的长度
 * - localStart: 本地文件写入的起始位置
 * <p>
 * 使用场景：
 * 1. 大文件分片下载（多线程下载不同片段）
 * 2. 视频分段加载（边播边下）
 * 3. 断点续传（从上次中断位置继续）
 * 4. P2P 下载（下载文件的不同部分）
 * <p>
 * 注意事项：
 * 1. 服务器必须支持 Range 请求
 * 2. start + length 不能超过文件总大小
 * 3. localStart 通常等于 start，特殊场景可以不同
 * 4. 建议配合 MD5 校验确保分片完整性
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/9/23.
 * Description: Range 下载管理工具类，支持文件分片下载和断点续传
 */
public class DownloadRangeUtils {

    /**
     * 初始化
     *
     * @param context        Application Context
     * @param maxDownloadNum 同时容许下载的最大数量，如果主要用于大文件下载：建议3个，最大不建议超过5个
     * @param isDebug        是否开启调试模式
     */
    public static void init(Context context, int maxDownloadNum, DownloadClientConfig downloadConfig, Boolean isDebug) {
        DownloadRangeManager.INSTANCE.init(context, maxDownloadNum, downloadConfig, isDebug);
    }

    public static void init(Context context, boolean isDebug) {
        DownloadRangeManager.INSTANCE.init(context, isDebug);
    }

    public static void onDestroy() {
        DownloadRangeManager.INSTANCE.onDestroy();
    }

    public static void startDownload(Context context, String url, String filePath, long start, long length,
                                     DownloadListener listener) {
        startDownload(context, url, filePath, start, length, start, "", listener);
    }

    public static void startDownload(Context context, String url, String filePath, long start, long length,
                                     long localStart, String md5, DownloadListener listener) {
        DownloadItem info = new DownloadItem();
        info.setDownloadURL(url);
        info.setDownloadListener(listener);
        info.setFilePath(filePath);
        info.setContentMD5(md5);
        info.setNeedRecord(false);
        startDownload(context, info, start, length, localStart);
    }

    public static void startDownload(Context context, @NotNull DownloadItem info, long start, long length,
                                     long localStart) {
        startDownload(context, info, start, length, localStart, info.shouldForceReDownload());
    }

    /**
     * 添加一个下载任务
     *
     * @param info 添加任务的信息，除 downloadURL ，其余都非必填，下载本地仅支持传入文件夹，不支持传入下载文件路径，如果是要下载到指定文件，请参考 DownloadTools 二次分封装
     */
    public static void startDownload(Context context, @NotNull DownloadItem info, long start, long length,
                                     long localStart, boolean forceDownload) {
        if (!DownloadRangeManager.INSTANCE.hasInit()) {
            DownloadRangeManager.INSTANCE.init(context);
        }
        if (forceDownload && info.getDownloadPriority() < DownloadItem.FORCE_DOWNLOAD_PRIORITY) {
            info.setDownloadPriority(DownloadItem.FORCE_DOWNLOAD_PRIORITY);
        }
        info.setShouldForceReDownload(true);
        DownloadRangeManager.INSTANCE.addTask(info, start, length, localStart);
    }

    /**
     * 通过下载地址获取任务信息
     *
     * @param downloadURL 下载地址
     */
    public static DownloadItem getTaskByDownloadURL(@NotNull final String downloadURL, long start, long length,
                                                    long localStart) {
        return DownloadTaskList.INSTANCE.getTaskByDownloadID(
                getDownloadIDByURL(downloadURL, start, length, localStart));
    }

    public static long getDownloadIDByURL(String url, long start, long length, long localStart) {
        return DownloadItem.getDownloadIDByURL(url,
                DownloadItem.getDownloadActionKey(DownloadItem.TYPE_RANGE, start, length, localStart));
    }

    /**
     * 暂停一个下载任务
     *
     * @param downloadID 添加任务的信息
     */
    public static void pauseDownload(@NotNull final long downloadID, boolean canAutoResume) {
        if (canAutoResume) {
            DownloadRangeManager.INSTANCE.pauseTask(downloadID, DownloadPauseType.PAUSED_BY_ALL, false);
        } else {
            DownloadRangeManager.INSTANCE.pauseTask(downloadID, DownloadPauseType.PAUSED_BY_USER, false);
        }
    }

    /**
     * 恢复一个下载任务
     *
     * @param downloadID    恢复任务的信息
     * @param pauseOnMobile 4G是否暂停下载
     */
    public static void resumeDownload(long downloadID, boolean pauseOnMobile) {
        DownloadRangeManager.INSTANCE.resumeTask(downloadID, null, true, pauseOnMobile);
    }

    /**
     * 删除一个下载任务
     *
     * @param downloadID 删除任务的信息
     * @param deleteFile 删除任务时是否删除相关文件
     */
    public static void deleteTask(long downloadID, boolean deleteFile) {
        DownloadRangeManager.INSTANCE.deleteTask(downloadID, true, deleteFile);
    }

    /**
     * 暂停所有下载任务
     */
    public static void pauseAll(boolean pauseMaxPriorityDownload, boolean canAutoResume) {
        if (canAutoResume) {
            DownloadRangeManager.INSTANCE.pauseAllTask(DownloadPauseType.PAUSED_BY_ALL, pauseMaxPriorityDownload);
        } else {
            DownloadRangeManager.INSTANCE.pauseAllTask(DownloadPauseType.PAUSED_BY_USER, pauseMaxPriorityDownload);
        }
    }

    /**
     * 暂停所有下载中的任务
     */
    public static void pauseDownloading(boolean pauseMaxPriorityDownload, boolean canAutoResume) {
        if (canAutoResume) {
            DownloadRangeManager.INSTANCE.pauseDownloadingTask(DownloadPauseType.PAUSED_BY_ALL,
                    pauseMaxPriorityDownload);
        } else {
            DownloadRangeManager.INSTANCE.pauseDownloadingTask(DownloadPauseType.PAUSED_BY_USER,
                    pauseMaxPriorityDownload);
        }
    }

    /**
     * 暂停所有在等待的下载任务
     */
    public static void pauseWaiting(boolean pauseMaxPriorityDownload, boolean canAutoResume) {
        if (canAutoResume) {
            DownloadRangeManager.INSTANCE.pauseWaitingTask(DownloadPauseType.PAUSED_BY_ALL, pauseMaxPriorityDownload);
        } else {
            DownloadRangeManager.INSTANCE.pauseWaitingTask(DownloadPauseType.PAUSED_BY_USER, pauseMaxPriorityDownload);
        }
    }

    /**
     * 恢复所有下载任务
     *
     * @param pauseOnMobile 4G是否暂停下载
     */

    public static void resumeAll(boolean pauseOnMobile) {
        DownloadRangeManager.INSTANCE.resumeAllTask(pauseOnMobile);
    }

    /**
     * 恢复所有下载失败的任务
     *
     * @param pauseOnMobile 4G是否暂停下载
     */
    public static void resumeFailed(boolean pauseOnMobile) {
        DownloadRangeManager.INSTANCE.resumeFailedTask(pauseOnMobile);
    }

    /**
     * 恢复所有被暂停的下载任务
     *
     * @param pauseOnMobile 4G是否暂停下载
     */
    public static void resumePause(boolean pauseOnMobile) {
        DownloadRangeManager.INSTANCE.resumePauseTask(pauseOnMobile);
    }

    /**
     * 获取任务列表
     *
     * @return 所有下载任务列表
     */
    @NotNull
    public static List<DownloadItem> getAll() {
        return DownloadRangeManager.INSTANCE.getAllTask();
    }

    /**
     * 获取已经下载完成的任务
     *
     * @return
     */
    @NotNull
    public static List<DownloadItem> getFinished() {
        return DownloadRangeManager.INSTANCE.getFinishedTask();
    }

    /**
     * 获取正在下载的任务
     *
     * @return
     */
    @NotNull
    public static List<DownloadItem> getDownloading() {
        return DownloadRangeManager.INSTANCE.getDownloadingTask();
    }

    /**
     * 获取正在等待的下载任务
     *
     * @return
     */
    @NotNull
    public static List<DownloadItem> getWaiting() {
        return DownloadRangeManager.INSTANCE.getWaitingTask();
    }
}