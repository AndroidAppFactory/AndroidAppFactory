package com.bihe0832.android.lib.panel.bean;

import android.graphics.Paint;
import android.graphics.Path;
import androidx.annotation.NonNull;

/**
 * 画笔路径
 */
public class DrawPenPoint implements Cloneable {

    /**
     * 绘画路径
     */
    private Path mPath;

    /**
     * 画笔
     */
    private Paint mPaint;

    public Path getPath() {
        return mPath;
    }

    public void setPath(Path path) {
        this.mPath = path;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint paint) {
        this.mPaint = paint;
    }


    @NonNull
    @Override
    protected DrawPenPoint clone() {
        DrawPenPoint drawPenPoint = new DrawPenPoint();
        drawPenPoint.setPaint(mPaint);
        drawPenPoint.setPath(mPath);
        return drawPenPoint;
    }
}
