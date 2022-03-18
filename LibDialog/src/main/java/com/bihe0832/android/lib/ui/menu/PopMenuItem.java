package com.bihe0832.android.lib.ui.menu;

import android.graphics.Bitmap;
import android.view.View;

public class PopMenuItem {

    private String actionName = "";
    private Bitmap icon = null;
    private int iconResId = -1;
    private View.OnClickListener actionClickListener = null;

    public PopMenuItem(){

    }

    public PopMenuItem(String actionName, View.OnClickListener actionClickListener) {
        this.actionName = actionName;
        this.actionClickListener = actionClickListener;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap mIcon) {
        this.icon = mIcon;
    }


    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public View.OnClickListener getActionClickListener() {
        return actionClickListener;
    }

    public void setItemClickListener(View.OnClickListener actionClickListener) {
        this.actionClickListener = actionClickListener;
    }


}
