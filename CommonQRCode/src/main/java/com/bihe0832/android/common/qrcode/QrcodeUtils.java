package com.bihe0832.android.common.qrcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import com.bihe0832.android.framework.constant.ZixieActivityRequestCode;
import com.bihe0832.android.framework.router.RouterAction;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.lib.qrcode.QRCodeDecodingHandler;
import com.bihe0832.android.lib.qrcode.QRCodeEncodingHandler;
import com.google.zxing.Result;

import java.util.HashMap;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/4/7.
 * Description: Description
 */
public class QrcodeUtils {
    public static final void openQrScan(Activity activity, boolean needSound, boolean needVibrate) {
        HashMap dataParam = new HashMap();
        dataParam.put(RouterConstants.INTENT_EXTRA_VALUE_QRCODE_SCAN_SOUND, String.valueOf(needSound));
        dataParam.put(RouterConstants.INTENT_EXTRA_VALUE_QRCODE_SCAN_VIBRATE, String.valueOf(needVibrate));
        RouterAction.INSTANCE.openForResult(activity, RouterAction.INSTANCE.getFinalURL(RouterConstants.MODULE_NAME_QRCODE_SCAN, dataParam), ZixieActivityRequestCode.QRCODE_SCAN);
    }

    public static final void openQrScanAndParse(boolean needSound, boolean needVibrate) {
        HashMap dataParam = new HashMap();
        dataParam.put(RouterConstants.INTENT_EXTRA_VALUE_QRCODE_SCAN_SOUND, String.valueOf(needSound));
        dataParam.put(RouterConstants.INTENT_EXTRA_VALUE_QRCODE_SCAN_VIBRATE, String.valueOf(needVibrate));
        RouterAction.INSTANCE.openPageByRouter(RouterConstants.MODULE_NAME_QRCODE_SCAN_AND_PARSE, dataParam, Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static final Result decodeQRcode(String filePath) {
        return QRCodeDecodingHandler.decodeQRcode(filePath);
    }

    public static final Result decodeQRcode(String filePath, int width) {
        return QRCodeDecodingHandler.decodeQRcode(filePath, width);
    }


    public static final Result decodeQRcode(Context context, Uri uri, int width) {
        return QRCodeDecodingHandler.decodeQRcode(context, uri, width);
    }

    public static final Result decodeQRcode(Bitmap bitmap) {
        return QRCodeDecodingHandler.decodeQRcode(bitmap);
    }

    public static final Bitmap createQRCode(String str, int widthAndHeight) {
        return QRCodeEncodingHandler.createQRCode(str, widthAndHeight);
    }


    public static final Bitmap createQRCode(String content, int widthPix, int heightPix, Bitmap logoBm) {
        return QRCodeEncodingHandler.createQRCode(content, widthPix, heightPix, logoBm);
    }
}
