package com.bihe0832.android.lib.qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.bihe0832.android.lib.media.image.BitmapUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
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
        return decodeQRcode(getRGBLuminanceSource(bitmap), HINTS);
    }

    public static Result decodeQRcode(Bitmap bitmap, Map<DecodeHintType, Object> hints) {
        try {
            return decodeQRcode(getRGBLuminanceSource(bitmap), hints);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Result decodeQRcode(LuminanceSource source, Map<DecodeHintType, Object> hints) {
        Result result = null;
        MultiFormatReader reader = new MultiFormatReader();
        try {
            reader.setHints(hints);
            result = decodeQRcode(reader, source);
            if (result == null) {
                result = decodeQRcode(reader, source.invert());
            }
            if (result == null && source.isRotateSupported()) {
                result = decodeQRcode(reader, source.rotateCounterClockwise());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.reset();
        }
        return result;
    }

    private static Result decodeQRcode(MultiFormatReader reader, LuminanceSource source) {
        Result result = null;
        try {
            try {
                //采用HybridBinarizer解析
                result = reader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
            } catch (Exception e) {

            }
            if (result == null) {
                //如果没有解析成功，再采用GlobalHistogramBinarizer解析一次
                result = reader.decodeWithState(new BinaryBitmap(new GlobalHistogramBinarizer(source)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取RGBLuminanceSource
     *
     * @param bitmap
     * @return
     */
    private static RGBLuminanceSource getRGBLuminanceSource(@NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        return new RGBLuminanceSource(width, height, pixels);

    }
}
