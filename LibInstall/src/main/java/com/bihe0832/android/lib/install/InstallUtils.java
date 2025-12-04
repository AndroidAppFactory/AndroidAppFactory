package com.bihe0832.android.lib.install;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes;
import com.bihe0832.android.lib.file.provider.ZixieFileProvider;
import com.bihe0832.android.lib.install.apk.APKInstall;
import com.bihe0832.android.lib.install.obb.OBBFormats;
import com.bihe0832.android.lib.install.obb.ObbFileInstall;
import com.bihe0832.android.lib.install.splitapk.SplitApksInstallHelper;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.timer.BaseTask;
import com.bihe0832.android.lib.timer.TaskManager;
import com.bihe0832.android.lib.ui.toast.ToastUtil;
import com.bihe0832.android.lib.utils.MathUtils;
import com.bihe0832.android.lib.utils.intent.IntentUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import com.bihe0832.android.lib.zip.ZipUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by zixie on 2017/11/1.
 * <p>
 * APK 安装工具类，支持单个 APK、Split APKs、OBB 等多种安装方式。
 * <p>
 * 使用前提：按照 {@link ZixieFileProvider} 的说明定义好 lib_bihe0832_file_folder 和 zixie_file_paths.xml，
 * 或者直接将文件放在 {@link ZixieFileProvider#getZixieFilePath(Context)} 的子目录。
 * <p>
 * 如果不使用库自定义的 FileProvider，请使用 {@link InstallUtils#installAPP(Context, Uri, File)} 安装。
 */
public class InstallUtils {

    public static final String TAG = "InstallUtils";

    // ==================== APK 安装类型枚举 ====================

    /**
     * APK 安装类型
     */
    public enum ApkInstallType {
        NULL,       // 无效类型
        APK,        // 单个 APK
        OBB,        // 带 OBB 文件的 APK
        SPLIT_APKS  // Split APKs（多个 APK 文件）
    }

    // ==================== 公共 API ====================

    /**
     * 获取文件的安装类型
     *
     * @param filepath 文件路径（APK、ZIP 或文件夹）
     * @return 安装类型
     */
    public static ApkInstallType getFileType(String filepath) {
        if (TextUtils.isEmpty(filepath)) {
            return ApkInstallType.NULL;
        }
        if (FileMimeTypes.INSTANCE.isApkFile(filepath)) {
            return ApkInstallType.APK;
        }
        File file = new File(filepath);
        if (file.isDirectory()) {
            return getApkInstallTypeByFolder(file);
        }
        return getApkInstallTypeByZip(filepath);
    }

    /**
     * 检查是否有安装 APK 的权限
     *
     * @param context      上下文
     * @param showToast    无权限时是否显示 Toast 提示
     * @param autoSettings 无权限时是否自动跳转设置页
     * @return 是否有安装权限
     */
    public static boolean hasInstallAPPPermission(Context context, boolean showToast, boolean autoSettings) {
        boolean hasPermission = true;
        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.O) {
            try {
                hasPermission = context.getPackageManager().canRequestPackageInstalls();
            } catch (Exception e) {
                hasPermission = false;
                e.printStackTrace();
            }
            if (!hasPermission) {
                if (showToast) {
                    ToastUtil.showShort(context, context.getString(R.string.install_permission));
                }
                if (autoSettings) {
                    IntentUtils.startAppSettings(context, Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                }
            }
        }
        return hasPermission;
    }

    /**
     * 卸载应用
     *
     * @param context     上下文
     * @param packageName 包名
     */
    public static void uninstallAPP(Context context, String packageName) {
        APKInstall.unInstallAPK(context, packageName);
    }

    /**
     * 使用自定义 FileProvider 安装 APK
     *
     * @param context      上下文
     * @param fileProvider FileProvider URI
     * @param file         APK 文件
     */
    public static void installAPP(Context context, Uri fileProvider, File file) {
        APKInstall.installAPKByProvider(context, fileProvider, file, "", 0, null);
    }

    /**
     * 安装 APK（简化版）
     *
     * @param context  上下文
     * @param filePath 文件路径
     */
    public static void installAPP(Context context, String filePath) {
        installAPP(context, filePath, "", 0, null);
    }

    public static void installAPP(Context context, String filePath, String packageName, InstallListener listener) {
        if (FileUtils.INSTANCE.checkFileExist(filePath)) {
            File file = new File(filePath);
            int compareTime = (int) (file.length() / (FileUtils.SPACE_MB * 5));
            installAPP(context, filePath, packageName, MathUtils.getMax(60, compareTime), null);
        } else {
            listener.onInstallFailed(InstallErrorCode.FILE_NOT_FOUND);
        }

    }

    /**
     * 安装 APK（完整参数版）
     * <p>
     * 安装逻辑：
     * - Android >= 8.0：直接安装，系统会自动弹出权限对话框
     * - Android < 8.0 且有权限：直接安装
     * - Android < 8.0 且无权限：跳转设置页 + 定时轮询等待授权
     *
     * @param context     上下文
     * @param filePath    文件路径
     * @param packageName 包名（用于解压目录命名）
     * @param delayTime   等待权限的超时时间（秒），仅 Android < 8.0 且无权限时有效
     * @param listener    安装监听器
     */
    public static void installAPP(Context context, String filePath, String packageName, int delayTime, InstallListener listener) {
        // Android >= 8.0 或已有权限：直接安装
        boolean shouldInstallDirectly = BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.O || hasInstallAPPPermission(context, false, false);

        if (shouldInstallDirectly) {
            doInstallAPP(context, filePath, packageName, delayTime, listener);
        } else if (delayTime > 0) {
            // Android < 8.0 且无权限：等待授权后安装
            waitPermissionAndInstall(context, filePath, packageName, delayTime, listener);
        } else {
            ZLog.d(TAG, "installAPP: no permission and autoCheck disabled");
        }
    }

    // ==================== 安装执行方法 ====================

    /**
     * 直接执行安装（不检查权限，在后台线程执行）
     */
    private static void doInstallAPP(Context context, String filePath, String packageName, int delayTime, InstallListener listener) {
        ThreadManager.getInstance().start(() -> {
            ZLog.d(TAG, "doInstallAPP start: " + filePath);
            installByFileType(context, filePath, packageName, delayTime, wrapListener(listener));
        });
    }

    /**
     * 等待权限授予后再安装（Android < 8.0 且无权限时使用）
     */
    private static void waitPermissionAndInstall(Context context, String filePath, String packageName, int delayTime, InstallListener listener) {
        // 显示提示并跳转设置页
        hasInstallAPPPermission(context, true, true);

        final String taskName = "installAPP_" + filePath.hashCode();
        TaskManager.getInstance().addTask(new BaseTask() {
            private int times = 0;

            @Override
            public int getMyInterval() {
                return 2;
            }

            @Override
            public int getNextEarlyRunTime() {
                return 0;
            }

            @Override
            public void doTask() {
                times++;
                if (hasInstallAPPPermission(context, false, false)) {
                    ZLog.d(TAG, "waitPermissionAndInstall: permission granted");
                    doInstallAPP(context, filePath, packageName, delayTime, listener);
                    TaskManager.getInstance().removeTask(taskName);
                } else {
                    ZLog.d(TAG, "waitPermissionAndInstall: waiting for permission");
                }
                if (times > delayTime) {
                    ZLog.d(TAG, "waitPermissionAndInstall: timeout after " + times + "s");
                    TaskManager.getInstance().removeTask(taskName);
                    if (listener != null) {
                        listener.onInstallTimeOut();
                    }
                }
            }

            @Override
            public String getTaskName() {
                return taskName;
            }
        });
    }

    /**
     * 包装监听器，添加日志输出
     */
    private static InstallListener wrapListener(InstallListener listener) {
        return new InstallListener() {
            @Override
            public void onInstallTimeOut() {
                ZLog.d(TAG, "onInstallTimeOut");
                if (listener != null) listener.onInstallTimeOut();
            }

            @Override
            public void onInstallSuccess() {
                ZLog.d(TAG, "onInstallSuccess");
                if (listener != null) listener.onInstallSuccess();
            }

            @Override
            public void onUnCompress() {
                ZLog.d(TAG, "onUnCompress");
                if (listener != null) listener.onUnCompress();
            }

            @Override
            public void onInstallPrepare() {
                ZLog.d(TAG, "onInstallPrepare");
                if (listener != null) listener.onInstallPrepare();
            }

            @Override
            public void onInstallStart() {
                ZLog.d(TAG, "onInstallStart");
                if (listener != null) listener.onInstallStart();
            }

            @Override
            public void onInstallFailed(int errorCode) {
                ZLog.d(TAG, "onInstallFailed: " + errorCode);
                if (listener != null) listener.onInstallFailed(errorCode);
            }
        };
    }

    // ==================== 按文件类型安装 ====================

    /**
     * 根据文件类型选择安装方式
     */
    static void installByFileType(Context context, String filePath, String packageName, int delayTime, InstallListener listener) {
        try {
            File file = new File(filePath);
            ZLog.d(TAG, "installByFileType: " + file.getAbsolutePath());

            if (!file.exists()) {
                listener.onInstallFailed(InstallErrorCode.FILE_NOT_FOUND);
                return;
            }

            if (FileMimeTypes.INSTANCE.isApkFile(filePath)) {
                // 单个 APK 文件
                APKInstall.installAPK(context, file.getAbsolutePath(), delayTime, listener);
            } else if (ZipUtils.isZipFile(file.getAbsolutePath(), true)) {
                // ZIP 文件
                installFromZip(context, filePath, packageName, delayTime, listener);
            } else if (file.isDirectory()) {
                // 文件夹
                installFromFolder(context, file.getAbsolutePath(), packageName, delayTime, listener);
            } else {
                // 其他文件，尝试直接安装
                APKInstall.installAPK(context, file.getAbsolutePath(), delayTime, listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZLog.d(TAG, "installByFileType failed: " + e.getMessage());
            listener.onInstallFailed(InstallErrorCode.UNKNOWN_EXCEPTION);
        }
    }

    // ==================== ZIP 文件安装 ====================

    /**
     * 从 ZIP 文件安装
     */
    static void installFromZip(@NotNull Context context, String zipFilePath, String packageName, int delayTime, InstallListener listener) {
        ZLog.d(TAG, "installFromZip: " + zipFilePath);

        String finalPackageName = TextUtils.isEmpty(packageName) ? FileUtils.INSTANCE.getFileNameWithoutEx(zipFilePath) : packageName;

        ApkInstallType type = getApkInstallTypeByZip(zipFilePath);

        if (type == ApkInstallType.OBB) {
            ObbFileInstall.installObbAPKByZip(context, zipFilePath, finalPackageName, delayTime, listener);
        } else if (type == ApkInstallType.SPLIT_APKS) {
            // Split APKs：解压后使用 PackageInstaller
            String extractDir = ZixieFileProvider.getZixieCacheFolder(context) + finalPackageName;
            ZLog.d(TAG, "installFromZip: extracting to " + extractDir);
            listener.onUnCompress();
            ZipUtils.unCompress(zipFilePath, extractDir);

            ArrayList<String> apkFiles = new ArrayList<>();
            collectApkFiles(new File(extractDir), apkFiles);
            SplitApksInstallHelper.INSTANCE.installApk(context, apkFiles, delayTime, listener);
        } else {
            // 其他类型：解压后从文件夹安装
            String extractDir = ZixieFileProvider.getZixieCacheFolder(context) + finalPackageName;
            ZLog.d(TAG, "installFromZip: extracting to " + extractDir);
            listener.onUnCompress();
            ZipUtils.unCompress(zipFilePath, extractDir);
            installFromFolder(context, extractDir, finalPackageName, delayTime, listener);
        }
    }

    // ==================== 文件夹安装 ====================

    /**
     * 从文件夹安装
     */
    static void installFromFolder(@NotNull Context context, String folderPath, String packageName, int delayTime, InstallListener listener) {
        ZLog.d(TAG, "installFromFolder: " + folderPath);

        String finalPackageName = TextUtils.isEmpty(packageName) ? FileUtils.INSTANCE.getFileName(folderPath) : packageName;

        File folder = new File(folderPath);
        ApkInstallType type = getApkInstallTypeByFolder(folder);
        ZLog.d(TAG, "installFromFolder type: " + type);

        if (type == ApkInstallType.OBB) {
            ObbFileInstall.installObbAPKByFile(context, folderPath, finalPackageName, delayTime, listener);
        } else if (type == ApkInstallType.SPLIT_APKS) {
            // Split APKs：使用 PackageInstaller
            ArrayList<String> apkFiles = new ArrayList<>();
            collectApkFiles(folder, apkFiles);
            SplitApksInstallHelper.INSTANCE.installApk(context, apkFiles, delayTime, listener);
        } else {
            // 其他类型：找到第一个 APK 文件安装
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (FileMimeTypes.INSTANCE.isApkFile(file.getAbsolutePath())) {
                        APKInstall.installAPK(context, file.getAbsolutePath(), delayTime, listener);
                        return;
                    }
                }
            }
            listener.onInstallFailed(InstallErrorCode.FILE_NOT_FOUND);
        }
    }

    // ==================== 文件类型判断 ====================

    /**
     * 根据 ZIP 文件内容判断安装类型
     */
    static ApkInstallType getApkInstallTypeByZip(String zipFile) {
        if (zipFile == null) {
            return ApkInstallType.APK;
        }

        int apkCount = 0;
        for (String fileName : ZipUtils.getFileList(zipFile)) {
            if (OBBFormats.isObbFile(fileName)) {
                return ApkInstallType.OBB;
            }
            if (FileMimeTypes.INSTANCE.isApkFile(fileName)) {
                apkCount++;
                if (apkCount > 1) {
                    return ApkInstallType.SPLIT_APKS;
                }
            }
        }

        return apkCount > 0 ? ApkInstallType.APK : ApkInstallType.NULL;
    }

    /**
     * 根据文件夹内容判断安装类型
     */
    static ApkInstallType getApkInstallTypeByFolder(File folder) {
        if (folder == null || !folder.exists()) {
            return ApkInstallType.NULL;
        }

        int apkCount = 0;
        LinkedList<File> pendingFolders = new LinkedList<>();
        pendingFolders.add(folder);

        while (!pendingFolders.isEmpty()) {
            File currentFolder = pendingFolders.removeFirst();
            File[] files = currentFolder.listFiles();
            if (files == null) continue;

            for (File file : files) {
                if (file.isDirectory()) {
                    pendingFolders.add(file);
                } else if (OBBFormats.isObbFile(file.getAbsolutePath())) {
                    return ApkInstallType.OBB;
                } else if (FileMimeTypes.INSTANCE.isApkFile(file.getAbsolutePath())) {
                    apkCount++;
                    if (apkCount > 1) {
                        return ApkInstallType.SPLIT_APKS;
                    }
                }
            }
        }

        return apkCount > 0 ? ApkInstallType.APK : ApkInstallType.NULL;
    }

    // ==================== 工具方法 ====================

    /**
     * 递归收集文件夹中的所有 APK 文件
     */
    static void collectApkFiles(File folder, ArrayList<String> files) {
        if (folder == null) return;

        if (folder.isDirectory()) {
            File[] children = folder.listFiles();
            if (children != null) {
                for (File child : children) {
                    collectApkFiles(child, files);
                }
            }
        } else if (FileMimeTypes.INSTANCE.isApkFile(folder.getName())) {
            files.add(folder.getAbsolutePath());
        }
    }
}
