package com.bihe0832.android.lib.camera.scan.analyze;

import androidx.annotation.Nullable;
import com.bihe0832.android.lib.camera.scan.config.DecodeConfig;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.qrcode.QRCodeDecodingHandler;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.qrcode.QRCodeReader;
import java.util.Map;

public abstract class BarcodeFormatAnalyzer extends AreaRectAnalyzer {

    private Reader mReader;

    public BarcodeFormatAnalyzer(@Nullable Map<DecodeHintType, Object> hints) {
        this(new DecodeConfig().setHints(hints));
    }

    public BarcodeFormatAnalyzer(@Nullable DecodeConfig config) {
        super(config);
        initReader();
    }

    private void initReader() {
        mReader = createReader();
    }

    @Nullable
    @Override
    public Result analyze(byte[] data, int dataWidth, int dataHeight, int left, int top, int width, int height) {
        Result rawResult = null;
        if (mReader != null) {
            try {
                long start = System.currentTimeMillis();
                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, dataWidth, dataHeight, left, top,
                        width, height, false);
                rawResult = decodeInternal(source, isMultiDecode);
                if (rawResult == null && mDecodeConfig != null) {
                    if (mDecodeConfig.isSupportVerticalCode()) {
                        byte[] rotatedData = new byte[data.length];
                        for (int y = 0; y < dataHeight; y++) {
                            for (int x = 0; x < dataWidth; x++) {
                                rotatedData[x * dataHeight + dataHeight - y - 1] = data[x + y * dataWidth];
                            }
                        }
                        PlanarYUVLuminanceSource tempSource = new PlanarYUVLuminanceSource(rotatedData, dataHeight,
                                dataWidth, top, left, height, width, false);
                        rawResult = decodeInternal(tempSource, mDecodeConfig.isSupportVerticalCodeMultiDecode());
                    }

                    if (rawResult == null && mDecodeConfig.isSupportLuminanceInvert()) {
                        rawResult = decodeInternal(source.invert(),
                                mDecodeConfig.isSupportLuminanceInvertMultiDecode());
                    }

                    if (rawResult == null && source.isRotateSupported()) {
                        rawResult = decodeInternal(source.rotateCounterClockwise(), isMultiDecode);
                    }
                }
                if (rawResult != null) {
                    long end = System.currentTimeMillis();
                    ZLog.d("Found barcode in " + (end - start) + " ms");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mReader.reset();
            }
        }
        return rawResult;
    }

    private Result decodeInternal(LuminanceSource source, boolean isMultiDecode) {
        Result rawResult = null;
        try {
            if (mReader instanceof QRCodeReader) {
                rawResult = QRCodeDecodingHandler.decodeOnce((QRCodeReader) mReader, source, isMultiDecode);
            } else {
                rawResult = QRCodeDecodingHandler.decodeOnce((MultiFormatReader) mReader, source, isMultiDecode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rawResult;
    }

    public abstract Reader createReader();

}
