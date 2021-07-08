package com.bihe0832.android.common.test.log;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.bihe0832.android.common.test.R;
import com.bihe0832.android.framework.log.LoggerFile;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author hardyshi code@bihe0832.com
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
        log_title.setText(data.mTitleName);

        log_open.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try { //设置intent的data和Type属性
                    LoggerFile.INSTANCE.openLog(data.mLogFileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        log_send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try { //设置intent的data和Type属性
                    LoggerFile.INSTANCE.sendLog(data.mLogFileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
