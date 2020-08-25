package com.bihe0832.android.lib.superapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.bihe0832.android.lib.utils.intent.IntentUtils;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-12-16.
 * Description: Description
 */
public class QQHelper {
    /**
     * 打开指定的QQ聊天页面
     *
     * @param context 上下文
     * @param QQ      QQ号码
     */
    public static boolean openQQChat(Context context, String QQ) {
        try {
            String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + QQ;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            IntentUtils.startIntent(context, intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    /****************
     *
     * @param key 由官网生成的key https://qun.qq.com/join.html
     *
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public static boolean joinQQGroup(Context context, String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            IntentUtils.startIntent(context, intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }
}
