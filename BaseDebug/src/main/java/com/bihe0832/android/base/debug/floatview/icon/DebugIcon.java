package com.bihe0832.android.base.debug.floatview.icon;

import android.content.Context;
import android.view.WindowManager;
import com.bihe0832.android.lib.aaf.tools.AAFException;
import com.bihe0832.android.lib.floatview.IconViewWithRedDot;
import com.bihe0832.android.lib.theme.ThemeResourcesManager;

/**
 * @author zixie code@bihe0832.com Created on 8/26/21.
 */
public class DebugIcon extends IconViewWithRedDot {

    public DebugIcon(Context context) throws AAFException {
        super(context, ThemeResourcesManager.INSTANCE.getDrawable(com.bihe0832.android.lib.aaf.res.R.drawable.icon_qq));
    }

    @Override
    public int getIconLocationX() {
//        return 800;
        return super.getIconLocationX();
    }

    @Override
    public int getIconLocationY() {
//        return 1080;
        return super.getIconLocationY();
    }


    @Override
    protected void resetParams(WindowManager.LayoutParams mParams) {
//        mParams.x = getIconLocationX();
//        mParams.y = getIconLocationY();
    }
}
