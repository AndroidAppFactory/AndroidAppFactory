package com.bihe0832.android.common.permission.special;

import android.Manifest;
import android.provider.Settings;
import com.bihe0832.android.common.permission.AAFPermissionManager;
import com.bihe0832.android.common.permission.R;
import com.bihe0832.android.lib.permission.PermissionManager;
import com.bihe0832.android.lib.permission.ui.PermissionDialog;
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2;
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener;
import com.bihe0832.android.lib.utils.intent.IntentUtils;
import java.util.Arrays;
import java.util.List;


public class PermissionsActivityWithSpecial extends PermissionsActivityV2 {

    @Override
    protected boolean needCheckPermission(String permission) {
        return !AAFPermissionManager.INSTANCE.permissionExtraCheckIsOK(this, permission) || super.needCheckPermission(
                permission);
    }

    @Override
    protected void doRequestPermissionsAction(String... permissions) {
        boolean hasSpecial = false;
        for (String permission : permissions) {
            if (!AAFPermissionManager.INSTANCE.permissionExtraCheckIsOK(this, permission)) {
                doSpecialCheck(permission);
                hasSpecial = true;
                break;
            }
        }
        if (!hasSpecial) {
            super.doRequestPermissionsAction(permissions);
        }
    }

    protected void doSpecialCheck(String permission) {
        if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                && !AAFPermissionManager.INSTANCE.isLocationEnabled(this)) {
            PermissionDialog dialog = getLocationPermissionDialog(permission);
            dialog.show();
        }
    }

    protected PermissionDialog getLocationPermissionDialog(String permission) {
        PermissionDialog dialog = new PermissionDialog(this);
        String content = PermissionManager.INSTANCE.getPermissionContent(this, scene, Arrays.asList(permission),
                getUseDefault(), getNeedSpecial());
        dialog.setTitle(getString(R.string.common_permission_title_location));
        dialog.setHtmlContent(content);
        dialog.setPositive(getString(R.string.common_permission_title_location));
        dialog.setNegative(getString(R.string.common_permission_action_delay));
        dialog.setShouldCanceled(true);
        dialog.setOnClickBottomListener(new OnDialogListener() {
            @Override
            public void onPositiveClick() {
                dialog.dismiss();
                IntentUtils.startAppSettings(PermissionsActivityWithSpecial.this,
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            }

            @Override
            public void onNegativeClick() {
                dialog.dismiss();
                for (String permissionGroupID : needCheckPermissionGroup) {
                    List<String> permissionsOfGroup = PermissionManager.INSTANCE.getPermissionsByGroupID(scene,
                            permissionGroupID);
                    if (permissionsOfGroup.contains(permission)) {
                        notifyUserCancel(permissionGroupID, permission);
                        finish();
                    }
                }
                notifyUserCancel("", permission);
                finish();
            }

            @Override
            public void onCancel() {
                onNegativeClick();
            }
        });
        return dialog;
    }
}
