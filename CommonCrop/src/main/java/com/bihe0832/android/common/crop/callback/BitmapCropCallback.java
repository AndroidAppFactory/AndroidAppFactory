package com.bihe0832.android.common.crop.callback;

import android.net.Uri;
import androidx.annotation.NonNull;

public interface BitmapCropCallback {

    void onBitmapCropped(@NonNull Uri resultUri, int offsetX, int offsetY, int imageWidth, int imageHeight);

    void onCropFailure(@NonNull Throwable t);

}