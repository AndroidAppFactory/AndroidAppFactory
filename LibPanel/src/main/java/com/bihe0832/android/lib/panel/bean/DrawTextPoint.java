package com.bihe0832.android.lib.panel.bean;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

/**
 * 文字
 */
public class DrawTextPoint implements Cloneable {

    /**
     * 唯一性标识
     */
    @SerializedName("id")
    private long mId;
    /**
     * 文字x坐标
     */
    @SerializedName("x")
    private float mX;
    /**
     * 文字y坐标
     */
    @SerializedName("y")
    private float mY;
    /**
     * 文字
     */
    @SerializedName("content")
    private String mStr;
    /**
     * 是否有下划线
     */
    @SerializedName("underline")
    private boolean mIsUnderline;
    /**
     * 是否斜体
     */
    @SerializedName("italics")
    private boolean mIsItalics;
    /**
     * 是否粗体
     */
    @SerializedName("bold")
    private boolean mIsBold;

    /**
     * 文字颜色
     */
    @SerializedName("color")
    private int mColor;

    /**
     * 文字颜色
     */
    @SerializedName("size")
    private int mSize;
    /**
     * 当前文字状态
     */
    @SerializedName("status")
    private int mStatus;

    /**
     * 是否显示
     */
    @SerializedName("visible")
    private boolean mIsVisible;


    public long getId() {
        return mId;
    }


    public void setId(long id) {
        this.mId = id;
    }

    public float getX() {
        return mX;
    }


    public void setX(float x) {
        this.mX = x;
    }


    public float getY() {
        return mY;
    }


    public void setY(float y) {
        this.mY = y;
    }


    public String getStr() {
        return mStr;
    }


    public void setStr(String str) {
        this.mStr = str;
    }


    public boolean getIsUnderline() {
        return mIsUnderline;
    }


    public void setIsUnderline(boolean isUnderline) {
        this.mIsUnderline = isUnderline;
    }


    public boolean getIsItalics() {
        return mIsItalics;
    }

    public void setIsItalics(boolean isItalics) {
        this.mIsItalics = isItalics;
    }

    public boolean getIsBold() {
        return mIsBold;
    }


    public void setIsBold(boolean isBold) {
        this.mIsBold = isBold;
    }


    public void setSize(int mSize) {
        this.mSize = mSize;
    }

    public int getSize() {
        return mSize;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public boolean getIsVisible() {
        return mIsVisible;
    }


    public void setIsVisible(boolean isVisible) {
        this.mIsVisible = isVisible;
    }

    @NonNull
    @Override
    protected DrawTextPoint clone() {
        try {
            DrawTextPoint drawTextPoint = (DrawTextPoint) super.clone();
            return drawTextPoint;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return new DrawTextPoint();
    }
}