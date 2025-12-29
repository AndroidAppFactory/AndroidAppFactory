package com.bihe0832.android.common.panel.ui;

import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.bihe0832.android.common.panel.R;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.lib.media.Media;
import com.bihe0832.android.lib.media.image.bitmap.BitmapUtil;
import com.bihe0832.android.lib.panel.PanelManager;
import com.bihe0832.android.lib.panel.constants.DrawEvent;
import com.bihe0832.android.lib.panel.event.DrawEventLiveData;
import com.bihe0832.android.lib.panel.widget.DrawPenView;
import com.bihe0832.android.lib.panel.widget.DrawTextLayout;
import com.bihe0832.android.lib.panel.widget.DrawTextView;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2023/9/6.
 *         Description:
 */
public class PanelDrawFragment extends BaseFragment {

    protected FrameLayout mContentLayout = null;
    protected DrawPenView mDrawPenView = null;
    protected DrawTextLayout mDrawTextView = null;

    @Override
    protected int getLayoutID() {
        return R.layout.com_bihe0832_common_panel_fragment_panel_draw;
    }

    @Override
    protected void initView(@NonNull View view) {
        super.initView(view);
        mContentLayout = view.findViewById(R.id.draw_content_layout);
        mDrawPenView = view.findViewById(R.id.draw_pen_view);
        mDrawTextView = view.findViewById(R.id.draw_text_view);
        mContentLayout.post(() -> {
            showPoints();
            selectOutSide();
        });
        initDrawEvent();
    }

    private void initDrawEvent() {
        DrawEventLiveData.INSTANCE.observe(this, event -> {
            if (event < DrawEvent.STATUS_START) {
                doDrawTypeEvent(event);
            } else if (event < DrawEvent.ACTION_START) {
                doDrawStatusEvent(event);
            } else {
                doDrawActionEvent(event);
            }
        });
    }

    private void doDrawTypeEvent(int event) {
        switch (event) {
            case DrawEvent.TYPE_DRAW_PEN:
                selectOutSide();
                drawPen();
                break;
            case DrawEvent.TYPE_DRAW_TEXT:
                selectOutSide();
                drawText();
                break;
            case DrawEvent.TYPE_DRAW_ERASER:
                selectOutSide();
                drawEraser();
                break;
            default:
                break;
        }
    }

    private void doDrawStatusEvent(int event) {
        switch (event) {
            case DrawEvent.STATUS_OUTSIDE_SELECTED:
                selectOutSide();
                break;
            case DrawEvent.STATUS_COLOR_CHANGED:
                colorChanged();
                break;
            case DrawEvent.STATUS_PEN_CHANGED:
                penChanged();
                break;
            case DrawEvent.STATUS_ERASER_CHANGED:
                eraserChanged();
                break;
            case DrawEvent.STATUS_TEXT_STYLE_CHANGED:
                textChanged();
                break;
            case DrawEvent.STATUS_RESET:
                showPoints();
                break;
            default:
                break;
        }
    }

    private void doDrawActionEvent(int event) {
        switch (event) {
            case DrawEvent.ACTION_REDO:
                redo();
                break;
            case DrawEvent.ACTION_UNDO:
                undo();
                break;
            case DrawEvent.ACTION_EXPORT:
                selectOutSide();
                saveImage();
                break;
            case DrawEvent.ACTION_SAVE:
                selectOutSide();
                break;
            case DrawEvent.ACTION_PAGE_NEW:
                selectOutSide();
                newPage();
                break;
            case DrawEvent.ACTION_PAGE_NEXT:
                selectOutSide();
                nextPage();
                break;
            case DrawEvent.ACTION_PAGE_PRE:
                selectOutSide();
                prePage();
                break;
            default:
                break;
        }
    }


    private void colorChanged() {
        mDrawPenView.setPenColor();
        mDrawTextView.setTextColor();
        mDrawPenView.showPoints();
        mDrawTextView.showPoints();
    }

    private void penChanged() {
        mDrawPenView.setPenSize();
        mDrawPenView.showPoints();
    }

    private void eraserChanged() {
        mDrawPenView.setEraserSize();
        mDrawPenView.showPoints();
    }

    private void textChanged() {
        mDrawTextView.setTextSize();
        mDrawTextView.showPoints();
    }

    private void selectOutSide() {
        if (null != PanelManager.getInstance().getCurrentDrawPoint().getDrawText()) {
            PanelManager.getInstance().getCurrentDrawPoint().getDrawText().setStatus(DrawTextView.TEXT_VIEW);
        }
        showPoints();
    }

