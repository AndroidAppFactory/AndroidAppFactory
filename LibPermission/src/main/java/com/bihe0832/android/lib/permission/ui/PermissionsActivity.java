package com.bihe0832.android.lib.permission.ui;

import static com.bihe0832.android.lib.permission.PermissionManager.PERMISSION_REQUEST_CODE;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.bihe0832.android.lib.language.MultiLanguageHelper;
import com.bihe0832.android.lib.permission.PermissionManager;
import com.bihe0832.android.lib.permission.PermissionsChecker;
import com.bihe0832.android.lib.permission.R;
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener;
import com.bihe0832.android.lib.utils.intent.IntentUtils;
import java.util.ArrayList;
import java.util.List;


public class PermissionsActivity extends AppCompatActivity {

    public static final String EXTRA_PERMISSIONS = "com.bihe0832.android.lib.permission.extra_permission"; // 权限参数
    public static final String EXTRA_SOURCE = "com.bihe0832.android.lib.permission.extra_source"; // 权限参数
    public static final String EXTRA_CAN_CANCEL = "com.bihe0832.android.lib.permission.can.cancel"; // 权限参数
    protected String[] needCheckPermissionGroup = null;
    protected boolean canCancle = false;
    protected String scene = "";
    private boolean autoDeny = false;
    private PermissionsChecker permissionsChecker; // 权限检测器
    private boolean isRequireCheck; // 是否需要系统权限检测, 防止和系统提示框重叠
    private PermissionDialog dialog = null;
    private long lastCheckTime = 0L;


    @Override
    protected void attachBaseContext(Context newBase) {
        if (newBase != null) {
            Context newContext = MultiLanguageHelper.INSTANCE.modifyContextLanguageConfig(newBase);
            super.attachBaseContext(newContext);
        } else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("PermissionsActivity need check permission");
        }
        MultiLanguageHelper.INSTANCE.modifyContextLanguageConfig(
                getResources(), MultiLanguageHelper.INSTANCE.getLanguageConfig(this)
        );
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
        startCheckPermission();
    }

    protected void startCheckPermission() {
        if (isRequireCheck) {
            ArrayList<String> needCheckList = new ArrayList<>();
            for (String permissionGroupID : needCheckPermissionGroup) {
                List<String> permissions = PermissionManager.INSTANCE.getPermissionsByGroupID(scene, permissionGroupID);
                for (String permission : permissions) {
                    if (needCheckPermission(permission)) {
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

    protected boolean needCheckPermission(String permission) {
        return permissionsChecker.lacksPermission(permission);
    }

    private void requestPermissions(String... permissions) {
        lastCheckTime = System.currentTimeMillis();
        doRequestPermissionsAction(permissions);
    }

    // 请求权限，如果有类似用户拒绝授权多久不能再次授权的逻辑，可以再此处处理
    protected void doRequestPermissionsAction(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    // 全部权限均已获取
    protected void allPermissionsGranted() {
        finish();
        PermissionManager.INSTANCE.getPermissionCheckResultListener().onSuccess(scene);
    }

    protected void setAutoDeny() {
        autoDeny = true;
    }

    // 如果是 autoDeny 为 true，此时直接弹框，不用校验
    public boolean isAutoDeny() {
        return autoDeny;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        long time = System.currentTimeMillis() - lastCheckTime;
        if (time < 1000) {
            setAutoDeny();
        }

        List<String> tempPermissionGroupList = new ArrayList<>();

        for (String permissionGroupID : needCheckPermissionGroup) {
            List<String> permissionsOfGroup = PermissionManager.INSTANCE.getPermissionsByGroupID(scene,
                    permissionGroupID);
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
     * @param tempPermissionGroupList 缺少权限的权限组ID
     */
    protected void showMissingPermissionDialog(final List<String> tempPermissionGroupList) {

        for (String permissionGroup : tempPermissionGroupList) {
            PermissionManager.INSTANCE.setUserDenyTime(permissionGroup);
        }
        String firstPermissionGroupID = tempPermissionGroupList.get(0);
        String firstPermission = "";
        List<String> permissionsOfGroup = PermissionManager.INSTANCE.getPermissionsByGroupID(scene,
                firstPermissionGroupID);
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
                    notifyUserCancel(finalFirstPermissionGroupID, finalFirstPermission);
                    dialog.dismiss();
                    finish();
                }

                @Override
                public void onCancel() {
                    notifyUserCancel(finalFirstPermissionGroupID, finalFirstPermission);
                    dialog.dismiss();
                    finish();
                }
            });
        } else {
            if (null != dialog) {
                dialog.dismiss();
            }
            notifyUserCancel(firstPermissionGroupID, firstPermission);
            finish();
        }
    }

    protected void notifyUserCancel(String firstPermissionGroupID, String firstPermission) {
        PermissionManager.INSTANCE.getPermissionCheckResultListener()
                .onUserCancel(scene, firstPermissionGroupID, firstPermission);
    }

    protected void onPermissionDialogPositiveClick(final List<String> tempPermissionGroupList) {
        if (tempPermissionGroupList.size() > 0) {
            if (tempPermissionGroupList.size() > 1) {
                IntentUtils.startAppDetailSettings(PermissionsActivity.this);
            } else {
                onPermissionDialogPositiveClick(tempPermissionGroupList.get(0));
            }
        }
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
