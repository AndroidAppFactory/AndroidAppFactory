package com.bihe0832.android.lib.permission.ui;

import static com.bihe0832.android.lib.permission.PermissionManager.PERMISSION_REQUEST_CODE;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import com.bihe0832.android.lib.permission.PermissionManager;
import com.bihe0832.android.lib.permission.PermissionsChecker;
import com.bihe0832.android.lib.permission.R;
import com.bihe0832.android.lib.ui.dialog.OnDialogListener;
import com.bihe0832.android.lib.utils.intent.IntentUtils;
import java.util.ArrayList;
import java.util.List;


public class PermissionsActivity extends Activity {

    public static final String EXTRA_PERMISSIONS = "com.bihe0832.android.lib.permission.extra_permission"; // 权限参数
    public static final String EXTRA_SOURCE = "com.bihe0832.android.lib.permission.extra_source"; // 权限参数
    public static final String EXTRA_CAN_CANCEL = "com.bihe0832.android.lib.permission.can.cancel"; // 权限参数


    private PermissionsChecker permissionsChecker; // 权限检测器
    private boolean isRequireCheck; // 是否需要系统权限检测, 防止和系统提示框重叠
    private String[] needCheckPermission = null;
    private boolean canCancle = false;
    private String scene = "";

    private PermissionDialog dialog = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("PermissionsActivity need check permission");
        }
        setContentView(R.layout.com_bihe0832_lib_permissions_activity);
        try {
            if (getIntent().hasExtra(EXTRA_PERMISSIONS)) {
                needCheckPermission = getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
            }

            if (getIntent().hasExtra(EXTRA_CAN_CANCEL)) {
                canCancle = getIntent().getBooleanExtra(EXTRA_CAN_CANCEL, false);
            }

            if (getIntent().hasExtra(EXTRA_SOURCE)) {
                scene = getIntent().getStringExtra(EXTRA_SOURCE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (needCheckPermission == null || needCheckPermission.length < 1) {
            PermissionManager.INSTANCE.getPermissionCheckResultListener().onFailed("permission error");
            finish();
        }

        permissionsChecker = new PermissionsChecker(this);
        isRequireCheck = true;
    }


    protected PermissionDialog getDialog(String permission) {
        return new PermissionDialog(this);
    }

    protected PermissionDialog getDialog(List<String> tempPermissionList) {
        return new PermissionDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRequireCheck) {
            ArrayList<String> needCheckList = new ArrayList<>();
            for (String permission : needCheckPermission) {
                if (permissionsChecker.lacksPermission(permission)) {
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
        List<String> tempPermissionList = new ArrayList<>();
        for (String permission : needCheckPermission) {
            if (permissionsChecker.lacksPermission(permission)) {
                tempPermissionList.add(permission);
                if (!checkAllPermissionsResult()) {
                    tempPermission = permission;
                    break;
                }
            }
        }
        if (!TextUtils.isEmpty(tempPermission)) {
            isRequireCheck = false;
            showMissingPermissionDialog(tempPermission);
        } else if (tempPermissionList.size() > 0) {
            isRequireCheck = false;
            showMissingPermissionDialog(tempPermissionList);
        } else {
            isRequireCheck = true;
            allPermissionsGranted(); // 全部权限都已获取
        }
    }

    protected boolean checkAllPermissionsResult() {
        return false;
    }

    private void showMissingPermissionDialog(final List<String> tempPermissionList) {
        if (dialog == null) {
            dialog = getDialog(tempPermissionList);
        }
        if (!dialog.isShowing()) {
            dialog.show(scene, tempPermissionList, canCancle, new OnDialogListener() {
                @Override
                public void onPositiveClick() {
                    onPermissionDialogPositiveClick(tempPermissionList);
                }

                @Override
                public void onNegativeClick() {
                    for (String permission : tempPermissionList) {
                        PermissionManager.INSTANCE.setUserDenyTime(permission);
                    }
                    PermissionManager.INSTANCE.getPermissionCheckResultListener()
                            .onUserCancel(scene, tempPermissionList.get(0));
                    dialog.dismiss();
                    finish();
                }

                @Override
                public void onCancel() {
                    for (String permission : tempPermissionList) {
                        PermissionManager.INSTANCE.setUserDenyTime(permission);
                    }
                    PermissionManager.INSTANCE.getPermissionCheckResultListener()
                            .onUserCancel(scene, tempPermissionList.get(0));
                    dialog.dismiss();
                    finish();
                }
            });
        }
    }

    private void showMissingPermissionDialog(final String showPermission) {
        getDialog(showPermission).show(scene, showPermission, canCancle, new OnDialogListener() {
            @Override
            public void onPositiveClick() {
                onPermissionDialogPositiveClick(showPermission);
            }


            @Override
            public void onNegativeClick() {
                PermissionManager.INSTANCE.setUserDenyTime(showPermission);
                PermissionManager.INSTANCE.getPermissionCheckResultListener().onUserCancel(scene, showPermission);
                finish();
            }

            @Override
            public void onCancel() {
                PermissionManager.INSTANCE.setUserDenyTime(showPermission);
                PermissionManager.INSTANCE.getPermissionCheckResultListener().onUserCancel(scene, showPermission);
                finish();
            }
        });
    }

    protected void onPermissionDialogPositiveClick(final List<String> tempPermissionList) {
        IntentUtils.startAppDetailSettings(PermissionsActivity.this);
    }

    protected void onPermissionDialogPositiveClick(final String showPermission) {
        String permissionSettings = PermissionManager.INSTANCE.getPermissionSettings(showPermission);
        if (!IntentUtils.startAppSettings(PermissionsActivity.this, permissionSettings, false)) {
            if (!IntentUtils.startSettings(PermissionsActivity.this, permissionSettings)) {
                IntentUtils.startAppDetailSettings(PermissionsActivity.this);
            }
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (dialog != null && dialog.isShowing()) {
//            dialog.cancel();
//        }
//    }
}
