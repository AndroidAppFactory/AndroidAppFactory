package com.bihe0832.android.lib.panel.bean;

import android.content.res.Configuration;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * 画板合集
 */
public class BoardsInfo {

    /**
     * 唯一性id
     */
    @SerializedName("id")
    private String mId;

    @SerializedName("orientation")
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
    /**
     * 画板合集
     */
    @SerializedName("board_points")
    private List<BoardInfo> mBoardPoints = new ArrayList<>();

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public List<BoardInfo> getBoardList() {
        return mBoardPoints;
    }

    public void setBoardList(List<BoardInfo> whiteBoardPoints) {
        this.mBoardPoints = whiteBoardPoints;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(int mOrientation) {
        this.mOrientation = mOrientation;
    }
}
