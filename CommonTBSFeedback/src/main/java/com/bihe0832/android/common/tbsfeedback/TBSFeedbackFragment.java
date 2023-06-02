package com.bihe0832.android.common.tbsfeedback;


import android.os.Bundle;

import com.bihe0832.android.common.feedback.FeedbackFragment;
import com.bihe0832.android.common.tbswebview.CommonWebviewFragment;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-07-26.
 * Description: Description
 */
public class TBSFeedbackFragment extends CommonWebviewFragment {

    public static TBSFeedbackFragment newInstance(String url) {
        TBSFeedbackFragment fragment = new TBSFeedbackFragment();
        Bundle bundle = FeedbackFragment.getFeedbackDataBundle(url);
        fragment.setArguments(bundle);
        return fragment;
    }

    // 追加业务参数，建议尽量追加静态参数，不要追加动态参数
    @Override
    protected String getFinalURL(String url) {
        return FeedbackFragment.getFinalFeedbackURL(url);
    }

    //在最后loadURL之前执行操作
    @Override
    protected void actionBeforeLoadURL(String url) {
        return;
    }
}
