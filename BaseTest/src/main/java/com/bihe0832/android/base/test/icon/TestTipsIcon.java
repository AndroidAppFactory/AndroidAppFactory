package com.bihe0832.android.base.test.icon;

import android.content.Context;
import com.bihe0832.android.lib.debug.icon.DebugLogTipsIcon;
import com.bihe0832.android.lib.utils.os.DisplayUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author hardyshi code@bihe0832.com Created on 8/26/21.
 */
public class TestTipsIcon extends DebugLogTipsIcon {


    public TestTipsIcon(@NotNull Context context) {
        super(context);
    }

    @Override
    public int getLocationX() {
        return DisplayUtil.getRealScreenSizeX(this.getContext()) - DisplayUtil.dip2px(this.getContext(), 300);
    }

    @Override
    public int getDefaultY() {
        return 0;
    }
}
