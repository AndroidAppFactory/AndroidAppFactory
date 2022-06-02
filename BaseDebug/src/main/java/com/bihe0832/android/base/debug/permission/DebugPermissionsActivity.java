package com.bihe0832.android.base.debug.permission;

import android.Manifest;
import android.Manifest.permission;
import com.bihe0832.android.lib.permission.ui.PermissionDialog;
import com.bihe0832.android.lib.permission.ui.PermissionsActivity;
import java.util.List;


public class DebugPermissionsActivity extends PermissionsActivity {

    @Override
    protected PermissionDialog getDialog(String permission) {
        if (permission.equals(Manifest.permission.CAMERA)) {
            return new DebugPermissionDialog(this);
        } else {
            return super.getDialog(permission);
        }
    }


    @Override
    protected PermissionDialog getDialog(List<String> tempPermissionList) {
        return super.getDialog(tempPermissionList);
//        return new DebugPermissionDialog(this);
    }

    @Override
    protected void onPermissionDialogPositiveClick(List<String> tempPermissionList) {
        onPermissionDialogPositiveClick(permission.SYSTEM_ALERT_WINDOW);
    }

    @Override
    protected boolean checkAllPermissionsResult() {
        return true;
    }
}
