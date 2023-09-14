package com.bihe0832.android.lib.panel;


import android.content.res.Configuration;
import android.graphics.Color;
import com.bihe0832.android.lib.panel.bean.BoardInfo;
import com.bihe0832.android.lib.panel.bean.BoardsInfo;
import com.bihe0832.android.lib.panel.bean.DrawPoint;
import com.bihe0832.android.lib.panel.constants.DrawEvent;
import com.bihe0832.android.lib.panel.event.DrawEventLiveData;
import com.bihe0832.android.lib.utils.IdGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 * 白板操作公共类
 */

public class PanelManager {

    /**
     * 单例
     */
    private static PanelManager mPanelManager;
    /**
     * 当前是否禁止白板操作
     */
    public boolean ENABLE = true;
    /**
     * 当前所在白板位置
     */
    public int mCurrentIndex = 0;
    public String mFilePath = "";
    /**
     * 当前绘画类型：笔或者文字等
     */
    public int mCurrentDrawType = DrawEvent.TYPE_DRAW_PEN;
    /**
     * 当前颜色
     */
    public int mCurrentColor = Color.BLACK;
    /**
     * 当前画笔大小
     */
    public int mCurrentPenSize = 16;
    /**
     * 当前文字大小
     */
    public int mCurrentTextSize = 36;
    /**
     * 当前橡皮擦大小
     */
    public int mCurrentEraserSize = 18;
    /**
     * 每次操作的唯一标识
     */
    private IdGenerator markGenerator = new IdGenerator(1);
    /**
     * 白板操作集
     */
    private BoardsInfo mCurrentBoard;

    /**
     * 私有实例化
     */
    private PanelManager() {
    }

    /**
     * 单例
     */
    public static PanelManager getInstance() {
        if (mPanelManager == null) {
            mPanelManager = new PanelManager();
        }
        return mPanelManager;
    }

    /**
     * 返回指定白板的操作集大小
     */
    public int getCurrentBoardPointSize() {
        if (mCurrentBoard != null && null != mCurrentBoard.getBoardList()) {
            return mCurrentBoard.getBoardList().size();
        } else {
            return 0;
        }
    }


    /**
     * 返回指定白板的操作集
     */
    public void initDrawBoard(BoardsInfo whiteBoardPoints) {
        if (whiteBoardPoints != null) {
            mCurrentBoard = whiteBoardPoints;
            initDrawBoard(whiteBoardPoints.getOrientation());
        } else {
            initDrawBoard(Configuration.ORIENTATION_PORTRAIT);
        }
    }

    /**
     * 初始化白板
     */
    public void initDrawBoard(int orientation) {
        if (mCurrentBoard == null) {
            mCurrentBoard = new BoardsInfo();
            mCurrentBoard.setOrientation(orientation);
            mCurrentBoard.setId(String.valueOf(System.currentTimeMillis()));
        }
        if (mCurrentBoard.getBoardList() == null) {
            mCurrentBoard.setBoardList(new ArrayList<BoardInfo>());
        }
    }

    /**
     * 返回指定白板的操作集
     */
    public BoardInfo getBoardPage(int i) {
        if (mCurrentBoard.getBoardList().size() <= i) {
            BoardInfo drawPointList = new BoardInfo();
            drawPointList.setId(i);
            mCurrentBoard.getBoardList().add(drawPointList);
            return getBoardPage(i);
        } else {
            return mCurrentBoard.getBoardList().get(i);
        }
    }

    /**
     * 新建白板
     */
    public void newPage() {
        mCurrentIndex = getCurrentBoardPointSize();
        getBoardPage(mCurrentIndex);
    }


    /**
     * 获取每次操作的唯一标识
     */
    public long getNewMarkId() {
        return markGenerator.generate();
    }

    public BoardsInfo getDrawBoard() {
        return mCurrentBoard;
    }

    public List<DrawPoint> getCurrentPagePoints() {
        return PanelManager.getInstance().getBoardPage(PanelManager.getInstance().mCurrentIndex)
                .getSavePoints();
    }

    public List<DrawPoint> getCurrentPageDeletePoints() {
        return PanelManager.getInstance().getBoardPage(PanelManager.getInstance().mCurrentIndex)
                .getDeletePoints();
    }

    public DrawPoint getCurrentDrawPoint() {
        List<DrawPoint> points = getCurrentPagePoints();
        if (points.size() > 0) {
            return PanelManager.getInstance().getCurrentPagePoints().get(points.size() - 1);
        } else {
            return new DrawPoint();
        }
    }

    public void distory() {
        DrawEventLiveData.INSTANCE.postValue(DrawEvent.TYPE_START);
        mCurrentBoard = null;
        mFilePath = "";
    }
}
