package com.bihe0832.android.common.settings.card;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bihe0832.android.common.about.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.lib.media.image.GlideExtKt;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.theme.ThemeResourcesManager;
import com.bihe0832.android.lib.ui.custom.view.background.TextViewWithBackground;
import com.bihe0832.android.lib.ui.custom.view.background.TextViewWithBackgroundExtKt;
import com.bihe0832.android.lib.utils.os.DisplayUtil;
import com.bumptech.glide.request.RequestOptions;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-11-21.
 *         Description: Description
 */
public class SettingsHolderGo extends CardBaseHolder {

    public TextView mHeader;
    public ImageView mHeaderIcon;
    public TextViewWithBackground mHeaderIsNew;
    public TextView mHeaderTips;
    public ImageView mHeadergo;
    public View settings_driver;

    public SettingsHolderGo(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void initView() {
        mHeader = getView(R.id.settings_title);
        mHeaderIcon = getView(R.id.settings_icon);
        mHeaderIsNew = getView(R.id.settings_is_new);
        mHeaderTips = getView(R.id.settings_tips);
        mHeadergo = getView(R.id.settings_go);
        settings_driver = getView(R.id.settings_driver);
    }

    @Override
    public void initData(CardBaseModule item) {
        SettingsDataGo data = (SettingsDataGo) item;
        itemView.setOnClickListener(data.mHeaderListener);

        if (data.mHeaderTipsListener != null) {
            mHeaderTips.setOnClickListener(data.mHeaderTipsListener);
        } else {
            mHeaderTips.setOnClickListener(data.mHeaderListener);
        }

        if (TextUtils.isEmpty(data.mItemIconURL)) {
            if (data.mItemIconRes < 0) {
                mHeaderIcon.setVisibility(View.GONE);
            } else {
                Drawable drawable = ThemeResourcesManager.INSTANCE.getDrawable(data.mItemIconRes);
                if (drawable != null) {
                    mHeaderIcon.setVisibility(View.VISIBLE);
                    GlideExtKt.loadImage(mHeaderIcon, drawable, 0, 0, new RequestOptions());
                    if (data.mItemIconResColorFilter == null) {
                        if (data.mAutoGenerateColorFilter) {
                            mHeaderIcon.setColorFilter(getContext().getResources().getColor(com.bihe0832.android.lib.aaf.res.R.color.textColorPrimary));
                        } else {
                            mHeaderIcon.setColorFilter(null);
                        }
                    } else {
                        mHeaderIcon.setColorFilter(data.mItemIconResColorFilter);
                    }
                } else {
                    mHeaderIcon.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            GlideExtKt.loadImage(mHeaderIcon, data.mItemIconURL, com.bihe0832.android.lib.aaf.res.R.mipmap.icon, com.bihe0832.android.lib.aaf.res.R.mipmap.icon);
            mHeaderIcon.setVisibility(View.VISIBLE);
        }

        mHeader.setText(TextFactoryUtils.getSpannedTextByHtml(data.mItemText));
        if (data.mHeaderTextBold) {
            mHeader.setTypeface(null, Typeface.BOLD);
        } else {
            mHeader.setTypeface(null, Typeface.NORMAL);
        }
        if (TextUtils.isEmpty(data.mTipsText)) {
            mHeaderTips.setVisibility(View.GONE);
        } else {
            CharSequence charSequence = TextFactoryUtils.getSpannedTextByHtml(data.mTipsText);//支持html
            mHeaderTips.setText(charSequence);
            mHeaderTips.setVisibility(View.VISIBLE);
        }

        if (data.mItemNewNum >= 0) {
            TextViewWithBackgroundExtKt.changeStatusWithUnreadMsg(mHeaderIsNew, data.mItemNewNum,
                    DisplayUtil.dip2px(getContext(), 8));
        } else {
            mHeaderIsNew.setVisibility(View.GONE);
        }

        if (data.mShowGo) {
            mHeadergo.setVisibility(View.VISIBLE);
        } else {
            mHeadergo.setVisibility(View.GONE);
        }
        if (data.mShowDriver) {
            settings_driver.setVisibility(View.VISIBLE);
        } else {
            settings_driver.setVisibility(View.GONE);
        }
    }
}
