package com.bihe0832.android.lib.panel.bean;


import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * 画板绘画路径存储
 */
public class BoardInfo {

    /**
     * 画板id
     */
    @SerializedName("id")
    private int mId;
    /**
     * 保存路径
     */
    @SerializedName("save_points")
    private List<DrawPoint> mSavePoints;
    /**
     * 撤销路径
     */
    @SerializedName("del_points")
    private List<DrawPoint> mDeletePoints;


    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public List<DrawPoint> getSavePoints() {
        if (mSavePoints == null) {
            mSavePoints = new ArrayList<DrawPoint>();
        }
        return mSavePoints;
    }

    public List<DrawPoint> getDeletePoints() {
        if (mDeletePoints == null) {
            mDeletePoints = new ArrayList<DrawPoint>();
        }
        return mDeletePoints;
    }

}
