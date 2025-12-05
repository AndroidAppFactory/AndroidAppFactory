package com.bihe0832.android.lib.install.apk;

import static com.bihe0832.android.lib.install.InstallErrorCode.FILE_NOT_FOUND;
import static com.bihe0832.android.lib.install.InstallErrorCode.START_SYSTEM_INSTALL_EXCEPTION;
import static com.bihe0832.android.lib.install.InstallUtils.TAG;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.file.provider.ZixieFileProvider;
import com.bihe0832.android.lib.install.InstallListener;
import com.bihe0832.android.lib.install.splitapk.SplitApksInstallHelper;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.timer.BaseTask;
import com.bihe0832.android.lib.timer.TaskManager;
import com.bihe0832.android.lib.utils.MathUtils;
import com.bihe0832.android.lib.utils.apk.APKUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author zixie code@bihe0832.com Created on 2020/9/25.
 * <p>
 * APK 安装工具类，支持两种安装方式：
 * 1. Session API（默认）：使用 PackageInstaller Session，回调可靠
 * 2. FileProvider（降级/强制）：使用 Intent 调用系统安装器，通过轮询检测安装结果
 */
public class APKInstall {

    private static final String INSTALL_TYPE = "application/vnd.android.package-archive";

    // 安装监听任务名前缀
    private static final String TASK_NAME_PREFIX = "APKInstall_";

    // 轮询间隔（TaskManager 单位是 500ms，2 表示 1 秒）
    private static final int POLL_INTERVAL = 2;

    // 默认安装超时时间（秒）
    public static final int DEFAULT_INSTALL_TIMEOUT = 120;

    // 应用上下文
    private static Context appContext = null;

    // ==================== 工具方法 ====================

    /**
     * 根据文件大小计算安装超时时间
     * <p>
     * 计算规则：每 5MB 需要 1 秒，最小为 DEFAULT_INSTALL_TIMEOUT
     *
     * @param file APK 文件
     * @return 超时时间（秒）
     */
    public static int calculateInstallTimeout(@Nullable File file) {
        if (file == null || !file.exists()) {
            return DEFAULT_INSTALL_TIMEOUT;
        }
        int compareTime = (int) (file.length() / (FileUtils.SPACE_MB * 20));
        return MathUtils.getMax(DEFAULT_INSTALL_TIMEOUT, compareTime);
    }

    // ==================== 安装方法 ====================

    /**
     * 安装 APK（不带超时检测）
     */
    public static void installAPK(Context context, String filePath, InstallListener listener) {
        installAPK(context, filePath, "", listener);
    }

    /**
     * 安装 APK（带超时检测，指定包名）
     *
     * @param context     上下文
     * @param filePath    APK 文件路径
     * @param packageName 包名（为空则自动解析）
     * @param listener    安装监听器
     */
    public static void installAPK(Context context, String filePath, String packageName, InstallListener listener) {
        File file = new File(filePath);
        Uri fileProvider = ZixieFileProvider.getZixieFileProvider(context, file);
        int timeout = listener != null ? calculateInstallTimeout(file) : 0;
        installAPK(context, fileProvider, file, packageName, false, timeout, listener);
    }

    public static void installAPK(Context context, String filePath, String packageName, boolean forceFileProvider, int timeoutSeconds, InstallListener listener) {
        File file = new File(filePath);
        Uri fileProvider = ZixieFileProvider.getZixieFileProvider(context, new File(filePath));
        installAPK(context, fileProvider, file, packageName, forceFileProvider, timeoutSeconds, listener);
    }

