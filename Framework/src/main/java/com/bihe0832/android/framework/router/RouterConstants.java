package com.bihe0832.android.framework.router;


import com.bihe0832.android.lib.file.select.FileSelectTools;

/**
 * Created by hardyshi on 2017/10/25.
 */

public class RouterConstants {

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
}
