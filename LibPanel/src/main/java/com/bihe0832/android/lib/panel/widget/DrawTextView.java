package com.bihe0832.android.lib.panel.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.panel.PanelManager;
import com.bihe0832.android.lib.panel.R;
import com.bihe0832.android.lib.panel.bean.DrawPoint;
import com.bihe0832.android.lib.panel.constants.DrawEvent;
import com.bihe0832.android.lib.panel.event.DrawEventLiveData;
import com.bihe0832.android.lib.ui.dialog.callback.DialogStringCallback;
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils;

;

public class DrawTextView extends RelativeLayout implements View.OnClickListener {

    /**
     * 显示状态
     */
    public static final int TEXT_VIEW = 1;
    /**
     * 编辑（文字编辑）状态
     */
    public static final int TEXT_EDIT = 2;
    /**
     * 详情（显示删除、编辑按钮）状态
     */
    public static final int TEXT_DETAIL = 3;
    /**
     * 被删除状态
     */
    public static final int TEXT_DELETE = 4;

    private View panel_draw_text_outside;

    private RelativeLayout panel_draw_text_layout;
    /**
     *
     */
    private RelativeLayout panel_draw_text_container;

    private TextView panel_draw_text_content;

    private Button panel_draw_text_action_delete;

    private Button panel_draw_text_action_edit;

    private Context mContext;

    private CallBackListener mCallBackListener;

    private DrawPoint mDrawPoint;

    public DrawTextView(Context context
            , DrawPoint drawPoint,
            CallBackListener callBackListener) {
        super(context);
        init(context, drawPoint, callBackListener);
    }

    private void init(Context context
            , DrawPoint drawPoint,
            CallBackListener callBackListener) {
        mContext = context;
        mDrawPoint = drawPoint.clone();
        mCallBackListener = callBackListener;
        initUI();
        initEvent();
        switchView(mDrawPoint.getDrawText().getStatus());

    }

    private void initUI() {
        LayoutInflater.from(mContext).inflate(R.layout.com_bihe0832_lib_panel_draw_text, this, true);
        panel_draw_text_outside = (View) findViewById(R.id.panel_draw_text_outside);
        panel_draw_text_layout = (RelativeLayout) findViewById(R.id.panel_draw_text_layout);
        panel_draw_text_container = (RelativeLayout) findViewById(R.id.panel_draw_text_container);
        panel_draw_text_content = (TextView) findViewById(R.id.panel_draw_text_content);
        panel_draw_text_action_delete = (Button) findViewById(R.id.panel_draw_text_action_delete);
        panel_draw_text_action_edit = (Button) findViewById(R.id.panel_draw_text_action_edit);
        if (null != mDrawPoint) {
            setText(mDrawPoint.getDrawText().getStr());
        }
        setLayoutParams();

    }

