package com.bihe0832.android.common.crop;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bihe0832.android.common.crop.constants.CropConstants;
import com.bihe0832.android.common.crop.model.AspectRatio;
import com.bihe0832.android.common.crop.ui.CropActivity;
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;


public class CropUtils {

    private static final String EXTRA_PREFIX = "com.bihe0832.android.common.crop";

    public static final String EXTRA_INPUT_URI = EXTRA_PREFIX + ".InputUri";
    public static final String EXTRA_OUTPUT_URI = EXTRA_PREFIX + ".OutputUri";
    public static final String EXTRA_OUTPUT_CROP_ASPECT_RATIO = EXTRA_PREFIX + ".CropAspectRatio";
    public static final String EXTRA_OUTPUT_IMAGE_WIDTH = EXTRA_PREFIX + ".ImageWidth";
    public static final String EXTRA_OUTPUT_IMAGE_HEIGHT = EXTRA_PREFIX + ".ImageHeight";
    public static final String EXTRA_OUTPUT_OFFSET_X = EXTRA_PREFIX + ".OffsetX";
    public static final String EXTRA_OUTPUT_OFFSET_Y = EXTRA_PREFIX + ".OffsetY";

    /**
     * Retrieve cropped image Uri from the result Intent
     *
     * @param intent crop result intent
     */
    @Nullable
    public static Uri getOutput(Intent intent) {
        return intent.getParcelableExtra(EXTRA_OUTPUT_URI);
    }

    /**
     * Retrieve the width of the cropped image
     *
     * @param intent crop result intent
     */
    public static int getOutputImageWidth(Intent intent) {
        return intent.getIntExtra(EXTRA_OUTPUT_IMAGE_WIDTH, -1);
    }

    /**
     * Retrieve the height of the cropped image
     *
     * @param intent crop result intent
     */
    public static int getOutputImageHeight(Intent intent) {
        return intent.getIntExtra(EXTRA_OUTPUT_IMAGE_HEIGHT, -1);
    }

    /**
     * Retrieve cropped image aspect ratio from the result Intent
     *
     * @param intent crop result intent
     * @return aspect ratio as a floating point value (x:y) - so it will be 1 for 1:1 or 4/3 for 4:3
     */
    public static float getOutputCropAspectRatio(Intent intent) {
        return intent.getFloatExtra(EXTRA_OUTPUT_CROP_ASPECT_RATIO, 0f);
    }


    /**
     * Send the crop Intent from an Activity with a custom request code
     *
     * @param activity Activity to receive result
     * @param requestCode requestCode for result
     */

    public static Intent getCropIntent(Context context, Uri source, Options options, Class<?> cls) {
        Intent cropIntent = new Intent();
        Bundle cropOptionsBundle = new Bundle();
        cropOptionsBundle.putParcelable(EXTRA_INPUT_URI, source);
        if (options != null) {
            cropOptionsBundle.putAll(options.getOptionBundle());
        }

        cropIntent.setClass(context, cls);
        cropIntent.putExtras(cropOptionsBundle);
        return cropIntent;
    }

    public static void startCrop(Activity activity, int requestCode, Uri source, Options options) {
        Intent cropIntent = getCropIntent(activity, source, options, CropActivity.class);
        activity.startActivityForResult(cropIntent, requestCode);
    }

    public static void startCrop(Fragment fragment, int requestCode, Uri source, Options options) {
        Intent cropIntent = getCropIntent(fragment.getContext(), source, options, CropActivity.class);
        fragment.startActivityForResult(cropIntent, requestCode);
    }

    public static void startCrop(Activity activity, Uri source, Options options) {
        startCrop(activity, ZixieActivityRequestCode.CROP_PHOTO, source, options);
    }

    public static void startCrop(Fragment fragment, Uri source, Options options) {
        startCrop(fragment, ZixieActivityRequestCode.CROP_PHOTO, source, options);
    }

    public static void startCrop(Activity activity, Uri source) {
        Options options = new Options();
        options.setAllowedGestures(CropConstants.GESTURE_TYPES_ALL, CropConstants.GESTURE_TYPES_ROTATE,
                CropConstants.GESTURE_TYPES_SCALE);
        startCrop(activity, source, options);
    }

    public static void startCrop(Fragment fragment, Uri source) {
        Options options = new Options();
        options.setAllowedGestures(CropConstants.GESTURE_TYPES_ALL, CropConstants.GESTURE_TYPES_ROTATE,
                CropConstants.GESTURE_TYPES_SCALE);
        startCrop(fragment, source, options);
    }

