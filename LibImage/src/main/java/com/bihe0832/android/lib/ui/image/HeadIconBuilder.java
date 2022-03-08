package com.bihe0832.android.lib.ui.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class HeadIconBuilder {

    private Context mContext;

    //图片地址链接
    private List<Object> mImageUrls = new ArrayList<>();
    //默认的图片ID
    private int mDefaultImageResId = R.mipmap.default_head_icon;

    private Map<Integer, Bitmap> mBitmapMap = new HashMap<>();

    private int mBGColor = Color.TRANSPARENT;
    // 小图分辨率
    private int mMaxWidth;
    // 小图分辨率
    private boolean mNeedCenterCrop = true;
    //行数
    private int mRowCount;
    //列数
    private int mColumnCount;
    //宫格间距
    private int mGap = 6;

    public HeadIconBuilder(Context mContext) {
        this.mContext = mContext;
    }

    public void setImageUrls(List<Object> imageUrls) {
        this.mImageUrls = imageUrls;
    }

    public int getDefaultImage() {
        return mDefaultImageResId;
    }

    public void setDefaultImage(int defaultImageResId) {
        this.mDefaultImageResId = defaultImageResId;
    }

    public void setNeedCenterCrop(boolean needCenterCrop) {
        this.mNeedCenterCrop = needCenterCrop;
    }

    public void setBgColor(int bgColor) {
        this.mBGColor = bgColor;
    }

    public void setItemWidth(int maxWidth) {
        this.mMaxWidth = maxWidth;
    }

    public void setGap(int mGap) {
        this.mGap = mGap;
    }


    private void calculateGridParam() {
        int imagesSize = getImageListSize();
        if (imagesSize < 3) {
            mRowCount = 1;
            mColumnCount = imagesSize;
        } else if (imagesSize <= 4) {
            mRowCount = 2;
            mColumnCount = 2;
        } else {
            mRowCount = imagesSize / 3 + (imagesSize % 3 == 0 ? 0 : 1);
            mColumnCount = 3;
        }
    }

    private void putBitmap(Bitmap bitmap, int position) {
        if (null == mBitmapMap) {
            mBitmapMap = new HashMap<>();
        }
        synchronized (mBitmapMap) {
            mBitmapMap.put(position, bitmap);
        }
    }

    private int getImageListSize() {
        if (mImageUrls.size() > 9) {
            return 9;
        } else {
            return mImageUrls.size();
        }
    }

    /**
     * 同步加载图片
     *
     * @param imageUrl
     * @param targetImageItemWidth
     * @return
     */
    private Bitmap loadImageBitmap(Object imageUrl, int targetImageItemWidth)
            throws ExecutionException, InterruptedException {
        RequestOptions options = new RequestOptions().error(getDefaultImage());
        if (mNeedCenterCrop) {
            options.centerCrop();
        }
        return Glide.with(mContext).asBitmap()
                .load(imageUrl)
                .apply(options)
                .submit(targetImageItemWidth, targetImageItemWidth)
                .get();
    }


    private void loadImageListData(int targetImageItemWidth) {
        Bitmap defaultIcon = BitmapFactory.decodeResource(mContext.getResources(), getDefaultImage());
        for (int i = 0; i < getImageListSize(); i++) {
            //下载图片
            try {
                Bitmap bitmap = loadImageBitmap(mImageUrls.get(i), targetImageItemWidth);
                putBitmap(bitmap, i);
            } catch (InterruptedException e) {
                e.printStackTrace();
                putBitmap(defaultIcon, i);
            } catch (ExecutionException e) {
                e.printStackTrace();
                putBitmap(defaultIcon, i);
            }
        }
    }

    private void drawDrawable(Canvas canvas, int targetImageItemWidth) {

        //画背景
        canvas.drawColor(mBGColor);
        //画组合图片
        int size = getImageListSize();
        int t_center = (mMaxWidth + mGap) / 2;//中间位置以下的顶点（有宫格间距）
        int b_center = (mMaxWidth - mGap) / 2;//中间位置以上的底部（有宫格间距）
        int l_center = (mMaxWidth + mGap) / 2;//中间位置以右的左部（有宫格间距）
        int r_center = (mMaxWidth - mGap) / 2;//中间位置以左的右部（有宫格间距）
        int center = (mMaxWidth - targetImageItemWidth) / 2;//中间位置以上顶部（无宫格间距）
        for (int i = 0; i < size; i++) {
            int rowNum = i / mRowCount;//当前行数
            int columnNum = i % mColumnCount;//当前列数

            int left = ((int) (targetImageItemWidth * (mColumnCount == 1 ? columnNum + 0.5 : columnNum) + mGap * (
                    columnNum + 1)));
            int top = ((int) (targetImageItemWidth * (mColumnCount == 1 ? rowNum + 0.5 : rowNum) + mGap * (rowNum + 1)));
            int right = left + targetImageItemWidth;
            int bottom = top + targetImageItemWidth;

            Bitmap bitmap = mBitmapMap.get(i);
            if (size == 1) {
                drawBitmapAtPosition(canvas, 0, 0, targetImageItemWidth, targetImageItemWidth, bitmap);
            } else if (size == 2) {
                drawBitmapAtPosition(canvas, left, center, right, center + targetImageItemWidth, bitmap);
            } else if (size == 3) {
                if (i == 0) {
                    drawBitmapAtPosition(canvas, center, top, center + targetImageItemWidth, bottom, bitmap);
                } else {
                    drawBitmapAtPosition(canvas, mGap * i + targetImageItemWidth * (i - 1), t_center,
                            mGap * i + targetImageItemWidth * i, t_center + targetImageItemWidth,
                            bitmap);
                }
            } else if (size == 4) {
                drawBitmapAtPosition(canvas, left, top, right, bottom, bitmap);
            } else if (size == 5) {
                if (i == 0) {
                    drawBitmapAtPosition(canvas, r_center - targetImageItemWidth,
                            r_center - targetImageItemWidth, r_center, r_center, bitmap);
                } else if (i == 1) {
                    drawBitmapAtPosition(canvas, l_center, r_center - targetImageItemWidth,
                            l_center + targetImageItemWidth, r_center, bitmap);
                } else {
                    drawBitmapAtPosition(canvas, mGap * (i - 1) + targetImageItemWidth * (i - 2),
                            t_center, mGap * (i - 1) + targetImageItemWidth * (i - 1), t_center +
                                    targetImageItemWidth, bitmap);
                }
            } else if (size == 6) {
                if (i < 3) {
                    drawBitmapAtPosition(canvas, mGap * (i + 1) + targetImageItemWidth * i,
                            b_center - targetImageItemWidth,
                            mGap * (i + 1) + targetImageItemWidth * (i + 1), b_center, bitmap);
                } else {
                    drawBitmapAtPosition(canvas, mGap * (i - 2) + targetImageItemWidth * (i - 3),
                            t_center, mGap * (i - 2) + targetImageItemWidth * (i - 2), t_center +
                                    targetImageItemWidth, bitmap);
                }
            } else if (size == 7) {
                if (i == 0) {
                    drawBitmapAtPosition(canvas, center, mGap, center + targetImageItemWidth,
                            mGap + targetImageItemWidth, bitmap);
                } else if (i > 0 && i < 4) {
                    drawBitmapAtPosition(canvas, mGap * i + targetImageItemWidth * (i - 1), center,
                            mGap * i + targetImageItemWidth * i, center + targetImageItemWidth,
                            bitmap);
                } else {
                    drawBitmapAtPosition(canvas, mGap * (i - 3) + targetImageItemWidth * (i - 4),
                            t_center + targetImageItemWidth / 2,
                            mGap * (i - 3) + targetImageItemWidth * (i - 3),
                            t_center + targetImageItemWidth / 2 + targetImageItemWidth, bitmap);
                }
            } else if (size == 8) {
                if (i == 0) {
                    drawBitmapAtPosition(canvas, r_center - targetImageItemWidth, mGap, r_center,
                            mGap + targetImageItemWidth, bitmap);
                } else if (i == 1) {
                    drawBitmapAtPosition(canvas, l_center, mGap, l_center + targetImageItemWidth,
                            mGap + targetImageItemWidth, bitmap);
                } else if (i > 1 && i < 5) {
                    drawBitmapAtPosition(canvas, mGap * (i - 1) + targetImageItemWidth * (i - 2), center,
                            mGap * (i - 1) + targetImageItemWidth * (i - 1),
                            center + targetImageItemWidth, bitmap);
                } else {
                    drawBitmapAtPosition(canvas, mGap * (i - 4) + targetImageItemWidth * (i - 5),
                            t_center + targetImageItemWidth / 2,
                            mGap * (i - 4) + targetImageItemWidth * (i - 4),
                            t_center + targetImageItemWidth / 2 + targetImageItemWidth, bitmap);
                }
            } else if (size == 9) {
                drawBitmapAtPosition(canvas, left, top, right, bottom, bitmap);
            }
        }
    }

    /**
     * 根据坐标画图
     */
    private void drawBitmapAtPosition(Canvas canvas, int left, int top, int right, int bottom, Bitmap bitmap) {
        if (null == bitmap) {
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), getDefaultImage());
        }
        if (null != bitmap) {
            Rect rect = new Rect(left, top, right, bottom);
            canvas.drawBitmap(bitmap, null, rect, null);
        }
    }

    public Bitmap generateBitmap() {
        calculateGridParam();
        int targetImageItemWidth = mMaxWidth;
        //单个图标的大小
        if (getImageListSize() > 1) {
            targetImageItemWidth = (mMaxWidth - (mColumnCount + 1) * mGap) / (mColumnCount == 1 ? 2 : mColumnCount);
        }
        loadImageListData(targetImageItemWidth);
        Bitmap mergeBitmap = Bitmap.createBitmap(mMaxWidth, mMaxWidth, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mergeBitmap);
        drawDrawable(canvas, targetImageItemWidth);
        canvas.save();
        canvas.restore();
        return mergeBitmap;
    }

    public interface GenerateBitmapCallback {

        void onResult(Bitmap bitmap, String filePath);
    }

    public void generateBitmap(final GenerateBitmapCallback call) {
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                final Bitmap result = generateBitmap();
                final String filePath = BitmapUtil.saveBitmap(mContext, result);
                ThreadManager.getInstance().runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        call.onResult(result, filePath);
                    }
                });
            }
        });
    }
}
