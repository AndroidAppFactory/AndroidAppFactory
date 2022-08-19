package com.bihe0832.android.lib.permission.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bihe0832.android.lib.permission.PermissionManager;
import com.bihe0832.android.lib.permission.R;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.ui.image.GlideExtKt;

import java.util.ArrayList;
import java.util.List;


public class PermissionsActivityV2 extends PermissionsActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_bihe0832_lib_permissions_activity_v2);
        showTips();
    }

    protected void showTips() {
        findViewById(R.id.permission_layout).setVisibility(View.VISIBLE);
        GlideExtKt.loadImage((ImageView) findViewById(R.id.permission_icon), PermissionManager.INSTANCE.getPermissionIcon(scene, ""));
        List<String> tempPermissionList = new ArrayList<>();
        for (String permission : needCheckPermissionGroup) {
            tempPermissionList.add(permission);
        }

        String permissionDesc = PermissionManager.INSTANCE.getPermissionDesc(scene, tempPermissionList, false);
        String permissionScene = PermissionManager.INSTANCE.getPermissionScene(scene, tempPermissionList, false);

        if (!TextUtils.isEmpty(permissionDesc) && !TextUtils.isEmpty(permissionScene)) {
            ((TextView) findViewById(R.id.permission_title)).setText(TextFactoryUtils.getSpannedTextByHtml(permissionDesc + "权限使用说明"));
            ((TextView) findViewById(R.id.permission_desc)).setText(TextFactoryUtils.getSpannedTextByHtml(permissionScene));
        } else {
            hideTips();
        }

    }

    protected void hideTips() {
        try {
            findViewById(R.id.permission_layout).setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected PermissionDialog getDialog(List<String> tempPermissionList) {
        if (autoDeny) {
            return super.getDialog(tempPermissionList);
        } else {
            return null;
        }
    }

    @Override
    protected void showMissingPermissionDialog(List<String> tempPermissionList) {
        hideTips();
        super.showMissingPermissionDialog(tempPermissionList);
    }
}
