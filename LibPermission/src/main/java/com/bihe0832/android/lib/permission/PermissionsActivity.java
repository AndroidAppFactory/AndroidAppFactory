package com.bihe0832.android.lib.permission;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import com.bihe0832.android.lib.ui.dialog.CommonDialog;
import com.bihe0832.android.lib.ui.dialog.OnDialogListener;
import com.bihe0832.android.lib.utils.apk.APKUtils;


public class PermissionsActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 0; // 系统权限管理页面的参数
    public static final String EXTRA_PERMISSIONS = "com.bihe0832.android.lib.permission.extra_permission"; // 权限参数
    public static final String EXTRA_CAN_CANCEL = "com.bihe0832.android.lib.permission.can.cancel"; // 权限参数

    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案

    private PermissionsChecker mChecker; // 权限检测器
    private boolean isRequireCheck; // 是否需要系统权限检测, 防止和系统提示框重叠
    private CommonDialog dialog = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("PermissionsActivity need check permission");
        }
        setContentView(R.layout.com_bihe0832_common_permissions_activity);

        mChecker = new PermissionsChecker(this);
        isRequireCheck = true;
        dialog = new CommonDialog(this);
        dialog.setTitle(getResources().getString(R.string.permission_title));
        dialog.setNegtive(getResources().getString(R.string.permission_negtive));
        dialog.setPositive(getResources().getString(R.string.permission_positive));
        dialog.setShouldCanceled(canCancel());
        dialog.setOnClickBottomListener(new OnDialogListener() {
            @Override
            public void onPositiveClick() {
                startAppSettings();
            }

            @Override
            public void onNegtiveClick() {
                PermissionManager.INSTANCE.getPermissionCheckResultListener().onUserCancel();
                finish();
            }

            @Override
            public void onCancel() {
                PermissionManager.INSTANCE.getPermissionCheckResultListener().onUserCancel();
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRequireCheck) {
            String[] permissions = getPermissions();
            if (mChecker.lacksPermissions(permissions)) {
                requestPermissions(permissions); // 请求权限
            } else {
                allPermissionsGranted(); // 全部权限都已获取
            }
        } else {
            isRequireCheck = true;
        }
    }

    // 返回传递的权限参数
    private String[] getPermissions() {
        return getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
    }

    private boolean canCancel() {
        return getIntent().getBooleanExtra(EXTRA_CAN_CANCEL, false);
    }

    // 请求权限兼容低版本
    private void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    // 全部权限均已获取
    private void allPermissionsGranted() {
        PermissionManager.INSTANCE.getPermissionCheckResultListener().onSuccess();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        int firstDenied = hasAllPermissionsGranted(grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && firstDenied < 0) {
            isRequireCheck = true;
            allPermissionsGranted();
        } else {
            isRequireCheck = false;
            showMissingPermissionDialog(permissions[firstDenied]);
        }
    }

    // 含有全部的权限
    private int hasAllPermissionsGranted(@NonNull int[] grantResults) {
        int firstDenied = -1;
        for (int grantResult : grantResults) {
            firstDenied++;
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return firstDenied;
            }
        }
        return firstDenied;
    }


    private void showMissingPermissionDialog(String permission) {
        dialog.setHtmlContent(
                APKUtils.getAppName(this) + "的"
                        + PermissionManager.INSTANCE.getPermissionScene(permission)
                        + "功能需要您开启"
                        + PermissionManager.INSTANCE.getPermissionDesc(permission)
                        + "权限，缺少权限在使用中可能会出现部分功能异常。"
        );
        dialog.show();
    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }
}
