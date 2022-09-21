package com.bihe0832.android.lib.permission.ui;

import static com.bihe0832.android.lib.permission.PermissionManager.PERMISSION_REQUEST_CODE;

import android.app.Activity;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

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
    protected String[] needCheckPermissionGroup = null;
    protected boolean canCancle = false;
    protected String scene = "";
    protected boolean autoDeny = false;
    private PermissionsChecker permissionsChecker; // 权限检测器
    private boolean isRequireCheck; // 是否需要系统权限检测, 防止和系统提示框重叠
    private PermissionDialog dialog = null;
    private long lastCheckTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("PermissionsActivity need check permission");
        }
        setContentView(R.layout.com_bihe0832_lib_permissions_activity);
        try {
            if (getIntent().hasExtra(EXTRA_PERMISSIONS)) {
                needCheckPermissionGroup = getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
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

        if (needCheckPermissionGroup == null || needCheckPermissionGroup.length < 1) {
            PermissionManager.INSTANCE.getPermissionCheckResultListener().onFailed(scene, "permission error");
            finish();
        }

        permissionsChecker = new PermissionsChecker(this);
        isRequireCheck = true;
    }

    protected PermissionDialog getDialog(List<String> tempPermissionGroupList) {
        return new PermissionDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRequireCheck) {
            ArrayList<String> needCheckList = new ArrayList<>();
            for (String permissionGroupID : needCheckPermissionGroup) {
                List<String> permissions = PermissionManager.INSTANCE.getPermissionsByGroupID(scene, permissionGroupID);
                for (String permission : permissions) {
                    if (permissionsChecker.lacksPermission(permission)) {
                        needCheckList.add(permission);
                    }
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
    protected void requestPermissions(String... permissions) {
        lastCheckTime = System.currentTimeMillis();
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    // 全部权限均已获取
    protected void allPermissionsGranted() {
        PermissionManager.INSTANCE.getPermissionCheckResultListener().onSuccess(scene);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        long time = System.currentTimeMillis() - lastCheckTime;
        if (time < 500) {
            autoDeny = true;
        }

        List<String> tempPermissionGroupList = new ArrayList<>();

        for (String permissionGroupID : needCheckPermissionGroup) {
            List<String> permissionsOfGroup = PermissionManager.INSTANCE.getPermissionsByGroupID(scene, permissionGroupID);
            for (String permission : permissionsOfGroup) {
                if (permissionsChecker.lacksPermission(permission)) {
                    tempPermissionGroupList.add(permissionGroupID);
                    break;
                }
            }
            if (tempPermissionGroupList.size() > 0 && !checkAllPermissionsResult()) {
                break;
            }
        }

        if (tempPermissionGroupList.size() > 0) {
            isRequireCheck = false;
            showMissingPermissionDialog(tempPermissionGroupList);
        } else {
            isRequireCheck = true;
            allPermissionsGranted(); // 全部权限都已获取
        }
    }

    protected boolean checkAllPermissionsResult() {
        return false;
    }

    /**
     *
     * @param tempPermissionGroupList 缺少权限的权限组ID
     */
    protected void showMissingPermissionDialog(final List<String> tempPermissionGroupList) {

        for (String permissionGroup : tempPermissionGroupList) {
            PermissionManager.INSTANCE.setUserDenyTime(permissionGroup);
        }
        String firstPermissionGroupID = tempPermissionGroupList.get(0);
        String firstPermission = "";
        List<String> permissionsOfGroup = PermissionManager.INSTANCE.getPermissionsByGroupID(scene, firstPermissionGroupID);
        for (String permission : permissionsOfGroup) {
            if (permissionsChecker.lacksPermission(permission)) {
                firstPermission = permission;
                break;
            }
        }

        if (dialog == null) {
            dialog = getDialog(tempPermissionGroupList);
        }

        if (null != dialog && !dialog.isShowing()) {
            final String finalFirstPermissionGroupID = firstPermissionGroupID;
            String finalFirstPermission = firstPermission;
            dialog.show(scene, tempPermissionGroupList, canCancle, new OnDialogListener() {
                @Override
                public void onPositiveClick() {
                    onPermissionDialogPositiveClick(tempPermissionGroupList);
                }

                @Override
                public void onNegativeClick() {
                    notifyUserCancle(finalFirstPermissionGroupID, finalFirstPermission);
                    dialog.dismiss();
                    finish();
                }

                @Override
                public void onCancel() {
                    notifyUserCancle(finalFirstPermissionGroupID, finalFirstPermission);
                    dialog.dismiss();
                    finish();
                }
            });
        } else {
            if (null != dialog) {
                dialog.dismiss();
            }
            notifyUserCancle(firstPermissionGroupID, firstPermission);
            finish();
        }
    }

    protected void notifyUserCancle(String firstPermissionGroupID, String firstPermission) {
        PermissionManager.INSTANCE.getPermissionCheckResultListener()
                .onUserCancel(scene, firstPermissionGroupID, firstPermission);
    }

    protected void onPermissionDialogPositiveClick(final List<String> tempPermissionGroupList) {
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
}
