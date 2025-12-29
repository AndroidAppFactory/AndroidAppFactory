package com.bihe0832.android.lib.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.media.image.GlideExtKt;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.theme.ThemeResourcesManager;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener;
import com.bihe0832.android.lib.ui.view.ext.ViewExtKt;
import com.bihe0832.android.lib.utils.os.DisplayUtil;


public class CommonDialog extends Dialog {

    /**
     * 设置确定取消按钮的回调
     */
    protected OnDialogListener onClickBottomListener = null;
    /**
     * 显示的标题
     */
    private TextView titleTv;
    /**
     * 显示的内容
     */
    private LinearLayout contentLayout;
    private TextView contentTv;
    /**
     * 确认和取消按钮
     */
    private Button negativeBn, positiveBn;
    /**
     * 中间图片
     */
    private ImageView imageView;
    /**
     * 按钮之间的分割线
     */
    private View columnLineView;
    private TextView feedback;
    private CheckBox nomoreCb;
    private View extraView;
    private boolean isShowCheckBox = false;
    private OnCheckedListener onCheckedListener = null;
    private OnDismissListener onDismissListener = null;
    private int contentColor = -1;
    private boolean loadImgWithFade = true;
    /**
     * 底部是否只有一个按钮
     */
    private boolean isSingle = false;
    /**
     * 都是内容数据
     */
    private String message = "";
    private String title = "";
    private String content = "";
    private CharSequence charSequenceContent = null;
    private MovementMethod movement = null;
    private String feedbackContent = "";
    private String positiveString = "";
    private String negativeString = "";
    private String imageUrl = null;
    private boolean shouldCanceledOutside = false;
    private int imageContentResId = -1;
    private int imageResId = -1;

    public CommonDialog(Context context) {
        super(context, R.style.AAFCommonDialog);
    }

    public CommonDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected int getLayoutID() {
        return R.layout.com_bihe0832_common_dialog_layout;
    }

