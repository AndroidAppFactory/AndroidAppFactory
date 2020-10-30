package com.bihe0832.android.framework.constant;


/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-07.
 * Description: 管理所有加速器相关的常量
 */
public class Constants {

    //本地配置文件名称，配置文件具体位置在assets
    public static final String CONFIG_COMMON_FILE_NAME = "config.default";
    public static final String CONFIG_SPECIAL_FILE_NAME = "config.others";

    public static final String SYSTEM_CONSTANT = "android";


    public static final int INSTALL_TYPE_NOT_FIRST = 1;
    public static final int INSTALL_TYPE_VERSION_FIRST = 2;
    public static final int INSTALL_TYPE_APP_FIRST = 3;

    public static final String KEY_LAST_INSTALLED_VERSION = "zixieLastInstalledVersion";
    public static final String KEY_LAST_INSTALLED_TIME_VERSION = "zixieVersionInstalledTime";
    public static final String KEY_LAST_INSTALLED_TIME_APP = "zixieAppInstalledTime";

}
