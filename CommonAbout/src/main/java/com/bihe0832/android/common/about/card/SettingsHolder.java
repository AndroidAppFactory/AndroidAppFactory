package com.bihe0832.android.common.about.card;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bihe0832.android.common.about.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.ui.image.GlideExtKt;
import com.bihe0832.android.lib.ui.textview.TextViewWithBackground;
import com.bihe0832.android.lib.ui.textview.ext.TextViewExtKt;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
public class SettingsHolder extends CardBaseHolder {

    public TextView mHeader;
    public ImageView mHeaderIcon;
    public TextViewWithBackground mHeaderIsNew;
    public TextView mHeaderTips;
    public ImageView mHeadergo;
    public View settings_drivider;

    public SettingsHolder(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void initView() {
        mHeader = getView(R.id.settings_title);
        mHeaderIcon = getView(R.id.settings_icon);
        mHeaderIsNew = getView(R.id.settings_is_new);
        mHeaderTips = getView(R.id.settings_tips);
        mHeadergo = getView(R.id.settings_go);
        settings_drivider = getView(R.id.settings_drivider);
    }

    @Override
    public void initData(CardBaseModule item) {
        SettingsData data = (SettingsData) item;
        if (data.mHeaderListener != null) {
            itemView.setOnClickListener(data.mHeaderListener);
        }

        if (data.mHeaderTipsListener != null) {
            mHeaderTips.setOnClickListener(data.mHeaderTipsListener);
        } else {
            mHeaderTips.setOnClickListener(data.mHeaderListener);
        }
        if (TextUtils.isEmpty(data.mItemIconURL)) {
            if (data.mItemIconRes < 0) {
                mHeaderIcon.setVisibility(View.GONE);
            } else {
                GlideExtKt.loadImage(mHeaderIcon, data.mItemIconRes);
                mHeaderIcon.setVisibility(View.VISIBLE);
            }
        } else {
            GlideExtKt.loadImage(mHeaderIcon, data.mItemIconURL, R.mipmap.icon, R.mipmap.icon);
            mHeaderIcon.setVisibility(View.VISIBLE);
        }

        mHeader.setText(data.mItemText);
        if (data.mHeaderTextBold) {
            mHeader.setTypeface(null, Typeface.BOLD);
        }
        if (TextUtils.isEmpty(data.mTipsText)) {
            mHeaderTips.setVisibility(View.GONE);
        } else {
            CharSequence charSequence = TextFactoryUtils.getSpannedTextByHtml(data.mTipsText);//支持html
            mHeaderTips.setText(charSequence);
            mHeaderTips.setVisibility(View.VISIBLE);
        }

        if (data.mItemIsNew) {
            TextViewExtKt.showUnreadMsg(mHeaderIsNew, 0, DisplayUtil.dip2px(getContext(), 8));
        } else {
            mHeaderIsNew.setVisibility(View.GONE);
        }

        if (data.mShowGo) {
            mHeadergo.setVisibility(View.VISIBLE);
        } else {
            mHeadergo.setVisibility(View.GONE);
        }
        if (data.mShowDriver) {
            settings_drivider.setVisibility(View.VISIBLE);
        } else {
            settings_drivider.setVisibility(View.GONE);
        }
    }
}
