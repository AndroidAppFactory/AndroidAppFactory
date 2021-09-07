package com.bihe0832.android.base.test.permission;

import com.bihe0832.android.lib.permission.ui.PermissionDialog;
import com.bihe0832.android.lib.permission.ui.PermissionsActivity;


public class TestPermissionsActivity extends PermissionsActivity {

    @Override
    protected PermissionDialog getDialog() {
        return new TestPermissionDialog(this);
    }
}
