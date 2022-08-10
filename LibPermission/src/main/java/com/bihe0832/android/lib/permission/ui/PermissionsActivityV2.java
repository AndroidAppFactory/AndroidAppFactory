package com.bihe0832.android.lib.permission.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
    protected void onCreate(@Nullable @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_bihe0832_lib_permissions_activity_v2);
        showTips();
    }

    protected void showTips() {
        GlideExtKt.loadImage((ImageView) findViewById(R.id.permission_icon), PermissionManager.INSTANCE.getPermissionIcon(scene, ""));
        List<String> tempPermissionList = new ArrayList<>();
        for (String permission : needCheckPermission) {
            tempPermissionList.add(permission);
        }
        ((TextView) findViewById(R.id.permission_title)).setText(TextFactoryUtils.getSpannedTextByHtml(PermissionManager.INSTANCE.getPermissionDesc(scene, tempPermissionList, false) + "权限使用说明"));
        ((TextView) findViewById(R.id.permission_desc)).setText(TextFactoryUtils.getSpannedTextByHtml(PermissionManager.INSTANCE.getPermissionScene(scene, tempPermissionList, false)));
    }


    protected void hideTips() {
        try {
            findViewById(R.id.permission_layout).setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void showMissingPermissionDialog(String showPermission) {
        super.showMissingPermissionDialog(showPermission);
        hideTips();
    }

    @Override
    protected void showMissingPermissionDialog(List<String> tempPermissionList) {
        super.showMissingPermissionDialog(tempPermissionList);
        hideTips();
    }
}
