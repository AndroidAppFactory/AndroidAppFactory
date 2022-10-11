package com.bihe0832.android.common.share;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.bihe0832.android.framework.ui.BaseBottomActivity;


/**
 * 不同分享的公共代码，分享的Activity的基类，提供基础的UI样式
 * <p>
 * 主题使用 AAF.ActivityTheme.Bottom
 */
public abstract class ShareBaseActivity extends BaseBottomActivity {

    /**
     * 布局layout
     *
     * @return
     */
    public int getLayoutID() {
        return R.layout.common_activity_share;
    }

    protected View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int vid = v.getId();
            if (vid == R.id.BaseRlContainer) {
                onShareCancelClick();
            } else if (vid == R.id.BaseShareToWeChatBtn) {
                onShareToWechatSessionBtnClick();
            } else if (vid == R.id.BaseShareToFriendsBtn) {
                onShareToWechatTimelineBtnClick();
            } else if (vid == R.id.BaseShareToQQBtn) {
                onShareToQQSessionBtnClick();
            } else if (vid == R.id.BaseShareToQzoneBtn) {
                onShareToQZoneBtnClick();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutID());
    }

    protected void initSuperView() {
        View BaseRlContainer = findViewById(R.id.BaseRlContainer);
        BaseRlContainer.setOnClickListener(onClickListener);
        findViewById(R.id.BaseShareToWeChatBtn).setOnClickListener(onClickListener);
        findViewById(R.id.BaseShareToFriendsBtn).setOnClickListener(onClickListener);
        findViewById(R.id.BaseShareToQQBtn).setOnClickListener(onClickListener);
        findViewById(R.id.BaseShareToQzoneBtn).setOnClickListener(onClickListener);
        if (showShareLink() && null != findViewById(R.id.BaseShareLinkBtn)) {
            findViewById(R.id.BaseShareLinkBtn).setVisibility(View.VISIBLE);
        }

        if (showSavePic() && null != findViewById(R.id.BaseShareDownloadBtn)) {
            findViewById(R.id.BaseShareDownloadBtn).setVisibility(View.VISIBLE);
        }

        if (showPicPreview() && null != findViewById(R.id.shareImagePreview)) {
            findViewById(R.id.shareImagePreview).setVisibility(View.VISIBLE);
        }
    }

    protected boolean showShareLink() {
        return true;
    }

    protected boolean showPicPreview() {
        return false;
    }

    protected boolean showSavePic() {
        return false;
    }

    @Override
    public void onBack() {
        onShareCancelClick();
    }

    protected View getBaseSharePanel() {
        return findViewById(R.id.BaseSharePanel);
    }

    protected abstract void onShareCancelClick();

    protected abstract void onShareToQQSessionBtnClick();

    protected abstract void onShareToQZoneBtnClick();

    protected abstract void onShareToWechatSessionBtnClick();

    protected abstract void onShareToWechatTimelineBtnClick();
}
