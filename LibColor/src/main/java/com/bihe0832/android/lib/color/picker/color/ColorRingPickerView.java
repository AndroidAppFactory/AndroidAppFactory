package com.bihe0832.android.lib.color.picker.color;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import com.bihe0832.android.lib.color.picker.base.BaseColorPickerView;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

/**
 * 颜色选择器，用于实现色相环颜色选择效果
 */
public class ColorRingPickerView extends BaseColorPickerView {

    /**
     * 默认大小，使用dp作为单位
     */
    private static final int DEFAULT_WIDTH_DP = 300;
    /**
     * 默认大小，使用dp作为单位
     */
    private static final int DEFAULT_HEIGHT_DP = 300;
    /**
     * 控件高
     */
    private int mHeight;
    /**
     * 控件宽
     */
    private int mWidth;
    /**
     * 色块大小
     */
    private int colorBlockSize = 0;
    /**
     * 选中色相环ID
     */
    private int mCurrentColorArrId = -1;
    /**
     * 选中环中颜色ID
     */
    private int mCurrentColorId = -1;

    /**
     * 选中状态颜色
     */
    @ColorInt
    private int mSelectedBlockColor = Color.parseColor("#FFFFFFFF");

    /**
     * 色相环颜色值
     */
    private String[][] colorArray = {
            {
                    "#fef5ce", "#fff3cd", "#feeeca", "#fdeac9", "#fee7c7", "#fce3c4", "#fbddc1", "#fad7c3",
                    "#fad0c2", "#f2ced0", "#e6cad9", "#d9c7e1", "#d2c3e0", "#cfc6e3", "#cac7e4", "#c9cde8",
                    "#c7d6ed", "#c7dced", "#c7e3e6", "#d2e9d9", "#deedce", "#e7f1cf", "#eef4d0", "#f5f7d0"
            },
            {
                    "#ffeb95", "#fee591", "#fcdf8f", "#fcd68d", "#facd89", "#f9c385", "#f7b882", "#f5ab86",
                    "#f29a82", "#e599a3", "#ce93b3", "#b48cbe", "#a588be", "#9d8cc2", "#9491c6", "#919dcf",
                    "#89abd9", "#85bada", "#86c5ca", "#9fd2b1", "#bada99", "#cbe198", "#dde899", "#edf099"
            },
            {
                    "#fee250", "#fed84f", "#fbce4d", "#f9c04c", "#f7b24a", "#f6a347", "#f39444", "#f07c4d",
                    "#ec614e", "#d95f78", "#b95b90", "#96549e", "#7c509d", "#6e59a4", "#5c60aa", "#5572b6",
                    "#3886c8", "#1c99c7", "#0daab1", "#57ba8b", "#90c761", "#b0d35f", "#ccdd5b", "#e5e756"
            },
            {
                    "#FDD900", "#FCCC00", "#fabd00", "#f6ab00", "#f39801", "#f18101", "#ed6d00", "#e94520",
                    "#e60027", "#cf0456", "#a60b73", "#670775", "#541b86", "#3f2b8e", "#173993", "#0c50a3",
                    "#0168b7", "#0081ba", "#00959b", "#03a569", "#58b530", "#90c320", "#b8d201", "#dadf00"
            },
            {
                    "#DBBC01", "#DAB101", "#D9A501", "#D69400", "#D28300", "#CF7100", "#CD5F00", "#CA3C18",
                    "#C7001F", "#B4004A", "#900264", "#670775", "#4A1277", "#142E82", "#0A448E", "#005AA0",
                    "#0070A2", "#018287", "#02915B", "#4A9D27", "#7DAB17", "#9EB801", "#BCC200", "#DBBC01"
            },
            {
                    "#B49900", "#B39000", "#B18701", "#AD7901", "#AB6B01", "#AA5B00", "#A84A00", "#A62D10",
                    "#A50011", "#94003C", "#770050", "#540060", "#3B0263", "#2B1568", "#10226C", "#053577",
                    "#004A87", "#005D88", "#006C6F", "#00784A", "#38831E", "#648B0A", "#829601", "#999F01"
            },
            {
                    "#9F8700", "#9E7F00", "#9D7601", "#9A6900", "#995E00", "#975000", "#954000", "#932406",
                    "#92000B", "#840032", "#6A0048", "#4A0055", "#320057", "#240D5D", "#0C1860", "#032C6A",
                    "#014076", "#005278", "#016064", "#006B41", "#2E7316", "#567C03", "#718500", "#888D00"
            }
    };

    public ColorRingPickerView(Context context) {
        super(context);
    }

    public ColorRingPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorRingPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置色相环颜色
     *
     * @param colorArray 各色相环颜色数组，由内向外依次绘制，索引0绘制于最内侧色环
     */
    public void setColor(String[][] colorArray) {
        this.colorArray = colorArray;
        int radius = getBigCircleRadius();
        /*
         * 计算色块大小
         */
        colorBlockSize = radius / (colorArray.length + 1);
        postInvalidate();
    }

    /**
     * 设置选中状态时mask颜色
     */
    public void setSelectedBlockColor(@ColorInt int color) {
        if (mSelectedBlockColor == color) {
            return;
        }
        mSelectedBlockColor = color;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawColors(canvas);
        drawTouchBlock(canvas);
        drawZoom(canvas);
    }


