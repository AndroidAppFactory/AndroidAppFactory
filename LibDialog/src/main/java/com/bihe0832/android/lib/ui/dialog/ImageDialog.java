package com.bihe0832.android.lib.ui.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.bihe0832.android.lib.media.image.GlideExtKt;
import com.bihe0832.android.lib.request.URLUtils;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

/**
 * @author zixie code@bihe0832.com
 * Created on 2023/7/20.
 * Description: Description
 */
public class ImageDialog extends CommonDialog {

    public static final int ORIENTATION_VERTICAL = 1;
    public static final int ORIENTATION_HORIZONTAL = 2;

    private int oritation = ORIENTATION_VERTICAL;
    private String url = "";
    private int res = -1;
    private Bitmap bitmap = null;
    private ImageView mContentImageView = null;

    private View mDialogLayout = null;

    @Override
    protected int getLayoutID() {
        return R.layout.com_bihe0832_common_image_dialog;
    }

    public ImageDialog(Context context) {
        super(context);
    }

    @Override
    protected void initView() {
        setShouldCanceled(true);
        mDialogLayout = findViewById(R.id.dialog_content_layout);
        if (mDialogLayout != null) {
            mDialogLayout.setOnClickListener(v -> dismiss());
        }
        mContentImageView = findViewById(R.id.dialog_image);
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

    @Override
    protected void refreshView() {
        int dp32 = DisplayUtil.dip2px(getContext(), 32f);
        if (null != mDialogLayout) {
            if (oritation == ORIENTATION_VERTICAL) {
                mDialogLayout.setPadding(dp32, dp32, dp32, DisplayUtil.dip2px(getContext(), 128f));
            } else {
                mDialogLayout.setPadding(dp32, dp32, dp32, dp32);
            }
        }

        if (null != mContentImageView) {
            if (URLUtils.isHTTPUrl(url)) {
                GlideExtKt.loadCenterInsideImage(mContentImageView, url, R.color.transparent, R.color.transparent);
            } else if (res > 0) {
                mContentImageView.setImageResource(res);
            } else {
                mContentImageView.setImageBitmap(bitmap);
            }
        }
        super.refreshView();
    }
}
