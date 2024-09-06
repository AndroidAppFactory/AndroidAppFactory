package com.bihe0832.android.base.debug.card.audio;

import com.bihe0832.android.base.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-11-21.
 *         Description: Description
 */
public class AudioData extends CardBaseModule {

    public String filePath;
    public String recogniseResult;

    public String amplitude = "最大振幅：未知";

    public AudioData(String netType) {
        filePath = netType;
    }

    public int getResID() {
        return R.layout.card_audio_view;
    }

    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return AudioHolder.class;
    }
}