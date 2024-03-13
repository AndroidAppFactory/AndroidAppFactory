package com.bihe0832.android.lib.qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import com.bihe0832.android.lib.media.image.BitmapUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2023/4/6.
 *         Description: Description
 */
public final class QRCodeDecodingHandler {
    public static final Map<DecodeHintType, Object> HINTS = new EnumMap<>(DecodeHintType.class);

    static {
        List<BarcodeFormat> allFormats = new ArrayList<>();
        allFormats.add(BarcodeFormat.AZTEC);
        allFormats.add(BarcodeFormat.CODABAR);
        allFormats.add(BarcodeFormat.CODE_39);
        allFormats.add(BarcodeFormat.CODE_93);
        allFormats.add(BarcodeFormat.CODE_128);
        allFormats.add(BarcodeFormat.DATA_MATRIX);
        allFormats.add(BarcodeFormat.EAN_8);
        allFormats.add(BarcodeFormat.EAN_13);
        allFormats.add(BarcodeFormat.ITF);
        allFormats.add(BarcodeFormat.MAXICODE);
        allFormats.add(BarcodeFormat.PDF_417);
        allFormats.add(BarcodeFormat.QR_CODE);
        allFormats.add(BarcodeFormat.RSS_14);
        allFormats.add(BarcodeFormat.RSS_EXPANDED);
        allFormats.add(BarcodeFormat.UPC_A);
        allFormats.add(BarcodeFormat.UPC_E);
        allFormats.add(BarcodeFormat.UPC_EAN_EXTENSION);
        HINTS.put(DecodeHintType.TRY_HARDER, BarcodeFormat.QR_CODE);
        HINTS.put(DecodeHintType.POSSIBLE_FORMATS, allFormats);
        HINTS.put(DecodeHintType.CHARACTER_SET, "UTF-8");
    }

    public static Result decodeQRcode(String filePath, int width) {
        return decodeQRcode(BitmapUtil.getLocalBitmap(filePath, width, width));
    }

    public static Result decodeQRcode(Context context, Uri uri, int width) {
        return decodeQRcode(BitmapUtil.getLocalBitmap(context, uri, width, width));
    }

    public static Result decodeQRcode(Bitmap bitmap) {
        com.google.zxing.RGBLuminanceSource source = null;
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            source = new com.google.zxing.RGBLuminanceSource(width, height, pixels);
            return new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(source)), HINTS);
        } catch (Exception e) {
            e.printStackTrace();
            if (source != null) {
                try {
                    return new MultiFormatReader().decode(new BinaryBitmap(new GlobalHistogramBinarizer(source)),
                            HINTS);
                } catch (Throwable e2) {
                    e2.printStackTrace();
                }
            }
            return null;
        }
    }
}
