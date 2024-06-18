package com.bihe0832.android.lib.floatview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.bihe0832.android.lib.aaf.tools.AAFException;
import com.bihe0832.android.lib.media.image.bitmap.BitmapUtil;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

public class IconViewWithRedDot extends IconView {

    public ImageView mView;//小图标正常态
    private ImageView mRedViewL;//Icon上左边的红点，用于Icon在屏幕右侧时展示
    private ImageView mRedViewR;//Icon上右边的红点，用于Icon在屏幕左侧时展示
    private View layout;//小图标外层布局
    private boolean mHasRed = false;
    private Bitmap mIconCache;


    @Override
    public ImageView getIconView() {
        return mView;
    }

    @Override
    public View getIconLayout() {
        return layout;
    }

    @Override
    public int getLayoutId() {
        return R.layout.com_bihe0832_icon_view;
    }

    @Override
    public void initView() {
        layout = findViewById(R.id.com_bihe0832_lib_icon_layout);
        mView = (ImageView) findViewById(R.id.com_bihe0832_lib_icon_icon);
        mRedViewL = (ImageView) findViewById(R.id.com_bihe0832_lib_icon_icon_redl);
        mRedViewR = (ImageView) findViewById(R.id.com_bihe0832_lib_icon_icon_redr);
    }

    public IconViewWithRedDot(Context context) throws AAFException {
        super(context);

    }

    public IconViewWithRedDot(Context context, Drawable drawable) throws AAFException {
        this(context);
        mView.setImageDrawable(drawable);
    }

    public void setHasNew(boolean isNew) {
        mHasRed = isNew;
    }


    public void setIconImage(final String iconURL) {
        if (TextUtils.isEmpty(iconURL)) {
            Log.e(TAG, "Icon URL is Empty can`t update");
            return;
        }
        if (null != mIconCache) {
            mView.setImageBitmap(mIconCache);
            return;
        } else {
            if (!TextUtils.isEmpty(iconURL)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.e(TAG, "Icon URL is ：" + iconURL);
                            Bitmap bitmap = BitmapUtil.getRemoteBitmap(iconURL, DisplayUtil.dip2px(getContext(), 40),
                                    DisplayUtil.dip2px(getContext(), 40));
                            if (bitmap != null) {
                                mIconCache = bitmap;
                                mView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mView.setImageBitmap(mIconCache);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    public void updateReddot() {

        boolean iconLocationIsLeft = iconLocationIsLeft();
        Log.d(TAG, "Icon updateViewRed point " + mHasRed);
        if (mHasRed) {
            if (iconLocationIsLeft) {
                mRedViewL.setVisibility(INVISIBLE);
                mRedViewR.setVisibility(VISIBLE);
            } else {
                mRedViewL.setVisibility(VISIBLE);
                mRedViewR.setVisibility(INVISIBLE);
            }
        } else {
            mRedViewR.setVisibility(INVISIBLE);
            mRedViewL.setVisibility(INVISIBLE);
        }
    }

    @Override
    public void onClick(final View v) {
        //先把Icon的红点置为0，然后开启大悬浮
        mHasRed = false;
        updateReddot();
        super.onClick(v);

    }

    @Override
    public void showIcon() {
        super.showIcon();
        updateReddot();
    }

    /**
     * 更新悬浮窗的位置
     */
    protected void updateViewPosition() {
        super.updateViewPosition();
        updateReddot();
    }
}