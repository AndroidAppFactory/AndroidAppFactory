package com.bihe0832.android.base.debug.card.section;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import com.bihe0832.android.base.debug.R;
import com.bihe0832.android.common.settings.card.SettingsHolderSwitch;
import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.lib.ui.view.ext.DrawableFactoryKt;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-11-21.
 *         Description: Description
 */

public class SettingsHolderSwitchForDebug extends SettingsHolderSwitch {

    public SettingsHolderSwitchForDebug(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void initData(CardBaseModule item) {
        super.initData(item);

        View content = getView(R.id.setting_content_layout);
        Drawable drawable = DrawableFactoryKt.getDrawable(Color.GREEN,12.0f,4,Color.YELLOW);
        content.setBackground(drawable);
        ViewGroup.MarginLayoutParams params = (MarginLayoutParams) content.getLayoutParams();
        params.setMargins(16,24,16,24);
        content.setLayoutParams(params);

    }
}
