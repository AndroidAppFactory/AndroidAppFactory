package com.bihe0832.android.framework.router;


import com.bihe0832.android.lib.file.select.FileSelectTools;

/**
 * Created by zixie on 2017/10/25.
 */

public class RouterConstants {

    //内置反馈
    public static final String MODULE_NAME_FEEDBACK = "zfeedback";
    public static final String MODULE_NAME_FEEDBACK_TBS = "ztbsfeedback";

    //内置文本编辑器
    public static final String MODULE_NAME_EDITOR = "zeditor";

    public static final String MODULE_NAME_WEB_PAGE = "zweb";
    public static final String MODULE_NAME_WEB_PAGE_TBS = "ztbsweb";

    //webview参数，要打开的URL
    public static final String INTENT_EXTRA_KEY_WEB_URL = FileSelectTools.INTENT_EXTRA_KEY_WEB_URL;
    //是否隐藏webview的标题栏
    public static final String INTENT_EXTRA_KEY_WEB_TITLE_STATUS = "showtitle";
    //隐藏标题栏
    public static final int INTENT_EXTRA_VALUE_WEB_TITLE_HIDE = 0;
    //展示标题栏
    public static final int INTENT_EXTRA_VALUE_WEB_TITLE_SHOW = 1;

    //webview参数，要跳转的URL
    public static final String INTENT_EXTRA_KEY_WEB_REDIRECT_URL = "redirect";

    public static final String MODULE_NAME_DEBUG = "zdebug";
    public static final String MODULE_NAME_SPLASH = "splash";

    //打开二维码扫描
    public static final String MODULE_NAME_QRCODE_SCAN = "qrscan";
    public static final String INTENT_EXTRA_KEY_QRCODE_SCAN_SOUND = "opensound";
    public static final String INTENT_EXTRA_KEY_QRCODE_SCAN_VIBRATE = "openvibrate";
    public static final String INTENT_EXTRA_KEY_QRCODE_ONLY = "qrcodeOnly";
    public static final String INTENT_EXTRA_KEY_AUTO_ZOOM = "autoZoom";

    // 扫码并且完成解析与跳转
    public static final String MODULE_NAME_QRCODE_SCAN_AND_PARSE = "qrparse";

    //二维码分享的数据
    public static final String MODULE_NAME_SHARE_QRCODE = "zshare";
    // 分享的内容
    public static final String INTENT_EXTRA_KEY_SHARE_DATA_WITH_ENCODE = INTENT_EXTRA_KEY_WEB_URL;
    // 分享的主标题
    public static final String INTENT_EXTRA_KEY_SHARE_TITLE_WITH_ENCODE = "title";
    // 分享的副标题
    public static final String INTENT_EXTRA_KEY_SHARE_DESC_WITH_ENCODE = "desc";
    // 分享APK
    public static final String MODULE_NAME_SHARE_APK = "zshareapk";
    // 是否支持发送APK
    public static final String INTENT_EXTRA_KEY_SHARE_SEND_APK = "apk";

    //日志查看
    public static final String MODULE_NAME_SHOW_LOG_LIST = "loglist";
    public static final String INTENT_EXTRA_KEY_SHOW_LOG_LIST_ACTION = "showAction";
    public static final String MODULE_NAME_SHOW_LOG = "log";
    public static final String INTENT_EXTRA_KEY_SHOW_LOG_SORT = "log.sort";
    public static final String INTENT_EXTRA_KEY_SHOW_LOG_NUM = "log.num";
    public static final String INTENT_EXTRA_KEY_SHOW_LOG_SHOW_LINE = "log.line";
}
