package com.bihe0832.android.lib.ui.dialog.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bihe0832.android.lib.media.image.GlideExtKt;
import com.bihe0832.android.lib.request.URLUtils;
import com.bihe0832.android.lib.ui.dialog.CommonDialog;
import com.bihe0832.android.lib.ui.dialog.R;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2023/7/20.
 *         Description: Description
 */
public class ImageDialog extends CommonDialog {

    public static final int ORIENTATION_VERTICAL = 1;
    public static final int ORIENTATION_HORIZONTAL = 2;
    private int oritation = 1;
    private String url = "";
    private int res = -1;
    private int mButtonHeight = 0;
    private int mButtonLeftMargin = 0;
    private int mButtonBottomMargin = 0;

    private Bitmap bitmap = null;
    private ImageView mContentImageView = null;
    private View mDialogLayout = null;
    private View mButtonLayout = null;
    private boolean mShowButtonBg = false;

    public ImageDialog(Context context) {
        super(context);
    }

    protected int getLayoutID() {
        return R.layout.com_bihe0832_common_image_dialog;
    }

    protected void initView() {
        this.setShouldCanceled(true);
        this.mDialogLayout = this.findViewById(R.id.dialog_content_layout);
        if (this.mDialogLayout != null) {
            this.mDialogLayout.setOnClickListener((v) -> {
                if (null != getOnClickBottomListener()) {
                    getOnClickBottomListener().onNegativeClick();
                }
            });
        }

        this.mContentImageView = (ImageView) this.findViewById(R.id.dialog_image);
        this.mButtonLayout = this.findViewById(R.id.dialog_button);
        if (this.mButtonLayout != null) {
            this.mButtonLayout.setOnClickListener((v) -> {
                if (null != getOnClickBottomListener()) {
                    getOnClickBottomListener().onPositiveClick();
                }
            });
        }
        if (mButtonHeight == 0) {
            mButtonHeight = DisplayUtil.dip2px(getContext(), 100f);
        }
    }

    public ImageDialog setOritation(int oritation) {
        this.oritation = oritation;
        return this;
    }

    public ImageDialog setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    public ImageDialog setImageRes(int res) {
        this.res = res;
        return this;
    }

    public ImageDialog setImageUrl(String url) {
        this.url = url;
        return this;
    }

    public ImageDialog setShowButton(boolean showButton) {
        this.mShowButtonBg = showButton;
        return this;
    }

    public ImageDialog setButtonHeightAndMargin(int heightDP, int leftMargin, int bottomMargin) {
        this.mButtonHeight = DisplayUtil.dip2px(getContext(), heightDP);
        this.mButtonLeftMargin = DisplayUtil.dip2px(getContext(), leftMargin);
        this.mButtonBottomMargin = DisplayUtil.dip2px(getContext(), bottomMargin);
        return this;
    }

    protected void refreshView() {
        if (null != this.mDialogLayout) {
            if (this.oritation == 1) {
                this.mDialogLayout.setPadding(0, 0, 0, DisplayUtil.dip2px(this.getContext(), 128.0F));
            } else {
                this.mDialogLayout.setPadding(0, 0, 0, 0);
            }
        }

        if (null != this.mButtonLayout) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) this.mButtonLayout.getLayoutParams();
            params.height = mButtonHeight;
            params.leftMargin = mButtonLeftMargin;
            params.rightMargin = mButtonLeftMargin;
            params.bottomMargin = mButtonBottomMargin;
            this.mButtonLayout.setLayoutParams(params);
            if (mShowButtonBg) {
                int colorAccent = getContext().getResources().getColor(com.bihe0832.android.lib.aaf.res.R.color.colorAccent);
                int colorWithAlpha = Color.argb(128, Color.red(colorAccent), Color.green(colorAccent),
                        Color.blue(colorAccent));
                this.mButtonLayout.setBackgroundColor(colorWithAlpha);

            } else {
                this.mButtonLayout.setBackgroundResource(com.bihe0832.android.lib.aaf.res.R.color.transparent);
            }
        }

        if (null != this.mContentImageView) {
            if (URLUtils.isHTTPUrl(this.url)) {
                GlideExtKt.loadCenterInsideImage(this.mContentImageView, this.url, com.bihe0832.android.lib.aaf.res.R.color.transparent,
                        com.bihe0832.android.lib.aaf.res.R.color.transparent);
            } else if (this.res > 0) {
                this.mContentImageView.setImageResource(this.res);
            } else {
                this.mContentImageView.setImageBitmap(this.bitmap);
            }
        }

        super.refreshView();
    }
}
