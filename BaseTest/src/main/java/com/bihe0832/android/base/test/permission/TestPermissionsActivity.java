package com.bihe0832.android.base.test.permission;

import android.Manifest;
import android.Manifest.permission;
import com.bihe0832.android.lib.permission.ui.PermissionDialog;
import com.bihe0832.android.lib.permission.ui.PermissionsActivity;
import java.util.List;


public class TestPermissionsActivity extends PermissionsActivity {

    @Override
    protected PermissionDialog getDialog(String permission) {
        if (permission.equals(Manifest.permission.CAMERA)) {
            return new TestPermissionDialog(this);
        } else {
            return super.getDialog(permission);
        }
    }


    @Override
    protected PermissionDialog getDialog(List<String> tempPermissionList) {
        return super.getDialog(tempPermissionList);
//        return new TestPermissionDialog(this);
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
