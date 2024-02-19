package com.bihe0832.android.framework.constant;


import java.nio.charset.Charset;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-11-07.
 *         Description: 管理通用常量
 */
public class Constants {

    //本地配置文件名称，配置文件具体位置在assets
    public static final String CONFIG_COMMON_FILE_NAME = "config.default";
    public static final String CONFIG_SPECIAL_FILE_NAME = "config.others";

    public static final String SYSTEM_CONSTANT = "android";

    public static final String CHAR_SET_NAME = "UTF-8";

    public static final String URL_USER_AGENT_VERSION = "ZixieVersion";
    public static final String URL_USER_AGENT_JS_BRIDGE_VERSION = "JSVersion";

    public static Charset CHAR_SET_UTF8 = Charset.forName(CHAR_SET_NAME);

    public static final float CUSTOM_DENSITY = 360f;

    public static final String CONFIG_KEY_LAYER_START_VALUE =  "com.bihe0832.android.config.layer.gray.start";
    public static final String CONFIG_KEY_LAYER_END_VALUE = "com.bihe0832.android.config.layer.gray.end";


}
