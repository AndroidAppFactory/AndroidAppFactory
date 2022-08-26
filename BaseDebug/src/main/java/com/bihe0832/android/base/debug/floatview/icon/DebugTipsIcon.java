package com.bihe0832.android.base.debug.floatview.icon;

import android.content.Context;
import com.bihe0832.android.lib.debug.icon.DebugLogTipsIcon;

import org.jetbrains.annotations.NotNull;

/**
 * @author zixie code@bihe0832.com Created on 8/26/21.
 */
public class DebugTipsIcon extends DebugLogTipsIcon {


    public DebugTipsIcon(@NotNull Context context) {
        super(context);
    }

    @Override
    public int getLocationX() {
        return 0;
//        return DisplayUtil.getRealScreenSizeX(this.getContext()) - DisplayUtil.dip2px(this.getContext(), 300);
    }

    @Override
    public int getDefaultY() {
        return 0;
    }
}
