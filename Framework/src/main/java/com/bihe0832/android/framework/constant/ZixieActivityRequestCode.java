package com.bihe0832.android.framework.constant;


import com.bihe0832.android.lib.file.select.FileSelectTools;
import com.bihe0832.android.lib.permission.PermissionManager;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-07.
 * Description: 管理所有加速器相关的常量
 */
public class ZixieActivityRequestCode {

    //文件选择

    public static final int PERMISSION_REQUEST_CODE = PermissionManager.PERMISSION_REQUEST_CODE;

    public static final int FILE_CHOOSER = FileSelectTools.FILE_CHOOSER;

    public static final int TAKE_PHOTO = 2;

    public static final int CHOOSE_PHOTO = 3;

    public static final int CROP_PHOTO = 4;

    public static final int QRCODE_SCAN = 5;
    // 二维码扫描结果
    public static final String INTENT_EXTRA_KEY_QR_SCAN = "qr_scan_result";


}