    public static void startSimpleCrop(Activity activity, Uri source) {
        Options options = new Options();
        options.setHideBottomControls(true);
        startCrop(activity, source, options);
    }

    public static void startSimpleCrop(Fragment fragment, Uri source) {
        Options options = new Options();
        options.setHideBottomControls(true);
        startCrop(fragment, source, options);
    }

    /**
     * Class that helps to setup advanced configs that are not commonly used. Use it with method
     * {@link #withOptions(Options)}
     */
    public static class Options {


        public static final String EXTRA_ASPECT_RATIO_X = EXTRA_PREFIX + ".AspectRatioX";
        public static final String EXTRA_ASPECT_RATIO_Y = EXTRA_PREFIX + ".AspectRatioY";

        public static final String EXTRA_MAX_SIZE_X = EXTRA_PREFIX + ".MaxSizeX";
        public static final String EXTRA_MAX_SIZE_Y = EXTRA_PREFIX + ".MaxSizeY";

        public static final String EXTRA_COMPRESSION_FORMAT_NAME = EXTRA_PREFIX + ".CompressionFormatName";
        public static final String EXTRA_COMPRESSION_QUALITY = EXTRA_PREFIX + ".CompressionQuality";

        public static final String EXTRA_ALLOWED_GESTURES = EXTRA_PREFIX + ".AllowedGestures";

        public static final String EXTRA_MAX_BITMAP_SIZE = EXTRA_PREFIX + ".MaxBitmapSize";
        public static final String EXTRA_MAX_SCALE_MULTIPLIER = EXTRA_PREFIX + ".MaxScaleMultiplier";

        public static final String EXTRA_CIRCLE_DIMMED_LAYER = EXTRA_PREFIX + ".CircleDimmedLayer";

        public static final String EXTRA_SHOW_CROP_FRAME = EXTRA_PREFIX + ".ShowCropFrame";
        public static final String EXTRA_CROP_FRAME_STROKE_WIDTH = EXTRA_PREFIX + ".CropFrameStrokeWidth";

        public static final String EXTRA_SHOW_CROP_GRID = EXTRA_PREFIX + ".ShowCropGrid";
        public static final String EXTRA_CROP_GRID_ROW_COUNT = EXTRA_PREFIX + ".CropGridRowCount";
        public static final String EXTRA_CROP_GRID_COLUMN_COUNT = EXTRA_PREFIX + ".CropGridColumnCount";
        public static final String EXTRA_HIDE_BOTTOM_CONTROLS = EXTRA_PREFIX + ".HideBottomControls";
        public static final String EXTRA_FREE_STYLE_CROP = EXTRA_PREFIX + ".FreeStyleCrop";

        public static final String EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT =
                EXTRA_PREFIX + ".AspectRatioSelectedByDefault";
        public static final String EXTRA_ASPECT_RATIO_OPTIONS = EXTRA_PREFIX + ".AspectRatioOptions";


        private final Bundle mOptionBundle;

        public Options() {
            mOptionBundle = new Bundle();
        }


        public Bundle getOptionBundle() {
            return mOptionBundle;
        }

        /**
         * Set one of {@link Bitmap.CompressFormat} that will be used to save resulting Bitmap.
         */
        public void setCompressionFormat(Bitmap.CompressFormat format) {
            mOptionBundle.putString(EXTRA_COMPRESSION_FORMAT_NAME, format.name());
        }

        /**
         * Set compression quality [0-100] that will be used to save resulting Bitmap.
         */
        public void setCompressionQuality(@IntRange(from = 0) int compressQuality) {
            mOptionBundle.putInt(EXTRA_COMPRESSION_QUALITY, compressQuality);
        }

        /**
         * Choose what set of gestures will be enabled on each tab - if any.
         */
        public void setAllowedGestures(@CropConstants.GestureTypes int tabScale,
                @CropConstants.GestureTypes int tabRotate, @CropConstants.GestureTypes int tabAspectRatio) {
            mOptionBundle.putIntArray(EXTRA_ALLOWED_GESTURES, new int[]{tabScale, tabRotate, tabAspectRatio});
        }

        /**
         * This method sets multiplier that is used to calculate max image scale from min image scale.
         *
         * @param maxScaleMultiplier - (minScale * maxScaleMultiplier) = maxScale
         */
        public void setMaxScaleMultiplier(@FloatRange(from = 1.0, fromInclusive = false) float maxScaleMultiplier) {
            mOptionBundle.putFloat(EXTRA_MAX_SCALE_MULTIPLIER, maxScaleMultiplier);
        }