    private void drawEraser() {
        PanelManager.getInstance().mCurrentDrawType = DrawEvent.TYPE_DRAW_ERASER;
        mDrawPenView.changeEraser();
        DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_TYPE_CHANGED);
    }

    private void drawPen() {
        PanelManager.getInstance().mCurrentDrawType = DrawEvent.TYPE_DRAW_PEN;
        mDrawPenView.setPaint(null);
        DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_TYPE_CHANGED);
    }

    private void drawText() {
        PanelManager.getInstance().mCurrentDrawType = DrawEvent.TYPE_DRAW_TEXT;
        if (PanelManager.getInstance().getCurrentDrawPoint().getDrawText() != null) {
            PanelManager.getInstance().getCurrentDrawPoint().getDrawText().setStatus(DrawTextView.TEXT_DETAIL);
        }
        mDrawTextView.setTextColor();
        mDrawTextView.setTextSize();
        DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_TYPE_CHANGED);
    }

    /**
     * 撤销
     */
    private void undo() {
        int size = PanelManager.getInstance().getCurrentPagePoints().size();
        if (size == 0) {
            return;
        }
        PanelManager.getInstance().getCurrentPageDeletePoints()
                .add(PanelManager.getInstance().getCurrentPagePoints().get(size - 1));
        PanelManager.getInstance().getCurrentPagePoints().remove(size - 1);
        size = PanelManager.getInstance().getCurrentPageDeletePoints().size();
        if (PanelManager.getInstance().getCurrentPageDeletePoints().get(size - 1).getType()
                == DrawEvent.TYPE_DRAW_PEN) {
            mDrawPenView.undo();
            mDrawPenView.showPoints();
        } else if (PanelManager.getInstance().getCurrentPageDeletePoints().get(size - 1).getType()
                == DrawEvent.TYPE_DRAW_TEXT) {
            mDrawTextView.undo();
            mDrawTextView.showPoints();
        }
        DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_REDO_UNDO_CHANGED);
    }

    /**
     * 重做
     */
    private void redo() {
        int size = PanelManager.getInstance().getCurrentPageDeletePoints().size();
        if (size == 0) {
            return;
        }
        PanelManager.getInstance().getCurrentPagePoints()
                .add(PanelManager.getInstance().getCurrentPageDeletePoints().get(size - 1));
        PanelManager.getInstance().getCurrentPageDeletePoints().remove(size - 1);
        size = PanelManager.getInstance().getCurrentPagePoints().size();
        if (PanelManager.getInstance().getCurrentPagePoints().get(size - 1).getType() == DrawEvent.TYPE_DRAW_PEN) {
            mDrawPenView.redo();
            mDrawPenView.showPoints();
        } else if (PanelManager.getInstance().getCurrentPagePoints().get(size - 1).getType()
                == DrawEvent.TYPE_DRAW_TEXT) {
            mDrawTextView.redo();
            mDrawTextView.showPoints();
        }
        DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_REDO_UNDO_CHANGED);
    }

    /**
     * 保存当前白板为图片
     */
    public void saveImage() {
        try {
            mContentLayout.setDrawingCacheEnabled(true);
            mContentLayout.buildDrawingCache();
            String fileName = BitmapUtil.getViewBitmap(mContentLayout);
            mContentLayout.destroyDrawingCache();
            Media.addToPhotos(getContext(), fileName);
            ZixieContext.INSTANCE.showToast(getString(com.bihe0832.android.model.res.R.string.white_board_export_tip) + fileName);
        } catch (Exception e) {
            ZixieContext.INSTANCE.showToast(getString(com.bihe0832.android.model.res.R.string.white_board_export_fail));
            e.printStackTrace();
        }
    }


    /**
     * 重新显示白板
     */
    private void showPoints() {
        if (isRootViewCreated()) {
            mDrawPenView.showPoints();
            mDrawTextView.showPoints();
        }
    }


    /**
     * 新建白板
     */
    private void newPage() {
        PanelManager.getInstance().newPage();
        showPoints();
        selectOutSide();
        DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_PAGE_CHANGED);
    }

    /**
     * 上一页
     */
    private void prePage() {
        if (PanelManager.getInstance().mCurrentIndex > 0) {
            PanelManager.getInstance().mCurrentIndex--;
            showPoints();
            selectOutSide();
        }
        DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_PAGE_CHANGED);
    }

    /**
     * 下一页
     */
    private void nextPage() {
        if (PanelManager.getInstance().mCurrentIndex + 1 < PanelManager.getInstance()
                .getCurrentBoardPointSize()) {
            PanelManager.getInstance().mCurrentIndex++;
            showPoints();
            selectOutSide();
        }
        DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_PAGE_CHANGED);
    }
}
