package com.bihe0832.android.lib.panel.bean;


import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

public class DrawPoint implements Cloneable {

    /**
     * 绘画类型
     */
    @SerializedName("type")
    private int mType;

    /**
     * 画笔路径
     */
    private DrawPenPoint mDrawPen;
    /**
     * 文字
     */
    @SerializedName("draw_text")
    private DrawTextPoint mDrawText;
    /**
     * 画笔路径（字符形式）
     */
    @SerializedName("draw_pen_str")
    private DrawPenStr mDrawPenStr;

    public DrawPenStr getDrawPenStr() {
        return mDrawPenStr;
    }

    public void setDrawPenStr(DrawPenStr DrawPenStr) {
        this.mDrawPenStr = DrawPenStr;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public DrawPenPoint getDrawPen() {
        return mDrawPen;
    }

    public void setDrawPen(DrawPenPoint drawPen) {
        this.mDrawPen = drawPen;
    }

    public DrawTextPoint getDrawText() {
        return mDrawText;
    }

    public void setDrawText(DrawTextPoint drawText) {
        mDrawText = drawText;
    }

    @NonNull
    @Override
    public DrawPoint clone() {
        DrawPoint dp = new DrawPoint();
        dp.mType = this.mType;
        if (this.mDrawText != null) {
            dp.mDrawText = this.mDrawText.clone();
        }
        if (this.mDrawPenStr != null) {
            dp.mDrawPenStr = this.mDrawPenStr.clone();
        }
        return dp;
    }
}
