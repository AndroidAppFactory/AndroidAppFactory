package com.bihe0832.android.base.test.icon;

import android.content.Context;
import android.view.WindowManager;
import com.bihe0832.android.base.test.R;
import com.bihe0832.android.lib.floatview.IconView;

/**
 * @author hardyshi code@bihe0832.com Created on 8/26/21.
 */
public class TestIcon extends IconView {

    public TestIcon(Context context) {
        super(context, context.getResources().getDrawable(R.mipmap.share_qq));
    }

    @Override
    public int getIconLocationX() {
        return 800;
    }

    @Override
    public int getIconLocationY() {
        return 1080;
    }


    @Override
    protected void resetParams(WindowManager.LayoutParams mParams) {
//        mParams.x = getIconLocationX();
//        mParams.y = getIconLocationY();
    }
}
