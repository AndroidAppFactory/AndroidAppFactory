package com.bihe0832.android.base.debug.card.section;

import com.bihe0832.android.common.settings.card.SettingsDataSwitch;
import com.bihe0832.android.lib.adapter.CardBaseHolder;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
public class SettingsDataSwitchForDebug extends SettingsDataSwitch {


    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return SectionHolderContent3.class;
    }
}