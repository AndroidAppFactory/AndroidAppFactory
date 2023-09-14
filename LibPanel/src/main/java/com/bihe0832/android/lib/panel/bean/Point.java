package com.bihe0832.android.lib.panel.bean;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

public class Point implements Cloneable {

    @SerializedName("x")
    private float mX;
    @SerializedName("y")
    private float mY;

    public Point() {
    }

    public Point(float x, float y) {
        mX = x;
        mY = y;
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


    @NonNull
    @Override
    protected Point clone() {
        try {
            Point point = (Point) super.clone();
            return point;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return new Point();
    }
}