    private void initEvent() {
        panel_draw_text_outside.setOnClickListener(this);
        panel_draw_text_container.setOnClickListener(this);
        panel_draw_text_action_delete.setOnClickListener(this);
        panel_draw_text_action_edit.setOnClickListener(this);
        panel_draw_text_content.setOnClickListener(this);
        panel_draw_text_content.setOnTouchListener(new OnTouchListener() {
            int lastX, lastY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (mDrawPoint.getDrawText().getStatus() == TEXT_DETAIL && PanelManager.getInstance().ENABLE) {
                    int ea = event.getAction();
                    switch (ea) {
                        case MotionEvent.ACTION_DOWN:
                            // 获取触摸事件触摸位置的原始X坐标
                            lastX = (int) event.getRawX();
                            lastY = (int) event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            int dx = (int) event.getRawX() - lastX;
                            int dy = (int) event.getRawY() - lastY;

                            int left = panel_draw_text_layout.getLeft() + dx;
                            int top = panel_draw_text_layout.getTop() + dy;
                            int right = panel_draw_text_layout.getRight() + dx;
                            int bottom = panel_draw_text_layout.getBottom() + dy;
                            if (left < 0) {
                                left = 0;
                                right = left + panel_draw_text_layout.getWidth();
                            }
                            if (right > getWidth()) {
                                right = getWidth();
                                left = right - panel_draw_text_layout.getWidth();
                            }
                            if (top < 0) {
                                top = 0;
                                bottom = top + panel_draw_text_layout.getHeight();
                            }
                            if (bottom > getHeight()) {
                                bottom = getHeight();
                                top = bottom - panel_draw_text_layout.getHeight();
                            }
                            mDrawPoint.getDrawText().setX(left);
                            mDrawPoint.getDrawText().setY(top);
                            ZLog.d("移动 :" + left + "," + top);
                            panel_draw_text_layout.layout(left, top, right, bottom);
                            lastX = (int) event.getRawX();
                            lastY = (int) event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            if (null != mCallBackListener) {
                                mCallBackListener.onUpdate(mDrawPoint);
                            }
                            break;
                    }
                }

                return false;
            }
        });
    }


    private void setText(String strText) {
        if (!TextUtils.isEmpty(strText)) {
            panel_draw_text_content.setText(strText);
        }
        panel_draw_text_content.setTextColor(mDrawPoint.getDrawText().getColor());
        panel_draw_text_content.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mDrawPoint.getDrawText().getSize());
        if (mDrawPoint.getDrawText().getIsUnderline()) {
            panel_draw_text_content.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        }
        if (mDrawPoint.getDrawText().getIsBold()) {
            panel_draw_text_content.getPaint().setFakeBoldText(true);
        }

        if (mDrawPoint.getDrawText().getIsItalics()) {
            panel_draw_text_content.getPaint().setTextSkewX(-0.25f);
        }

    }

    private void setLayoutParams() {
        LayoutParams layParamsTxt = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layParamsTxt.leftMargin = (int) mDrawPoint.getDrawText().getX();
        layParamsTxt.topMargin = (int) mDrawPoint.getDrawText().getY();
        panel_draw_text_layout.setLayoutParams(layParamsTxt);
    }

    public void switchView(int currentStatus) {
        switch (currentStatus) {
            case TEXT_VIEW:
                panel_draw_text_outside.setVisibility(View.GONE);
                panel_draw_text_content.setVisibility(View.VISIBLE);
                panel_draw_text_container.setBackgroundResource(
                        com.bihe0832.android.lib.aaf.res.R.color.transparent);
                panel_draw_text_action_edit.setVisibility(View.GONE);
                panel_draw_text_action_delete.setVisibility(View.GONE);
                break;
            case TEXT_EDIT:
                final String last = panel_draw_text_content.getText().toString();
                DialogUtils.INSTANCE.showInputDialog(getContext(),
                        getContext().getString(com.bihe0832.android.lib.aaf.res.R.string.panel_draw_text_add_title),
                        "",
                        TextUtils.isEmpty(last) ? getContext().getString(com.bihe0832.android.lib.aaf.res.R.string.dialog_button_ok) : getContext().getString(com.bihe0832.android.lib.aaf.res.R.string.panel_draw_text_add_positive),
                        TextUtils.isEmpty(last) ? "" : getContext().getString(com.bihe0832.android.lib.aaf.res.R.string.panel_draw_text_add_negative),
                        true,
                        EditorInfo.TYPE_CLASS_TEXT,
                        last,
                        "",
                        new DialogStringCallback() {
                            @Override
                            public void onPositiveClick(String s) {
                                mDrawPoint.getDrawText().setStr(s);
                                switchView(TEXT_DETAIL);
                            }

                            @Override
                            public void onNegativeClick(String s) {
                                switchView(TEXT_DETAIL);
                            }

                            @Override
                            public void onCancel(String s) {
                                switchView(TEXT_DETAIL);
                            }
                        }
                );
                break;
            case TEXT_DETAIL:
                panel_draw_text_outside.setBackgroundResource(com.bihe0832.android.lib.aaf.res.R.color.transparent);
                panel_draw_text_outside.setVisibility(View.VISIBLE);
                panel_draw_text_content.setVisibility(View.VISIBLE);
                panel_draw_text_container.setBackgroundResource(R.drawable.com_bihe0832_lib_panel_draw_text_border);
                if (PanelManager.getInstance().mCurrentDrawType == DrawEvent.TYPE_DRAW_TEXT) {
                    panel_draw_text_action_edit.setVisibility(View.VISIBLE);
                } else {
                    panel_draw_text_action_edit.setVisibility(View.GONE);
                }
                if (PanelManager.getInstance().mCurrentDrawType == DrawEvent.TYPE_DRAW_TEXT
                        || PanelManager.getInstance().mCurrentDrawType == DrawEvent.TYPE_DRAW_ERASER) {
                    panel_draw_text_action_delete.setVisibility(View.VISIBLE);
                } else {
                    panel_draw_text_action_delete.setVisibility(View.GONE);
                }
                break;
            case TEXT_DELETE:
            default:
                break;
        }
        if (mDrawPoint.getDrawText().getStatus() != currentStatus) {
            mDrawPoint.getDrawText().setStatus(currentStatus);
            if (null != mCallBackListener && currentStatus != TEXT_EDIT) {
                mCallBackListener.onUpdate(mDrawPoint);
            }
        }

    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        if (vId == R.id.panel_draw_text_outside) {
            if (mDrawPoint.getDrawText().getStatus() == TEXT_DETAIL && PanelManager.getInstance().ENABLE) {
                switchView(TEXT_VIEW);
            }
        } else if (vId == R.id.panel_draw_text_content) {
            if (PanelManager.getInstance().ENABLE) {
                if (PanelManager.getInstance().mCurrentDrawType == DrawEvent.TYPE_DRAW_TEXT) {
                    switchView(TEXT_DETAIL);
                    DrawEventLiveData.INSTANCE.postValue(DrawEvent.STATUS_TEXT_SELECTED);
                } else if (PanelManager.getInstance().mCurrentDrawType == DrawEvent.TYPE_DRAW_ERASER) {
                    switchView(TEXT_DETAIL);
                }
            }
        } else if (vId == R.id.panel_draw_text_action_delete) {
            if (PanelManager.getInstance().ENABLE) {
                switchView(TEXT_DELETE);
            }
        } else if (vId == R.id.panel_draw_text_action_edit) {
            if (PanelManager.getInstance().ENABLE) {
                switchView(TEXT_EDIT);
            }
        }
    }

    public interface CallBackListener {

        /**
         * 更新文字属性
         */
        void onUpdate(DrawPoint drawPoint);
    }
}
