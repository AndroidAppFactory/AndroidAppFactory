package com.bihe0832.android.lib.crop.jni;

import java.io.IOException;

/**
 * Crops part of image that fills the crop bounds.
 * <p/>
 * First image is downscaled if max size was set and if resulting image is larger that max size.
 * Then image is rotated accordingly.
 * Finally new Bitmap object is created and saved to file.
 */
public class CropAction {

    static {
        System.loadLibrary("aafcrop");
    }

    public static native boolean crop(String inputPath, String outputPath,
            int left, int top, int width, int height,
            float angle, float resizeScale,
            int format, int quality,
            int exifDegrees, int exifTranslation) throws IOException, OutOfMemoryError;

}
