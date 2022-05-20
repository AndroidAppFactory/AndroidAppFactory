package com.bihe0832.android.common.feedback;


import android.os.Bundle;
import com.bihe0832.android.common.webview.CommonWebviewFragment;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.lib.request.URLUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import com.bihe0832.android.lib.utils.os.ManufacturerUtil;

/**
 * @author hardyshi code@bihe0832.com
 *         Created on 2019-07-26.
 *         Description: Description
 */
public class FeedbackFragment extends CommonWebviewFragment {

    public static FeedbackFragment newInstance(String url) {
        FeedbackFragment fragment = new FeedbackFragment();
        Bundle bundle = getWebviewDataBundle(url, fragment.getPostData());
        fragment.setArguments(bundle);
        return fragment;
    }

    // 追加业务参数
    @Override
    protected String getFinalURL(String url) {
        String mUserID = ZixieContext.INSTANCE.getDeviceId();
        String mNickName = "";
        String mHeadIconURL = "";

        String postData = "&nickname=" + mNickName + "&avatar=" + URLUtils.encode(mHeadIconURL) + "&openid=" + mUserID;
        return url + "?d-wx-push=1" + postData;
    }

    private String getPostData() {
        StringBuilder builder = new StringBuilder();
        builder.append("clientInfo=");
        if (!ZixieContext.INSTANCE.isOfficial()) {
            builder.append("内部版本").append("/");
        }
        builder.append(ManufacturerUtil.INSTANCE.getMANUFACTURER()).append("/")
                .append(ManufacturerUtil.INSTANCE.getBRAND()).append("/").append(
                ManufacturerUtil.INSTANCE.getMODEL());
        builder.append("&clientVersion=");
        builder.append(ZixieContext.INSTANCE.getVersionName())
                .append("/").append(ZixieContext.INSTANCE.getVersionCode())
                .append("/").append(ZixieContext.INSTANCE.getVersionTag())
                .append("/").append(ZixieContext.INSTANCE.getChannelID());
        builder.append("&os=");
        builder.append("Android").append("/").append(BuildUtils.INSTANCE.getRELEASE()).append("/")
                .append(BuildUtils.INSTANCE.getSDK_INT());
        builder.append("&imei=").append(ZixieContext.INSTANCE.getDeviceId());
        builder.append("&customInfo=");
        return builder.toString();
    }

    //在最后loadURL之前执行操作
    @Override
    protected void actionBeforeLoadURL(String url) {
        return;
    }


}
