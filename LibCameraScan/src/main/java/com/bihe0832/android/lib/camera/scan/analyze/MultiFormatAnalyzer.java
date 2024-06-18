package com.bihe0832.android.lib.camera.scan.analyze;

import androidx.annotation.Nullable;
import com.bihe0832.android.lib.camera.scan.config.DecodeConfig;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.qrcode.QRCodeDecodingHandler;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import java.util.Map;

public class MultiFormatAnalyzer extends AreaRectAnalyzer {

    MultiFormatReader mReader;

    public MultiFormatAnalyzer() {
        this((DecodeConfig) null);
    }

    public MultiFormatAnalyzer(@Nullable Map<DecodeHintType, Object> hints) {
        this(new DecodeConfig().setHints(hints));
    }

    public MultiFormatAnalyzer(@Nullable DecodeConfig config) {
        super(config);
        initReader();
    }

    private void initReader() {
        mReader = new MultiFormatReader();
    }

    @Nullable
    @Override
    public Result analyze(byte[] data, int dataWidth, int dataHeight, int left, int top, int width, int height) {
        Result rawResult = null;
        try {
            long start = System.currentTimeMillis();
            mReader.setHints(mHints);
            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, dataWidth, dataHeight, left, top,
                    width, height, false);

            rawResult = QRCodeDecodingHandler.decodeCode(mReader, source, isMultiDecode);
            long end = System.currentTimeMillis();
            ZLog.d("Found barcode in " + (end - start) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mReader.reset();
        }
        return rawResult;
    }
}
