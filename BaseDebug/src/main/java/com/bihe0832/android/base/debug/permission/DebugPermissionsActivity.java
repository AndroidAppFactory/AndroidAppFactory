package com.bihe0832.android.base.debug.permission;

import android.Manifest;
import android.Manifest.permission;

import com.bihe0832.android.lib.permission.ui.PermissionDialog;
import com.bihe0832.android.lib.permission.ui.PermissionsActivity;
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2;

import java.util.List;


public class DebugPermissionsActivity extends PermissionsActivityV2 {

//    @Override
//    protected PermissionDialog getDialog(String permission) {
//        if (permission.equals(Manifest.permission.CAMERA)) {
//            return new DebugPermissionDialog(this);
//        } else {
//            return super.getDialog(permission);
//        }
//    }


    @Override
    protected PermissionDialog getDialog(List<String> tempPermissionGroupList) {
        return super.getDialog(tempPermissionGroupList);
//        return new DebugPermissionDialog(this);
    }

    @Override
    protected void onPermissionDialogPositiveClick(List<String> tempPermissionGroupList) {
        onPermissionDialogPositiveClick(permission.SYSTEM_ALERT_WINDOW);
    }

    @Override
    protected boolean checkAllPermissionsResult() {
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, com.bihe0832.android.framework.R.anim.fade_out);
    }
}
