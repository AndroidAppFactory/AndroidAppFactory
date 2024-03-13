package com.bihe0832.android.lib.qrcode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Tang
 */
public final class QRCodeEncodingHandler {

	public static final int ERROR_CORRECTION_Level_LENGTH = 200;

	public static Bitmap createQRCode(String str, int widthAndHeight) {
		return createQRCode(str, widthAndHeight, widthAndHeight, null);
	}

	/**
	 * 创建二维码
	 *
	 * @param content   content
	 * @param widthPix  widthPix
	 * @param heightPix heightPix
	 * @param logoBm    logoBm
	 * @return 二维码
	 */
	public static Bitmap createQRCode(String content, int widthPix, int heightPix, Bitmap logoBm) {
		if (content.length() > ERROR_CORRECTION_Level_LENGTH) {
			return createQRCode(content, ErrorCorrectionLevel.M, widthPix, heightPix, logoBm, 7);
		} else {
			return createQRCode(content, ErrorCorrectionLevel.H, widthPix, heightPix, logoBm, 7);
		}
	}

	public static Bitmap createQRCode(String content, ErrorCorrectionLevel errorCorrectionLevel, int widthPix,
			int heightPix, Bitmap logoBm, int percent) {
		try {
			if (content == null || "".equals(content)) {
				return null;
			}
			// 配置参数
			Map<EncodeHintType, Object> hints = new HashMap<>();
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			// 容错级别
			hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
			hints.put(EncodeHintType.MARGIN, 2);
			QRCodeWriter writer = new QRCodeWriter();
			// 图像数据转换，使用了矩阵转换
			BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints);

			int[] pixels = new int[widthPix * heightPix];
			// 下面这里按照二维码的算法，逐个生成二维码的图片，
			// 两个for循环是图片横列扫描的结果
			for (int y = 0; y < heightPix; y++) {
				for (int x = 0; x < widthPix; x++) {
					if (matrix.get(x, y)) {
						pixels[y * widthPix + x] = 0xff000000;
					} else {
						pixels[y * widthPix + x] = 0xffffffff;
					}
				}
			}
			// 生成二维码图片的格式，使用ARGB_8888
			Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);
			if (logoBm != null) {
				bitmap = addLogo(bitmap, logoBm, percent);
			}
			//必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 在二维码中间添加Logo图案
	 */
	private static Bitmap addLogo(Bitmap src, Bitmap logo, int percent) {
		if (src == null) {
			return null;
		}
		if (logo == null) {
			return src;
		}
		//获取图片的宽高
		int srcWidth = src.getWidth();
		int srcHeight = src.getHeight();
		int logoWidth = logo.getWidth();
		int logoHeight = logo.getHeight();
		if (srcWidth == 0 || srcHeight == 0) {
			return null;
		}
		if (logoWidth == 0 || logoHeight == 0) {
			return src;
		}
		//logo大小为二维码整体大小的1/5
		float scaleFactor = srcWidth * 1.0f / percent / logoWidth;
		Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
		try {
			Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(src, 0, 0, null);
			canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
			canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
			canvas.save();
			canvas.restore();
		} catch (Exception e) {
			bitmap = null;
			e.getStackTrace();
		}
		return bitmap;
	}
}
