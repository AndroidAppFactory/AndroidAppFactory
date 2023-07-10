package com.bihe0832.android.framework.permission.special;

import android.Manifest;
import android.provider.Settings;

import com.bihe0832.android.framework.permission.AAFPermissionManager;
import com.bihe0832.android.lib.permission.PermissionManager;
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2;
import com.bihe0832.android.lib.ui.dialog.CommonDialog;
import com.bihe0832.android.lib.ui.dialog.OnDialogListener;
import com.bihe0832.android.lib.utils.intent.IntentUtils;

import java.util.Arrays;
import java.util.List;


public class PermissionsActivityWithSpecial extends PermissionsActivityV2 {


    @Override
    protected void allPermissionsGranted() {
        boolean isAllOK = true;
        for (String permissionGroupID : needCheckPermissionGroup) {
            List<String> permissions = PermissionManager.INSTANCE.getPermissionsByGroupID(scene, permissionGroupID);
            for (String permission : permissions) {
                if (!AAFPermissionManager.INSTANCE.permissionExtraCheckIsOK(this, permission)) {
                    isAllOK = false;
                    doSpecialCheck(permissionGroupID, permission);
                    break;
                }
            }
        }

        if (isAllOK) {
            super.allPermissionsGranted();
        }
    }

    protected void doSpecialCheck(String permissionGroupID, String permission) {
        if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION) && !LocationPermissionWrapper.INSTANCE.isLocationEnabled(this)) {
            String content = PermissionManager.INSTANCE.getPermissionContent(this, scene, Arrays.asList(permission), getUseDefault(), getNeedSpecial());
            CommonDialog dialog = LocationPermissionWrapper.INSTANCE.getEnabledDialog(this, content);
            dialog.setOnClickBottomListener(new OnDialogListener() {
                @Override
                public void onPositiveClick() {
                    dialog.dismiss();
                    IntentUtils.startAppSettings(PermissionsActivityWithSpecial.this, Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                }

                @Override
                public void onNegativeClick() {
                    dialog.dismiss();
                    notifyUserCancel(permissionGroupID, permission);
                    finish();
                }

                @Override
                public void onCancel() {
                    onNegativeClick();
                }
            });
            dialog.show();

        }
    }
}
