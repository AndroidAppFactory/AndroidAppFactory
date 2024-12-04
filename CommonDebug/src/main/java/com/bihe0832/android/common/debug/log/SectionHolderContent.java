package com.bihe0832.android.common.debug.log;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.bihe0832.android.common.debug.R;
import com.bihe0832.android.common.debug.log.core.DebugLogInfoActivity;
import com.bihe0832.android.framework.file.AAFFileTools;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.ui.view.ext.ViewExtKt;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */

public class SectionHolderContent extends CardBaseHolder {
    public TextView log_title;
    public TextView log_open;
    public TextView log_send;

    public SectionHolderContent(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void initView() {
        log_title = getView(R.id.log_title);
        log_open = getView(R.id.log_open);
        log_send = getView(R.id.log_send);
    }

    @Override
    public void initData(CardBaseModule item) {
        final SectionDataContent data = (SectionDataContent) item;
        log_title.setText(TextFactoryUtils.getSpannedTextByHtml(data.mTitleName));


        log_open.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.mActionListener != null) {
                    data.mActionListener.onClick(v, SectionDataContent.TYPE_OPEN);
                } else {
                    try {
                        DebugLogInfoActivity.Companion.showLog(getContext(),data.mLogFileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        log_send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.mActionListener != null) {
                    data.mActionListener.onClick(v, SectionDataContent.TYPE_SEND);
                } else {
                    try {
                        AAFFileTools.INSTANCE.sendFile(data.mLogFileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

    }

}