    /**
     * 绘制色相环
     */
    private void drawColors(Canvas canvas) {
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Style.FILL);
        canvas.drawCircle(mCenterX, mCenterY, colorBlockSize, mPaint);
        for (int colorArrId = 0; colorArrId < colorArray.length; colorArrId++) {
            String[] colors = colorArray[colorArrId];
            /* 根据当前环颜色数计算角度步长 */
            int angleStep = 360 / colors.length;
            int radius = (colorArrId + 1) * colorBlockSize + colorBlockSize / 2;
            for (int colorId = 0; colorId < colors.length; colorId++) {
                int startAngle = -angleStep / 2 + colorId * angleStep;
                drawColorBlock(canvas, radius, Color.parseColor(colors[colorId]), startAngle,
                        angleStep);
            }

        }
    }

    /**
     * 绘制选中效果
     */
    private void drawTouchBlock(Canvas canvas) {
        if (mTouchX == 0 && mTouchY == 0) {
            return;
        }

        if (mIsRelease || mCurrentColor == -1) {
            return;
        }

        double distanceFromCenter = getDistanceFromCenter();
        if (distanceFromCenter >= getBigCircleRadius()) {
            return;
        }

        if (mCurrentColorArrId == 0) {
            mPaint.setStyle(Style.FILL);
            mPaint.setColor(mSelectedBlockColor);
            canvas.drawCircle(mCenterX, mCenterY, colorBlockSize, mPaint);
        } else {
            String[] colors = colorArray[mCurrentColorArrId - 1];
            /* 根据当前环颜色数计算角度步长 */
            int angleStep = 360 / colors.length;
            int radius = (mCurrentColorArrId + 1) * colorBlockSize
                    + colorBlockSize / 2;
            int startAngle = -angleStep / 2 + mCurrentColorId * angleStep;
            drawColorBlock(canvas, radius, mSelectedBlockColor, startAngle, angleStep);
        }
    }


    /**
     * 获取触点与圆心的距离
     */
    private double getDistanceFromCenter() {
        float factor = (mTouchX - mCenterX) * (mTouchX - mCenterX);
        factor += (mTouchY - mCenterY) * (mTouchY - mCenterY);
        return Math.sqrt(factor);
    }

    /**
     * 计算当前选中的色块位置
     */
    @Override
    protected void computeCurrent() {
        if (mTouchX == 0 && mTouchY == 0) {
            return;
        }

        double distanceFromCenter = getDistanceFromCenter();
        if (distanceFromCenter >= getBigCircleRadius()) {
            return;
        }

        // 计算色环ID
        mCurrentColorArrId = (int) (distanceFromCenter / colorBlockSize) - 1;
        if (mCurrentColorArrId >= colorArray.length) {
            mCurrentColorArrId = colorArray.length - 1;
        }

        // 计算当前选中的色块在色环中的位置
        if (mCurrentColorArrId >= 0) {
            String[] colors = colorArray[mCurrentColorArrId];
            /* 根据当前环颜色数计算角度步长 */
            int angleStep = 360 / colors.length;
            int angle = (int) (Math.atan2(mTouchY - mCenterY, mTouchX - mCenterX) * 180 / Math.PI);
            angle %= 360;
            if (angle < -angleStep / 2) {
                angle += 360;
            }

            if (angle > 360 - angleStep / 2) {
                angle -= 360;
            }
            mCurrentColorId = (int) Math.floor(angle / angleStep);
        }
        // 处理颜色监听
        if (mCurrentColorArrId >= 0) {
            String color = colorArray[mCurrentColorArrId][mCurrentColorId];
            int colorInInt = Color.parseColor(color);
            mCurrentColor = colorInInt;
            if (mListener == null) {
                return;
            }
            if (mIsRelease) {
                mListener.onColorSelected(colorInInt);
            } else {
                mListener.onColorSelecting(colorInInt);
            }
        } else {
            mCurrentColor = -1;
        }
    }

    /**
     * 绘制色块
     *
     * @param canvas
     * @param radius 半径
     * @param color 色块颜色
     * @param startAngle 开始角度
     * @param sweepAngle 覆盖角度
     */
    private void drawColorBlock(Canvas canvas, int radius, @ColorInt int color,
            int startAngle, int sweepAngle) {
        mPaint.setStrokeWidth(colorBlockSize);
        mPaint.setStyle(Style.STROKE);
        mPaint.setColor(color);
        RectF oval = new RectF(mCenterX - radius, mCenterY - radius, mCenterX
                + radius, mCenterY + radius);
        canvas.drawArc(oval, startAngle, sweepAngle, false, mPaint);
    }

    /**
     * 获取整个色环的半径，取宽和高中最小值的二分之一减去8像素
     *
     * @return 色环半径
     */
    private int getBigCircleRadius() {
        int radius = mWidth > mHeight ? mHeight / 2 : mWidth / 2;
        return radius - 8;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int defaultWidth = DisplayUtil.dip2px(getContext(), DEFAULT_WIDTH_DP);
        int defaultHeight = DisplayUtil.dip2px(getContext(), DEFAULT_HEIGHT_DP);
        if (widthMode == MeasureSpec.UNSPECIFIED
                || widthMode == MeasureSpec.AT_MOST) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(defaultWidth,
                    MeasureSpec.EXACTLY);
            mWidth = defaultWidth;
        } else {
            mWidth = widthSize;
        }

        if (heightMode == MeasureSpec.UNSPECIFIED
                || heightMode == MeasureSpec.AT_MOST) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(defaultHeight,
                    MeasureSpec.EXACTLY);
            mHeight = defaultHeight;
        } else {
            mHeight = heightSize;
        }

        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;
        int radius = getBigCircleRadius();
        /*
         * 计算色块大小
         */
        colorBlockSize = radius / (colorArray.length + 1);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}