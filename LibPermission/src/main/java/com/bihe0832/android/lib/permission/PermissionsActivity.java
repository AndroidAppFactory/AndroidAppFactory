package com.bihe0832.android.lib.permission;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import com.bihe0832.android.lib.ui.dialog.OnDialogListener;
import com.bihe0832.android.lib.utils.intent.IntentUtils;
import java.util.ArrayList;


public class PermissionsActivity extends Activity {

    public static final int PERMISSION_REQUEST_CODE = 0; // 系统权限管理页面的参数
    public static final String EXTRA_PERMISSIONS = "com.bihe0832.android.lib.permission.extra_permission"; // 权限参数
    public static final String EXTRA_CAN_CANCEL = "com.bihe0832.android.lib.permission.can.cancel"; // 权限参数


    private PermissionsChecker mChecker; // 权限检测器
    private boolean isRequireCheck; // 是否需要系统权限检测, 防止和系统提示框重叠
    private PermissionDialog dialog = null;
    private String[] needCheckPermission = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("PermissionsActivity need check permission");
        }
        setContentView(R.layout.com_bihe0832_lib_permissions_activity);
        needCheckPermission = getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
        if (needCheckPermission == null || needCheckPermission.length < 1) {
            PermissionManager.INSTANCE.getPermissionCheckResultListener().onFailed("permission error");
            finish();
        }

        mChecker = new PermissionsChecker(this);
        isRequireCheck = true;
        dialog = new PermissionDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRequireCheck) {
            ArrayList<String> needCheckList = new ArrayList<>();
            for (String permission : needCheckPermission) {
                if (mChecker.lacksPermission(permission)) {
                    needCheckList.add(permission);
                }
            }
            if (needCheckList.size() > 0) {
                requestPermissions(needCheckList.toArray(new String[needCheckList.size()])); // 请求权限
            } else {
                allPermissionsGranted(); // 全部权限都已获取
            }
        } else {
            isRequireCheck = true;
        }
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

        String tempPermission = "";
        for (String permission : needCheckPermission) {
            if (mChecker.lacksPermission(permission)) {
                tempPermission = permission;
                break;
            }
        }
        if (!TextUtils.isEmpty(tempPermission)) {
            isRequireCheck = false;
            showMissingPermissionDialog(tempPermission);
        } else {
            isRequireCheck = true;
            allPermissionsGranted(); // 全部权限都已获取
        }
    }

    private void showMissingPermissionDialog(final String showPermission) {
        dialog.show(showPermission, canCancel(), new OnDialogListener() {
            @Override
            public void onPositiveClick() {
                String permissionSettings = PermissionManager.INSTANCE.getPermissionSettings(showPermission);
                if (!IntentUtils.startAppSettings(PermissionsActivity.this, permissionSettings, false)) {
                    if (!IntentUtils.startSettings(PermissionsActivity.this, permissionSettings)) {
                        IntentUtils.startAppDetailSettings(PermissionsActivity.this);
                    }
                }
            }


            @Override
            public void onNegativeClick() {
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
}
