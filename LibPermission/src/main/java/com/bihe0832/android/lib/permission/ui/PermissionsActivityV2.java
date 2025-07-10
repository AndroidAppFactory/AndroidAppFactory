package com.bihe0832.android.lib.permission.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bihe0832.android.lib.media.image.GlideExtKt;
import com.bihe0832.android.lib.permission.PermissionManager;
import com.bihe0832.android.lib.permission.R;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import java.util.ArrayList;
import java.util.List;


public class PermissionsActivityV2 extends PermissionsActivity {

    public Boolean getUseDefault() {
        return true;
    }

    public Boolean getNeedSpecial() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_bihe0832_lib_permissions_activity_v2);

        showTips();
    }

    protected void showTips() {
        findViewById(R.id.permission_layout).setVisibility(View.VISIBLE);
        GlideExtKt.loadImage((ImageView) findViewById(R.id.permission_icon),
                PermissionManager.INSTANCE.getPermissionIcon(scene, ""));
        List<String> tempPermissionList = new ArrayList<>();
        for (String permission : needCheckPermissionGroup) {
            tempPermissionList.add(permission);
        }

        String permissionDesc = PermissionManager.INSTANCE.getPermissionDesc(scene, tempPermissionList, true,
                getNeedSpecial());
        String permissionScene = PermissionManager.INSTANCE.getPermissionScene(scene, tempPermissionList, false,
                getNeedSpecial());
        String permissionContent = PermissionManager.INSTANCE.getPermissionContent(this, scene, tempPermissionList,
                false, getNeedSpecial());

        if (!TextUtils.isEmpty(permissionDesc)) {
            ((TextView) findViewById(R.id.permission_title)).setText(TextFactoryUtils.getSpannedTextByHtml(
                    permissionDesc + " " + getString(R.string.com_bihe0832_permission_tips)));
            if (!TextUtils.isEmpty(permissionScene)) {
                ((TextView) findViewById(R.id.permission_desc)).setText(
                        TextFactoryUtils.getSpannedTextByHtml(permissionScene));
            } else if (!TextUtils.isEmpty(permissionContent)) {
                ((TextView) findViewById(R.id.permission_desc)).setText(
                        TextFactoryUtils.getSpannedTextByHtml(permissionContent));
            } else {
                if (getUseDefault()) {
                    String defaultPermissionScene = PermissionManager.INSTANCE.getPermissionScene(scene,
                            tempPermissionList, true, getNeedSpecial());
                    String defaultPermissionContent = PermissionManager.INSTANCE.getPermissionContent(this, scene,
                            tempPermissionList, true, getNeedSpecial());
                    if (!TextUtils.isEmpty(defaultPermissionScene)) {
                        ((TextView) findViewById(R.id.permission_desc)).setText(
                                TextFactoryUtils.getSpannedTextByHtml(defaultPermissionScene));
                    } else if (!TextUtils.isEmpty(defaultPermissionContent)) {
                        ((TextView) findViewById(R.id.permission_desc)).setText(
                                TextFactoryUtils.getSpannedTextByHtml(defaultPermissionContent));
                    } else {
                        hideTips();
                    }
                } else {
                    hideTips();
                }
            }
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
    protected PermissionDialog getDialog(List<String> tempPermissionGroupList) {
        if (isAutoDeny()) {
            PermissionDialog dialog = super.getDialog(tempPermissionGroupList);
            dialog.setUseDefault(getUseDefault());
            dialog.setNeedSpecial(getNeedSpecial());
            return dialog;
        } else {
            return null;
        }
    }

    @Override
    protected void showMissingPermissionDialog(List<String> tempPermissionGroupList) {
        hideTips();
        super.showMissingPermissionDialog(tempPermissionGroupList);
    }
}