    protected TextView getFeedback() {
        return feedback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutID());
        //初始化界面控件
        initView();
        //初始化界面控件的事件
        initEvent();
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    protected void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        if (positiveBn != null) {
            positiveBn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickBottomListener != null) {
                        onClickBottomListener.onPositiveClick();
                    }
                }
            });
        }

        if (negativeBn != null) {
            //设置取消按钮被点击后，向外界提供监听
            negativeBn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickBottomListener != null) {
                        onClickBottomListener.onNegativeClick();
                    }
                }
            });
        }

        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (onClickBottomListener != null) {
                    onClickBottomListener.onCancel();
                }
            }
        });
    }

    /**
     * 初始化界面控件的显示数据
     */
    protected void refreshView() {
        //如果用户自定了title和message
        setCanceledOnTouchOutside(shouldCanceledOutside);
        setCancelable(shouldCanceledOutside);
        int screenWidth = DisplayUtil.getScreenWidth(getContext());
        int screenheight = DisplayUtil.getScreenHeight(getContext());
        if (titleTv != null) {
            if (!TextUtils.isEmpty(title)) {
                titleTv.setText(TextFactoryUtils.getSpannedTextByHtml(title));
                titleTv.setVisibility(View.VISIBLE);
            } else {
                titleTv.setVisibility(View.GONE);
            }
        }

        if (null != contentTv) {
            if (!TextUtils.isEmpty(content) || null != charSequenceContent) {
                if (!TextUtils.isEmpty(content)) {
                    contentTv.setText(content);
                } else if (null != charSequenceContent) {
                    contentTv.setText(charSequenceContent);
                }
                if (null != movement) {
                    contentTv.setMovementMethod(movement);
                } else {
                    contentTv.setMovementMethod(new ScrollingMovementMethod());
                }
                contentTv.setVisibility(View.VISIBLE);
                if (contentColor != -1) {
                    contentTv.setTextColor(contentColor);
                }
            } else {
                contentTv.setVisibility(View.GONE);
            }
        }

        if (extraView != null && contentLayout != null && extraView.getParent() == null) {
            contentLayout.removeAllViews();
            contentLayout.addView(extraView);
        }
        //如果设置按钮的文字
        if (positiveBn != null) {
            if (!TextUtils.isEmpty(positiveString)) {
                positiveBn.setText(TextFactoryUtils.getSpannedTextByHtml(positiveString));
            } else {
                positiveBn.setText(ThemeResourcesManager.INSTANCE.getString(com.bihe0832.android.lib.aaf.res.R.string.dialog_button_ok));
            }
        }

        if (negativeBn != null) {
            if (!TextUtils.isEmpty(negativeString)) {
                negativeBn.setText(TextFactoryUtils.getSpannedTextByHtml(negativeString));
            }
        }

        if (imageView != null) {
            if (imageContentResId != -1) {
                imageView.setImageResource(imageContentResId);
                imageView.setVisibility(View.VISIBLE);
            } else if (imageUrl != null) {
                GlideExtKt.loadCenterCropImage(imageView, imageUrl, Color.BLACK, Color.WHITE, loadImgWithFade);
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.GONE);
            }

            if (imageView != null && imageView.getVisibility() == View.VISIBLE) {
                ViewGroup.LayoutParams para = imageView.getLayoutParams();
                if (para.width > 0) {
                    para.height = para.width * 1920 / 1080;
                } else {
                    para.height = (int) ((screenWidth - DisplayUtil.dip2px(getContext(), 88)) * 1080 / 1920);
                }
                imageView.setLayoutParams(para);
                if (screenWidth > screenheight) {
                    para.height = (int) (screenheight * 0.3);
                }
            }
        }

        if (feedback != null) {
            if (!TextUtils.isEmpty(feedbackContent)) {
                feedback.setText(feedbackContent);
                feedback.setVisibility(View.VISIBLE);
            } else {
                feedback.setVisibility(View.GONE);
            }
        }
        /**
         * 只显示一个按钮的时候隐藏取消按钮，回掉只执行确定的事件
         */
        if (isSingle || TextUtils.isEmpty(negativeString)) {
            if (columnLineView != null) {
                columnLineView.setVisibility(View.GONE);
            }
            if (negativeBn != null) {
                negativeBn.setVisibility(View.GONE);
            }
        } else {
            if (columnLineView != null) {
                columnLineView.setVisibility(View.VISIBLE);
            }
            if (negativeBn != null) {
                negativeBn.setVisibility(View.VISIBLE);
            }
        }

        if (isShowCheckBox) {
            nomoreCb.setVisibility(View.VISIBLE);
            nomoreCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (onCheckedListener != null) {
                        onCheckedListener.onChecked(isChecked);
                    }
                }
            });
        }
    }

    private void showAction() {
        if (!isShowing()) {
            Activity activity = ViewExtKt.getActivity(getContext());
            if (null != activity && !activity.isFinishing()) {
                super.show();
            } else {
                ZLog.e("activity is null or isFinishing");
            }
        }
        refreshView();
    }

    @Override
    public void show() {
        ThreadManager.getInstance().runOnUIThread(new Runnable() {
            @Override
            public void run() {
                showAction();
            }
        });
    }

    /**
     * 初始化界面控件
     */
    protected void initView() {
        try {
            negativeBn = (Button) findViewById(R.id.negative);
        } catch (Exception e) {
            ZLog.e("\n\n CommonDialog throw Exception while initView negative");
            e.printStackTrace();
        }
        try {
            positiveBn = (Button) findViewById(R.id.positive);
        } catch (Exception e) {
            ZLog.e("\n\n CommonDialog throw Exception while initView positive");
            e.printStackTrace();
        }
        try {
            titleTv = (TextView) findViewById(R.id.title);
        } catch (Exception e) {
            ZLog.e("\n\n CommonDialog throw Exception while initView title");
            e.printStackTrace();
        }
        try {
            contentTv = (TextView) findViewById(R.id.content);
        } catch (Exception e) {
            ZLog.e("\n\n CommonDialog throw Exception while initView content");
            e.printStackTrace();
        }
        try {
            columnLineView = findViewById(R.id.column_line);
        } catch (Exception e) {
            ZLog.e("\n\n CommonDialog throw Exception while initView column_line");
            e.printStackTrace();
        }
        try {
            imageView = (ImageView) findViewById(R.id.content_img);
        } catch (Exception e) {
            ZLog.e("\n\n CommonDialog throw Exception while initView content_img");
            e.printStackTrace();
        }
        try {
            feedback = (TextView) findViewById(R.id.feedback);
        } catch (Exception e) {
            ZLog.e("\n\n CommonDialog throw Exception while initView feedback");
            e.printStackTrace();
        }
        try {
            nomoreCb = (CheckBox) findViewById(R.id.nomore_cb);
        } catch (Exception e) {
            ZLog.e("\n\n CommonDialog throw Exception while initView nomore_cb");
            e.printStackTrace();
        }
        try {
            contentLayout = findViewById(R.id.content_layout);
        } catch (Exception e) {
            ZLog.e("\n\n CommonDialog throw Exception while initView content_layout");
            e.printStackTrace();
        }
    }

    public OnDialogListener getOnClickBottomListener() {
        return onClickBottomListener;
    }

    public CommonDialog setOnClickBottomListener(OnDialogListener clickBottomListener) {
        this.onClickBottomListener = clickBottomListener;
        return this;
    }

    public OnDismissListener getOnDismissListener() {
        return onDismissListener;
    }

    @Override
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        super.setOnDismissListener(this.onDismissListener);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public CommonDialog setOnCheckedListener(OnCheckedListener onCheckedListener) {
        this.onCheckedListener = onCheckedListener;
        return this;
    }

    public CommonDialog setIsShowCheckBox(boolean isShowCheckBox) {
        this.isShowCheckBox = isShowCheckBox;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public CommonDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getContent() {
        return content;
    }

    public CommonDialog setContent(String content) {
        this.content = content;
        return this;
    }

    public CommonDialog setContentTextColor(int colorRes) {
        this.contentColor = colorRes;
        return this;
    }

    public CommonDialog setHtmlContent(String content) {
        this.charSequenceContent = TextFactoryUtils.getSpannedTextByHtml(content);
        return this;
    }

    public CommonDialog setHtmlContent(CharSequence content, MovementMethod method) {
        this.charSequenceContent = content;
        this.movement = method;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CommonDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getPositive() {
        return positiveString;
    }

    public CommonDialog setPositive(String positive) {
        this.positiveString = positive;
        return this;
    }

    public void addViewToContent(View view) {
        extraView = view;
    }

    public String getNegative() {
        return negativeString;
    }

    public CommonDialog setNegative(String negtive) {
        this.negativeString = negtive;
        return this;
    }

    /**
     * replaced by {@link #setShouldCanceled(boolean)}
     */
    @Deprecated
    @Override
    public void setCancelable(boolean flag) {
        super.setCancelable(flag);
        shouldCanceledOutside = flag;
    }

    /**
     * replaced by {@link #setShouldCanceled(boolean)}
     */
    @Deprecated
    public void setCanceledOnTouchOutside(boolean flag) {
        super.setCanceledOnTouchOutside(flag);
        shouldCanceledOutside = flag;
    }

    public boolean getShouldCanceled() {
        return shouldCanceledOutside;
    }

    public CommonDialog setShouldCanceled(boolean flag) {
        shouldCanceledOutside = flag;
        return this;
    }

    public int getImageResId() {
        return imageResId;
    }

    public CommonDialog setImageResId(int imageResId) {
        this.imageResId = imageResId;
        return this;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public CommonDialog setSingle(boolean single) {
        isSingle = single;
        return this;
    }

    public int getImageContentResId() {
        return imageContentResId;
    }

    public CommonDialog setImageContentResId(int imageContentResId) {
        this.imageContentResId = imageContentResId;
        return this;
    }

    public CommonDialog setFeedBackContent(String feedbackContent) {
        this.feedbackContent = feedbackContent;
        return this;
    }

    public CommonDialog setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public CommonDialog setLoadImgWithFade(boolean needFade) {
        this.loadImgWithFade = needFade;
        return this;
    }

    public interface OnCheckedListener {

        public void onChecked(boolean isChecked);
    }
}