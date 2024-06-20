package com.bihe0832.android.common.qrcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.fragment.app.Fragment;
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode;
import com.bihe0832.android.framework.router.RouterAction;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.lib.qrcode.QRCodeDecodingHandler;
import com.bihe0832.android.lib.qrcode.QRCodeEncodingHandler;
import com.google.zxing.Result;
import java.util.HashMap;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2023/4/7.
 *         Description: Description
 */
public class QrcodeUtils {

    public static final void openQrScan(Activity activity) {
        openQrScan(activity, true, true, true, false);
    }

    public static final void openQrScan(Fragment fragment) {
        openQrScan(fragment, true, true, true, false);
    }

    public static final void openQrScanAndParse() {
        openQrScanAndParse(true, true, true, true);
    }

    public static final void openQrScan(Activity activity, boolean needSound, boolean needVibrate, boolean onlyQRCode,
            boolean autoZoom) {
        HashMap dataParam = new HashMap();
        dataParam.put(RouterConstants.INTENT_EXTRA_KEY_QRCODE_SCAN_SOUND, String.valueOf(needSound));
        dataParam.put(RouterConstants.INTENT_EXTRA_KEY_QRCODE_SCAN_VIBRATE, String.valueOf(needVibrate));
        dataParam.put(RouterConstants.INTENT_EXTRA_KEY_QRCODE_ONLY, String.valueOf(onlyQRCode));
        dataParam.put(RouterConstants.INTENT_EXTRA_KEY_AUTO_ZOOM, String.valueOf(autoZoom));

        RouterAction.INSTANCE.openForResult(activity,
                RouterAction.INSTANCE.getFinalURL(RouterConstants.MODULE_NAME_QRCODE_SCAN, dataParam),
                ZixieActivityRequestCode.QRCODE_SCAN);
    }

    public static final void openQrScan(Fragment fragment, boolean needSound, boolean needVibrate, boolean onlyQRCode,
            boolean autoZoom) {
        HashMap dataParam = new HashMap();
        dataParam.put(RouterConstants.INTENT_EXTRA_KEY_QRCODE_SCAN_SOUND, String.valueOf(needSound));
        dataParam.put(RouterConstants.INTENT_EXTRA_KEY_QRCODE_SCAN_VIBRATE, String.valueOf(needVibrate));
        dataParam.put(RouterConstants.INTENT_EXTRA_KEY_QRCODE_ONLY, String.valueOf(onlyQRCode));
        dataParam.put(RouterConstants.INTENT_EXTRA_KEY_AUTO_ZOOM, String.valueOf(autoZoom));

        RouterAction.INSTANCE.openForResult(fragment,
                RouterAction.INSTANCE.getFinalURL(RouterConstants.MODULE_NAME_QRCODE_SCAN, dataParam),
                ZixieActivityRequestCode.QRCODE_SCAN);
    }

    public static final void openQrScanAndParse(boolean needSound, boolean needVibrate, boolean onlyQRCode,
            boolean autoZoom) {
        HashMap dataParam = new HashMap();
        dataParam.put(RouterConstants.INTENT_EXTRA_KEY_QRCODE_SCAN_SOUND, String.valueOf(needSound));
        dataParam.put(RouterConstants.INTENT_EXTRA_KEY_QRCODE_SCAN_VIBRATE, String.valueOf(needVibrate));
        dataParam.put(RouterConstants.INTENT_EXTRA_KEY_QRCODE_ONLY, String.valueOf(onlyQRCode));
        dataParam.put(RouterConstants.INTENT_EXTRA_KEY_AUTO_ZOOM, String.valueOf(autoZoom));
        RouterAction.INSTANCE.openPageByRouter(RouterConstants.MODULE_NAME_QRCODE_SCAN_AND_PARSE, dataParam,
                Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static final Result decodeQRcode(String filePath) {
        return QRCodeDecodingHandler.decodeCode(filePath, 500, 500);
    }

    public static final Result decodeQRcode(String filePath, int width) {
        return QRCodeDecodingHandler.decodeCode(filePath, width, width);
    }


    public static final Result decodeQRcode(Context context, Uri uri, int width) {
        return QRCodeDecodingHandler.decodeCode(context, uri, width, width);
    }

    public static final Result decodeQRcode(Bitmap bitmap) {
        return QRCodeDecodingHandler.decodeCode(bitmap);
    }

    public static final Bitmap createQRCode(String str, int widthAndHeight) {
        return QRCodeEncodingHandler.createQRCode(str, widthAndHeight);
    }


    public static final Bitmap createQRCode(String content, int widthPix, int heightPix, Bitmap logoBm) {
        return QRCodeEncodingHandler.createQRCode(content, widthPix, heightPix, logoBm);
    }
}