    /**
     * 安装 APK（完整参数版本）
     *
     * @param context           上下文
     * @param file              APK 文件
     * @param fileProvider      FileProvider URI（为空则自动生成）
     * @param packageName       包名（为空则自动解析）
     * @param forceFileProvider 是否强制使用 FileProvider 方式（跳过 Session API）
     * @param timeoutSeconds    超时时间（秒），0 表示不检测超时
     * @param listener          安装监听器
     */
    public static void installAPK(Context context, Uri fileProvider, File file, String packageName,
                                  boolean forceFileProvider, int timeoutSeconds, InstallListener listener) {
        if (!file.exists()) {
            if (listener != null) {
                listener.onInstallFailed(FILE_NOT_FOUND);
            }
            return;
        }

        // 决定使用哪种安装方式
        // 1. 强制 FileProvider
        // 2. 无回调需求（listener == null 或 timeoutSeconds <= 0）
        // 3. API < 21（Session API 不支持）
        boolean useFileProvider = forceFileProvider
                || listener == null
                || timeoutSeconds <= 0
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;

        if (useFileProvider) {
            ZLog.d(TAG, "installAPK: using FileProvider method, forceFileProvider=" + forceFileProvider
                    + ", hasListener=" + (listener != null) + ", timeout=" + timeoutSeconds);
            try {
                installAPKByProvider(context, fileProvider, file, packageName, timeoutSeconds, listener);
            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onInstallFailed(START_SYSTEM_INSTALL_EXCEPTION);
                }
            }
        } else {
            ZLog.d(TAG, "installAPK: using Session API method");
            installAPKByPackageInstaller(context, fileProvider, file.getAbsolutePath(), packageName, timeoutSeconds, listener);
        }
    }

    // ==================== Session API 安装方式（默认） ====================

    /**
     * 通过 Session API 安装 APK（使用 SplitApksInstallHelper）
     * 回调可靠，不依赖包可见性权限
     *
     * @param context        上下文
     * @param filePath       APK 文件路径
     * @param fileProvider   FileProvider URI（用于降级时使用）
     * @param packageName    包名（用于降级时的轮询检测）
     * @param timeoutSeconds 超时时间
     * @param listener       安装监听器
     */
    public static void installAPKByPackageInstaller(Context context, Uri fileProvider, String filePath, String packageName,
                                                     int timeoutSeconds, InstallListener listener) {
        ArrayList<String> files = new ArrayList<>(Collections.singletonList(filePath));

        SplitApksInstallHelper.INSTANCE.installApkWithFallback(
                context,
                files,
                timeoutSeconds,
                listener,
                // 降级回调：Session 安装失败时使用 FileProvider 方式
                (errorCode) -> {
                    ZLog.w(TAG, "Session install failed with errorCode=" + errorCode + ", fallback to FileProvider");
                    try {
                        File file = new File(filePath);
                        installAPKByProvider(context, fileProvider, file, packageName, timeoutSeconds, listener);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (listener != null) {
                            listener.onInstallFailed(START_SYSTEM_INSTALL_EXCEPTION);
                        }
                    }
                }
        );
    }

    /**
     * 通过 FileProvider 安装 APK（带超时检测）
     *
     * @param context        上下文
     * @param fileProvider   FileProvider URI
     * @param file           APK 文件
     * @param packageName    包名（为空则自动解析）
     * @param timeoutSeconds 超时时间（秒），0 表示不检测超时
     * @param listener       安装监听器
     */
    public static void installAPKByProvider(Context context, Uri fileProvider, File file, String packageName, int timeoutSeconds, InstallListener listener) {
        if (file == null || !file.exists()) {
            if (listener != null) {
                listener.onInstallFailed(FILE_NOT_FOUND);
            }
            return;
        }

        String filePath = file.getAbsolutePath();
        // 包名为空时自动解析
        String finalPackageName = TextUtils.isEmpty(packageName) ? APKUtils.getApkPackageName(context, filePath) : packageName;
        ZLog.d(TAG, "installAPKByProvider: filePath=" + filePath + ", packageName=" + finalPackageName + ", timeoutSeconds=" + timeoutSeconds);

        // 初始化上下文
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }

        // 记录安装前的版本信息（用于检测更新安装）
        long installedVersionCode = APKUtils.getAppVersionCode(context, finalPackageName);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            if (BuildUtils.INSTANCE.getSDK_INT() < Build.VERSION_CODES.N) {
                intent.setDataAndType(Uri.fromFile(file), INSTALL_TYPE);
            } else {
                if (fileProvider == null) {
                    ZLog.e(TAG, "installAPK: fileProvider is null, cannot install");
                    if (listener != null) {
                        listener.onInstallFailed(FILE_NOT_FOUND);
                    }
                    return;
                }
                intent.setDataAndType(fileProvider, INSTALL_TYPE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            if (BuildUtils.INSTANCE.getSDK_INT() > Build.VERSION_CODES.O
                    && !context.getPackageManager().canRequestPackageInstalls()) {
                Log.w(TAG, "app doesn't have install permission, system will show permission dialog");
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            if (listener != null) {
                listener.onInstallStart();
            }

            // 启动安装监听
            if (timeoutSeconds > 0) {
                startInstallMonitor(finalPackageName, installedVersionCode, timeoutSeconds, listener);
            } else {
                // 不监听安装结果，直接回调超时
                ZLog.d(TAG, "installAPKByProvider: timeoutSeconds <= 0, skip install monitor");
                if (listener != null) {
                    listener.onInstallTimeOut();
                }
            }

        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onInstallFailed(START_SYSTEM_INSTALL_EXCEPTION);
            }
        }
    }

    // ==================== 安装监听（TaskManager 轮询） ====================

    /**
     * 启动安装监听（使用 TaskManager 轮询检测）
     *
     * @param packageName             包名，无效时只做超时检测
     * @param preInstalledVersionCode 安装前的版本号（-1 表示未安装）
     * @param timeoutSeconds          超时时间
     * @param listener                监听器
     */
    private static void startInstallMonitor(String packageName, long preInstalledVersionCode, int timeoutSeconds, InstallListener listener) {
        if (TextUtils.isEmpty(packageName)) {
            // 包名无效：无法检测安装结果，直接超时回调
            ZLog.d(TAG, "startInstallMonitor: invalid packageName, cannot monitor");
            ThreadManager.getInstance().runOnUIThread(() -> {
                if (listener != null) {
                    listener.onInstallTimeOut();
                }
            });
            return;
        }

        // 检查是否能查询目标包名（兼容 QUERY_ALL_PACKAGES 和 <queries> 两种方式）
        if (!APKUtils.canQueryPackage(appContext, packageName)) {
            ZLog.w(TAG, "startInstallMonitor: cannot query package " + packageName + ", need QUERY_ALL_PACKAGES permission or <queries> declaration on Android 11+");
            ThreadManager.getInstance().runOnUIThread(() -> {
                if (listener != null) {
                    listener.onInstallTimeOut();
                }
            });
            return;
        }

        ZLog.d(TAG, "startInstallMonitor: packageName=" + packageName + ", preVersion=" + preInstalledVersionCode + ", timeout=" + timeoutSeconds);

        String taskName = TASK_NAME_PREFIX + packageName;

        // 移除可能存在的旧任务
        TaskManager.getInstance().removeTask(taskName);

        // 添加轮询任务
        TaskManager.getInstance().addTask(new BaseTask() {
            private int elapsedSeconds = 0;

            @Override
            public int getMyInterval() {
                return POLL_INTERVAL;
            }

            @Override
            protected int getNextEarlyRunTime() {
                return 0;
            }

            @Override
            protected void doTask() {
                elapsedSeconds++;

                // 检查安装状态
                if (checkInstallSuccess(packageName, preInstalledVersionCode)) {
                    ZLog.d(TAG, "pollInstallStatus: install succeeded for " + packageName);
                    TaskManager.getInstance().removeTask(taskName);
                    ThreadManager.getInstance().runOnUIThread(() -> {
                        if (listener != null) {
                            listener.onInstallSuccess();
                        }
                    });
                    return;
                }

                // 检查超时
                if (elapsedSeconds >= timeoutSeconds) {
                    ZLog.d(TAG, "pollInstallStatus: timeout for " + packageName);
                    TaskManager.getInstance().removeTask(taskName);
                    ThreadManager.getInstance().runOnUIThread(() -> {
                        if (listener != null) {
                            listener.onInstallTimeOut();
                        }
                    });
                }
            }

            @Override
            public String getTaskName() {
                return taskName;
            }

            @Override
            protected boolean runAfterAdd() {
                return false;
            }
        });
    }

    /**
     * 检查应用是否安装成功
     *
     * @param packageName             包名
     * @param preInstalledVersionCode 安装前的版本号（0 表示未安装）
     * @return 是否安装成功
     */
    private static boolean checkInstallSuccess(String packageName, long preInstalledVersionCode) {
        if (appContext == null) return false;

        long currentVersionCode = APKUtils.getAppVersionCode(appContext, packageName);
        ZLog.d(TAG, "checkInstallSuccess: packageName=" + packageName + ", preVersion=" + preInstalledVersionCode + ", currentVersion=" + currentVersionCode);

        if (preInstalledVersionCode <= 0) {
            // 之前未安装，现在已安装则成功
            return currentVersionCode > 0;
        } else {
            // 之前已安装，版本号变化则成功（覆盖安装）
            // 注意：同版本覆盖安装无法检测，会超时
            return currentVersionCode > 0 && currentVersionCode != preInstalledVersionCode;
        }
    }

    // ==================== 卸载方法 ====================

    /**
     * 卸载应用
     */
    public static void unInstallAPK(Context context, String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}
