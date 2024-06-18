package com.bihe0832.android.lib.qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.bihe0832.android.lib.media.image.bitmap.BitmapUtil;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import java.util.Map;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2023/4/6.
 *         Description: Description
 */
public final class QRCodeDecodingHandler {

    public static Result decodeCode(String filePath, int width, int height) {
        return decodeCode(BitmapUtil.getLocalBitmap(filePath, width, height));
    }

    public static Result decodeQRCode(String filePath, int width, int height) {
        return decodeQRCode(BitmapUtil.getLocalBitmap(filePath, width, height));
    }

    public static Result decodeCode(Context context, Uri uri, int width, int height) {
        return decodeCode(BitmapUtil.getLocalBitmap(context, uri, width, height));
    }

    public static Result decodeQRCode(Context context, Uri uri, int width, int height) {
        return decodeQRCode(BitmapUtil.getLocalBitmap(context, uri, width, height));
    }

    public static Result decodeCode(Bitmap bitmap) {
        return decodeCode(getRGBLuminanceSource(bitmap), DecodeFormatManager.ALL_HINTS);
    }

    public static Result decodeQRCode(Bitmap bitmap) {
        return decodeQRCode(getRGBLuminanceSource(bitmap));
    }

    public static Result decodeCode(Bitmap bitmap, Map<DecodeHintType, Object> hints) {
        try {
            return decodeCode(getRGBLuminanceSource(bitmap), hints);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Result decodeCode(LuminanceSource source, Map<DecodeHintType, Object> hints) {
        Result result = null;
        MultiFormatReader reader = new MultiFormatReader();
        try {
            reader.setHints(hints);
            result = decodeCode(reader, source, true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.reset();
        }
        return result;
    }

    public static Result decodeQRCode(LuminanceSource source) {
        Result result = null;
        QRCodeReader reader = new QRCodeReader();
        try {
            result = decodeQRCode(reader, source, true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.reset();
        }
        return result;
    }

    public static Result decodeCode(MultiFormatReader reader, LuminanceSource source, boolean isMultiDecode) {
        Result result = null;
        try {
            result = decodeOnce(reader, source, isMultiDecode);
            if (result == null) {
                result = decodeOnce(reader, source.invert(), isMultiDecode);
            }
            if (result == null && source.isRotateSupported()) {
                result = decodeOnce(reader, source.rotateCounterClockwise(), isMultiDecode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.reset();
        }
        return result;
    }

    public static Result decodeQRCode(QRCodeReader reader, LuminanceSource source, boolean isMultiDecode) {
        Result result = null;
        try {
            result = decodeOnce(reader, source, isMultiDecode);
            if (result == null) {
                result = decodeOnce(reader, source.invert(), isMultiDecode);
            }
            if (result == null && source.isRotateSupported()) {
                result = decodeOnce(reader, source.rotateCounterClockwise(), isMultiDecode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.reset();
        }
        return result;
    }

    public static Result decodeOnce(MultiFormatReader reader, LuminanceSource source, boolean isMultiDecode) {
        Result result = null;
        try {
            try {
                //采用HybridBinarizer解析
                result = reader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (isMultiDecode && result == null) {
                //如果没有解析成功，再采用GlobalHistogramBinarizer解析一次
                result = reader.decodeWithState(new BinaryBitmap(new GlobalHistogramBinarizer(source)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Result decodeOnce(QRCodeReader reader, LuminanceSource source, boolean isMultiDecode) {
        Result result = null;
        try {
            try {
                //采用HybridBinarizer解析
                result = reader.decode(new BinaryBitmap(new HybridBinarizer(source)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (isMultiDecode && result == null) {
                //如果没有解析成功，再采用GlobalHistogramBinarizer解析一次
                result = reader.decode(new BinaryBitmap(new GlobalHistogramBinarizer(source)));
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
    public static RGBLuminanceSource getRGBLuminanceSource(@NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        return new RGBLuminanceSource(width, height, pixels);
    }


}
