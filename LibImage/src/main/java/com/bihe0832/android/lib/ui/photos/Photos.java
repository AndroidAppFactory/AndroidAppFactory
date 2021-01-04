package com.bihe0832.android.lib.ui.photos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-12-17.
 * Description: Description
 */
public class Photos {
    public static void addPicToPhotos(Context context, String filePath){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), filePath, "", "");
        }catch (Exception e){
            e.printStackTrace();
        }

        Uri uri = Uri.parse(filePath);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }
}
