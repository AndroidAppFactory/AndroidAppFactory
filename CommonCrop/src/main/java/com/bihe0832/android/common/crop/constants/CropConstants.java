package com.bihe0832.android.common.crop.constants;

import android.graphics.Bitmap;
import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2023/11/13.
 *         Description:
 */
public class CropConstants {

    public static final int GESTURE_TYPES_NONE = 0;
    public static final int GESTURE_TYPES_SCALE = 1;
    public static final int GESTURE_TYPES_ROTATE = 2;
    public static final int GESTURE_TYPES_ALL = 3;

    @IntDef({GESTURE_TYPES_NONE, GESTURE_TYPES_SCALE, GESTURE_TYPES_ROTATE, GESTURE_TYPES_ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GestureTypes {

    }


    public static final int DEFAULT_COMPRESS_QUALITY = 90;
    public static final int MIN_SIZE = 10;
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;

}
