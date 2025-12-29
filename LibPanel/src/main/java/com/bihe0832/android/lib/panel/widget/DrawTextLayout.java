package com.bihe0832.android.lib.panel.widget;


import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.bihe0832.android.lib.panel.PanelManager;
import com.bihe0832.android.lib.panel.R;
import com.bihe0832.android.lib.panel.bean.DrawPoint;
import com.bihe0832.android.lib.panel.bean.DrawTextPoint;
import com.bihe0832.android.lib.panel.constants.DrawEvent;
import com.bihe0832.android.lib.panel.event.DrawEventLiveData;

/**
 * 白板--文字层
 */
public class DrawTextLayout extends FrameLayout {


    private Context mContext;


    private static final int MARGIN_RIGHT = 100;
    private static final int MARGIN_BOTTOM = 75;

    public DrawTextLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }


    public DrawTextLayout(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;

    }

    public void init(Activity activity) {
        LayoutParams params = (LayoutParams) this.getLayoutParams();
        if (activity.getWindowManager() != null) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            params.width = display.getWidth();
            params.height = display.getWidth();
        }
        this.setLayoutParams(params);
        this.setBackgroundColor(getResources().getColor(com.bihe0832.android.lib.aaf.res.R.color.transparent));
        showPoints();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (PanelManager.getInstance().mCurrentDrawType == DrawEvent.TYPE_DRAW_TEXT
                && PanelManager.getInstance().ENABLE) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    float moveX = event.getX();
                    float moveY = event.getY();
                    if (getHeight() - moveY < dip2px(MARGIN_BOTTOM)) {
                        moveY = moveY - dip2px(MARGIN_BOTTOM);
                    }
                    if (getWidth() - moveX < dip2px(MARGIN_RIGHT)) {
                        moveX = moveX - dip2px(MARGIN_RIGHT);
                    }
                    DrawTextPoint ip = new DrawTextPoint();
                    ip.setX(moveX);
                    ip.setY(moveY);
                    ip.setColor(PanelManager.getInstance().mCurrentColor);
                    ip.setSize(PanelManager.getInstance().mCurrentTextSize);
                    ip.setStatus(DrawTextView.TEXT_EDIT);
                    ip.setIsVisible(true);
                    ip.setId(PanelManager.getInstance().getNewMarkId());

                    DrawPoint drawPoint = new DrawPoint();
                    drawPoint.setType(DrawEvent.TYPE_DRAW_TEXT);
                    drawPoint.setDrawText(ip);
                    showPoints();
                    showNewPoint(drawPoint);
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    public void showPoints() {
        int size = PanelManager.getInstance().getCurrentPagePoints().size();
        this.removeAllViews();
        if (size == 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            DrawPoint dp = PanelManager.getInstance().getCurrentPagePoints().get(i);
            if (dp.getType() == DrawEvent.TYPE_DRAW_TEXT && dp.getDrawText().getIsVisible()
                    && dp.getDrawText().getStatus() != DrawTextView.TEXT_DELETE) {
                final DrawTextView dw = new DrawTextView(mContext,
                        dp, mCallBackListener);
                dw.setTag(i);
                this.addView(dw);
            }
        }
    }


    private void showNewPoint(DrawPoint dp) {
        if (dp.getType() == DrawEvent.TYPE_DRAW_TEXT && dp.getDrawText().getIsVisible()
                && dp.getDrawText().getStatus() != DrawTextView.TEXT_DELETE) {
            final DrawTextView dw = new DrawTextView(mContext,
                    dp, mCallBackListener);
            this.addView(dw);
        }
    }

    private DrawTextView.CallBackListener mCallBackListener = new DrawTextView.CallBackListener() {

        @Override
        public void onUpdate(DrawPoint drawPoint) {
            updatePoint(drawPoint);
            showPoints();
        }

    };

    private void updatePoint(DrawPoint drawPoint) {
        int size = PanelManager.getInstance().getCurrentPagePoints().size();
        for (int i = size - 1; i >= 0; i--) {
            DrawPoint temp = PanelManager.getInstance().getCurrentPagePoints().get(i);
            if (temp.getType() == DrawEvent.TYPE_DRAW_TEXT && temp.getDrawText().getId() == drawPoint.getDrawText()
                    .getId()) {//如果文字组件是之前已存在，则隐藏之前的
                PanelManager.getInstance().getCurrentPagePoints().get(i).getDrawText().setIsVisible(false);
                break;
            }
        }
        if (!TextUtils.isEmpty(drawPoint.getDrawText().getStr())) {
            PanelManager.getInstance().getCurrentPagePoints().add(drawPoint);
            DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_DRAW_START);
        }
        PanelManager.getInstance().getCurrentPageDeletePoints().clear();
    }

    /**
     * 设置文字颜色
     */
    public void setTextColor() {
        int size = PanelManager.getInstance().getCurrentPagePoints().size();
        if (size == 0) {
            return;
        }
        DrawPoint dp = PanelManager.getInstance().getCurrentPagePoints().get(size - 1);
        if (dp.getType() == DrawEvent.TYPE_DRAW_TEXT && dp.getDrawText().getStatus() == DrawTextView.TEXT_DETAIL) {
            DrawPoint temp = dp.clone();
            temp.getDrawText().setColor(PanelManager.getInstance().mCurrentColor);
            updatePoint(temp);
            showPoints();
        }
    }

    /**
     * 设置文字颜色
     */
    public void setTextSize() {
        int size = PanelManager.getInstance().getCurrentPagePoints().size();
        if (size == 0) {
            return;
        }
        DrawPoint dp = PanelManager.getInstance().getCurrentPagePoints().get(size - 1);
        if (dp.getType() == DrawEvent.TYPE_DRAW_TEXT && dp.getDrawText().getStatus() == DrawTextView.TEXT_DETAIL) {
            DrawPoint temp = dp.clone();
            temp.getDrawText().setSize(PanelManager.getInstance().mCurrentTextSize);
            updatePoint(temp);
            showPoints();
        }
    }

    /**
     * 撤销
     */
    public void undo() {
        DrawPoint drawPoint = PanelManager.getInstance().getCurrentPageDeletePoints()
                .get(PanelManager.getInstance().getCurrentPageDeletePoints().size() - 1);
        int size = PanelManager.getInstance().getCurrentPagePoints().size();
        if (size > 0) {
            for (int i = size - 1; i >= 0; i--) {
                DrawPoint temp = PanelManager.getInstance().getCurrentPagePoints().get(i);
                if (temp.getType() == DrawEvent.TYPE_DRAW_TEXT && temp.getDrawText().getId() == drawPoint.getDrawText()
                        .getId()) {//如果文字组件是之前已存在，则显示最近的
                    PanelManager.getInstance().getCurrentPagePoints().get(i).getDrawText().setIsVisible(true);
                    break;
                }
            }
        }
        showPoints();
    }

    /**
     * 重做
     */
    public void redo() {
        int size = PanelManager.getInstance().getCurrentPagePoints().size();
        DrawPoint drawPoint = PanelManager.getInstance().getCurrentPagePoints().get(size - 1);
        if (size > 1) {
            for (int i = size - 2; i >= 0; i--) {
                DrawPoint temp = PanelManager.getInstance().getCurrentPagePoints().get(i);
                if (temp.getType() == DrawEvent.TYPE_DRAW_TEXT && temp.getDrawText().getId() == drawPoint.getDrawText()
                        .getId()) {//如果文字组件是之前已存在，则隐藏之前的
                    PanelManager.getInstance().getCurrentPagePoints().get(i).getDrawText().setIsVisible(false);
                    break;
                }
            }
        }
        showPoints();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
