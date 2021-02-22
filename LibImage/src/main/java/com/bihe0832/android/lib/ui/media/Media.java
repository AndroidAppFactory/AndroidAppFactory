package com.bihe0832.android.lib.ui.media;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;

import com.bihe0832.android.lib.file.FileUtils;

import java.io.File;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-12-17.
 * Description: Description
 */
public class Media {

    public static void addPicToPhotos(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), filePath, "", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri uri = Uri.parse(filePath);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }


    public static boolean addVideoToPhotos(Context context, String filePath) {
        File file = new File(filePath);
        if (FileUtils.INSTANCE.checkFileExist(filePath)) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.TITLE, file.getName());
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis());
            values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            values.put(MediaStore.MediaColumns.SIZE, file.length());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "video/*");
            Uri uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            MediaScannerConnection mMediaScanner = new MediaScannerConnection(context, null);
            mMediaScanner.connect();
            if (mMediaScanner != null && mMediaScanner.isConnected()) {
                mMediaScanner.scanFile(file.getAbsolutePath(), "video/*");
            }
            return uri != null;
        } else {
            return false;
        }
    }
}
