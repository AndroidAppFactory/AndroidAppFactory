package com.bihe0832.android.lib.camera.scan.analyze;

import android.content.res.Configuration;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageProxy;
import com.google.zxing.Result;

/**
 * 分析器
 */
public interface Analyzer {

    /**
     * Analyzes an image to produce a result.
     *
     * @param image The image to analyze
     * @param orientation {@link Configuration#ORIENTATION_LANDSCAPE},
     *         {@link Configuration#ORIENTATION_PORTRAIT}.
     * @return
     */
    @Nullable
    Result analyze(@NonNull ImageProxy image, int orientation);
}
