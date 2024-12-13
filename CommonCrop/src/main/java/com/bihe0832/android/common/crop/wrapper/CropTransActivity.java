package com.bihe0832.android.common.crop.wrapper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bihe0832.android.common.crop.ui.CropActivity;
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode;
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback;


public class CropTransActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Intent intentC = new Intent(this, CropActivity.class);
        intentC.putExtras(bundle);
        startActivityForResult(intentC, ZixieActivityRequestCode.CROP_PHOTO);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AAFDataCallback<Uri> callback = CropWrapper.INSTANCE.getCallBack();
        if (callback != null) {
            if (ZixieActivityRequestCode.CROP_PHOTO == requestCode) {
                if (Activity.RESULT_OK == resultCode) {
                    if (data.getData() != null) {
                        callback.onSuccess(data.getData());
                    } else {
                        callback.onError(-1, String.format("crop data is empty:%s,%s", requestCode, resultCode));
                    }
                } else {
                    callback.onError(resultCode, String.format("crop failed:%s,%s", requestCode, resultCode));
                }
            } else {
                callback.onError(-2, String.format("crop data bad requestCode:%s,%s", requestCode, resultCode));
            }
        }
        finish();
    }
}
