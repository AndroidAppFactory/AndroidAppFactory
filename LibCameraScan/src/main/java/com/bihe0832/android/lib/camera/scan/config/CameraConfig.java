package com.bihe0832.android.lib.camera.scan.config;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;

/**
 * 相机配置：主要用于提供相机预览时可自定义一些配置，便于扩展
 */
public class CameraConfig {

    public CameraConfig() {

    }

    @NonNull
    public Preview options(@NonNull Preview.Builder builder) {
        return builder.build();
    }

    @NonNull
    public CameraSelector options(@NonNull CameraSelector.Builder builder) {
        return builder.build();
    }

    @NonNull
    public ImageAnalysis options(@NonNull ImageAnalysis.Builder builder) {
        return builder.build();
    }

}
