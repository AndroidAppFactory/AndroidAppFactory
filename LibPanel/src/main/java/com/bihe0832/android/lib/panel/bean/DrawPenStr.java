package com.bihe0832.android.lib.panel.bean;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * 画笔路径(字符串格式，方便储存)
 */
public class DrawPenStr implements Cloneable {

    /**
     * 画笔颜色
     */
    @SerializedName("color")
    private int mColor;
    /**
     * 画笔粗细
     */
    @SerializedName("stroke_width")
    private float mStrokeWidth;
    /**
     * 是否橡皮擦
     */
    @SerializedName("eraser")
    private boolean mIsEraser;
    /**
     * 移动到初始点坐标
     */
    @SerializedName("moveto")
    private Point mMoveTo;
    /**
     * 移动中A集
     */
    @SerializedName("a")
    private List<Point> mQuadToA;
    /**
     * 移动中B集
     */
    @SerializedName("b")
    private List<Point> mQuadToB;
    /**
     * 移动到终点坐标
     */
    @SerializedName("liveto")
    private Point mLineTo;
    /**
     * 所在界面高距坐标
     */
    @SerializedName("offset")
    private Point mOffset;

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.mStrokeWidth = strokeWidth;
    }

    public boolean getIsEraser() {
        return mIsEraser;
    }


    public void setIsEraser(boolean isEraser) {
        this.mIsEraser = isEraser;
    }

    public Point getMoveTo() {
        return mMoveTo;
    }

    public void setMoveTo(Point moveTo) {
        this.mMoveTo = moveTo;
    }


    public List<Point> getQuadToA() {
        if (null == mQuadToA) {
            mQuadToA = new ArrayList<Point>();
        }
        return mQuadToA;
    }

    public void setQuadToA(List<Point> quadToA) {
        this.mQuadToA = quadToA;
    }

    public List<Point> getQuadToB() {
        if (null == mQuadToB) {
            mQuadToB = new ArrayList<Point>();
        }
        return mQuadToB;
    }

    public void setQuadToB(List<Point> quadToB) {
        this.mQuadToB = quadToB;
    }

    public Point getLineTo() {
        return mLineTo;
    }

    public void setLineTo(Point lineTo) {
        this.mLineTo = lineTo;
    }

    public Point getOffset() {
        return mOffset;
    }

    public void setOffset(Point offset) {
        this.mOffset = offset;
    }

    @NonNull
    @Override
    protected DrawPenStr clone() {
        try {
            DrawPenStr drawPenStr = (DrawPenStr) super.clone();
            return drawPenStr;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return new DrawPenStr();
    }
}
