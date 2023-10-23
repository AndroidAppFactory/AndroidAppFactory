package com.bihe0832.android.lib.file.select;

import static com.bihe0832.android.lib.file.mimetype.FileMimeTypesKt.FILE_TYPE_ALL;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import androidx.core.app.ActivityCompat;

/**
 * @author zixie code@bihe0832.com Created on 4/8/21.
 */
public class FileSelectTools {

    public static final int FILE_CHOOSER = 1;
    public static final int FILE_CHOOSER_SYSTEM = 2;

    public static final String INTENT_EXTRA_KEY_WEB_URL = "url";
    public static final String INTENT_EXTRA_KEY_NEED_SDCARD_PERMISSION = "permission";

    public static void openFileSelect(Activity activity, String url, String fileType, boolean needSDCardPermission) {
        try {
            Intent intent = new Intent(activity, FileActivity.class);
            intent.setAction(Intent.ACTION_PICK);
            intent.setType(fileType);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if (!TextUtils.isEmpty(url)) {
                intent.putExtra(INTENT_EXTRA_KEY_WEB_URL, url);
            }
            if (needSDCardPermission) {
                intent.putExtra(INTENT_EXTRA_KEY_NEED_SDCARD_PERMISSION, true);
            }
            ActivityCompat.startActivityForResult(activity, intent, FILE_CHOOSER, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openFileSelect(Activity activity, String url, String fileType) {
        openFileSelect(activity, url, fileType, false);
    }


    public static void openFileSelect(Activity activity, String url) {
        openFileSelect(activity, url, FILE_TYPE_ALL);
    }

    public static void openFileSelect(Activity activity) {
        openFileSelect(activity, "");
    }


    public static void openAndroidFileSelect(Activity activity, String fileType) {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(fileType);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            ActivityCompat.startActivityForResult(activity, intent, FILE_CHOOSER_SYSTEM, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