        /**
         * Setter for max size for both width and height of bitmap that will be decoded from an input Uri and used in
         * the view.
         *
         * @param maxBitmapSize - size in pixels
         */
        public void setMaxBitmapSize(@IntRange(from = CropConstants.MIN_SIZE) int maxBitmapSize) {
            mOptionBundle.putInt(EXTRA_MAX_BITMAP_SIZE, maxBitmapSize);
        }

        /**
         * @param isCircle - set it to true if you want dimmed layer to have an circle inside
         */
        public void setCircleDimmedLayer(boolean isCircle) {
            mOptionBundle.putBoolean(EXTRA_CIRCLE_DIMMED_LAYER, isCircle);
        }

        /**
         * @param show - set to true if you want to see a crop frame rectangle on top of an image
         */
        public void setShowCropFrame(boolean show) {
            mOptionBundle.putBoolean(EXTRA_SHOW_CROP_FRAME, show);
        }

        /**
         * @param width - desired width of crop frame line in pixels
         */
        public void setCropFrameStrokeWidth(@IntRange(from = 0) int width) {
            mOptionBundle.putInt(EXTRA_CROP_FRAME_STROKE_WIDTH, width);
        }

        /**
         * @param show - set to true if you want to see a crop grid/guidelines on top of an image
         */
        public void setShowCropGrid(boolean show) {
            mOptionBundle.putBoolean(EXTRA_SHOW_CROP_GRID, show);
        }

        /**
         * @param count - crop grid rows count.
         */
        public void setCropGridRowCount(@IntRange(from = 0) int count) {
            mOptionBundle.putInt(EXTRA_CROP_GRID_ROW_COUNT, count);
        }

        /**
         * @param count - crop grid columns count.
         */
        public void setCropGridColumnCount(@IntRange(from = 0) int count) {
            mOptionBundle.putInt(EXTRA_CROP_GRID_COLUMN_COUNT, count);
        }

        /**
         * @param hide - set to true to hide the bottom controls (shown by default)
         */
        public void setHideBottomControls(boolean hide) {
            mOptionBundle.putBoolean(EXTRA_HIDE_BOTTOM_CONTROLS, hide);
        }


        public void setFreeStyleCropType(int type) {
            mOptionBundle.putInt(EXTRA_FREE_STYLE_CROP, type);
        }

        /**
         * Pass an ordered list of desired aspect ratios that should be available for a user.
         *
         * @param selectedByDefault - index of aspect ratio option that is selected by default (starts with 0).
         * @param aspectRatio - list of aspect ratio options that are available to user
         */
        public void setAspectRatioOptions(int selectedByDefault, AspectRatio... aspectRatio) {
            if (selectedByDefault >= aspectRatio.length) {
                throw new IllegalArgumentException(String.format(Locale.US,
                        "Index [selectedByDefault = %d] (0-based) cannot be higher or equal than aspect ratio options count [count = %d].",
                        selectedByDefault, aspectRatio.length));
            }
            mOptionBundle.putInt(EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, selectedByDefault);
            mOptionBundle.putParcelableArrayList(EXTRA_ASPECT_RATIO_OPTIONS,
                    new ArrayList<Parcelable>(Arrays.asList(aspectRatio)));
        }

        /**
         * Set an aspect ratio for crop bounds. User won't see the menu with other ratios options.
         *
         * @param x aspect ratio X
         * @param y aspect ratio Y
         */
        public void withAspectRatio(float x, float y) {
            mOptionBundle.putFloat(EXTRA_ASPECT_RATIO_X, x);
            mOptionBundle.putFloat(EXTRA_ASPECT_RATIO_Y, y);
        }

        /**
         * Set an aspect ratio for crop bounds that is evaluated from source image width and height. User won't see the
         * menu with other ratios options.
         */
        public void useSourceImageAspectRatio() {
            withAspectRatio(0, 0);
        }


        /**
         * Set maximum size for result cropped image. Maximum size cannot be less then {@value MIN_SIZE}
         *
         * @param width max cropped image width
         * @param height max cropped image height
         */
        public void withMaxResultSize(@IntRange(from = CropConstants.MIN_SIZE) int width,
                @IntRange(from = CropConstants.MIN_SIZE) int height) {
            if (width < CropConstants.MIN_SIZE) {
                width = CropConstants.MIN_SIZE;
            }

            if (height < CropConstants.MIN_SIZE) {
                height = CropConstants.MIN_SIZE;
            }

            mOptionBundle.putInt(EXTRA_MAX_SIZE_X, width);
            mOptionBundle.putInt(EXTRA_MAX_SIZE_Y, height);
        }

    }

}
