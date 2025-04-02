package com.bihe0832.android.common.debug.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.bihe0832.android.common.debug.R;
import com.bihe0832.android.framework.file.AAFFileTools;
import com.bihe0832.android.framework.router.RouterHelperWrapperKt;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.lib.text.TextFactoryUtils;

/**
 * @author zixie code@bihe0832.com Created on 2019-11-21. Description: Description
 */

public class SectionHolderContent extends CardBaseHolder {

    public TextView log_title;
    public TextView log_open;
    public TextView log_send;
    public View log_layout;

    public SectionHolderContent(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void initView() {
        log_layout = getView(R.id.log_layout);
        log_title = getView(R.id.log_title);
        log_open = getView(R.id.log_open);
        log_send = getView(R.id.log_send);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void initData(CardBaseModule item) {
        final SectionDataContent data = (SectionDataContent) item;
        log_title.setText(TextFactoryUtils.getSpannedTextByHtml(data.mTitleName));
        View.OnClickListener openListener = v -> {
            if (data.mActionListener != null) {
                data.mActionListener.onClick(v, SectionDataContent.TYPE_OPEN);
            } else {
                try {
                    RouterHelperWrapperKt.showFileContent(data.mLogFileName, data.mSort, data.mShowLine);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        if (data.mShowAction) {
            log_open.setOnClickListener(openListener);
            log_open.setVisibility(View.VISIBLE);
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
            log_send.setVisibility(View.VISIBLE);
            log_layout.setBackground(null);
            log_layout.setOnClickListener(null);
        } else {
            log_send.setVisibility(View.GONE);
            log_open.setVisibility(View.GONE);
            log_layout.setOnClickListener(openListener);
            log_layout.setBackground(getContext().getResources().getDrawable(R.drawable.common_button_bg_shape));
        }
    }

}
