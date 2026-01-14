package com.bihe0832.android.app.router;


import com.bihe0832.android.common.webview.base.BaseWebViewFragment;

/**
 * AAF 路由常量定义
 *
 * 定义应用内所有路由模块的名称常量，用于页面跳转和模块间通信
 * 继承自框架层的 RouterConstants，扩展应用层特有的路由
 *
 * @author zixie code@bihe0832.com
 * Created on 2017/10/25.
 */
public class RouterConstants extends com.bihe0832.android.framework.router.RouterConstants {

    /** Intent 参数：Web 页面 URL */
    public static final String INTENT_EXTRA_KEY_WEB_URL = BaseWebViewFragment.INTENT_KEY_URL;

    /** 路由模块：关于页面 */
    public static final String MODULE_NAME_BASE_ABOUT = "about";

    /** 路由模块：消息中心 */
    public static final String MODULE_NAME_MESSAGE = "message";

    /** 路由模块：多语言设置 */
    public static final String MODULE_NAME_LANGUAGE = "language";
}
