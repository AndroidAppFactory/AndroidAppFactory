package com.google.zxing.activity;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.bihe0832.android.common.photos.PhotoWrapperKt;
import com.bihe0832.android.common.qrcode.R;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.framework.ui.BaseActivity;
import com.bihe0832.android.lib.media.image.CheckedEnableImageView;
import com.bihe0832.android.lib.qrcode.QRCodeDecodingHandler;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.ui.dialog.LoadingDialog;
import com.bihe0832.lib.audio.player.block.AudioPLayerManager;
import com.google.zxing.Result;
import com.google.zxing.camera.CameraManager;
import com.google.zxing.decoding.CaptureActivityHandler;
import com.google.zxing.decoding.InactivityTimer;
import com.google.zxing.view.ViewfinderView;

import java.io.IOException;

public class BaseCaptureActivity extends BaseActivity implements Callback {

    private static final long VIBRATE_DURATION = 300L;
    private static final float BEEP_VOLUME = 0.80f;

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private LoadingDialog mLoading = null;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private AudioPLayerManager blockAudioPlayerManager = null;

    private boolean opensound = true;
    private boolean openvibrate = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_bihe0832_qrcode_activity_scanner);
        findViewById(R.id.common_qrcode_scanner_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });

        initData(getIntent());
        if (opensound) {
            blockAudioPlayerManager = new AudioPLayerManager();
        }
        initFlashAction();

        CheckedEnableImageView btnAlbum = (CheckedEnableImageView) findViewById(R.id.common_qrcode_album);
        btnAlbum.setColorFilter(Color.WHITE);
        initAlbumAction(btnAlbum);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.common_qrcode_viewfinder_content);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    @Override
    public void onBack() {
        handleDecode(null);
    }

    protected void initFlashAction() {

        CheckedEnableImageView btnFlash = (CheckedEnableImageView) findViewById(R.id.common_qrcode_flash);
        btnFlash.setOnClickListener(view -> {
            try {
                boolean isSuccess = CameraManager.get().setFlashLight(!btnFlash.isChecked());
                if (!isSuccess) {
                    ZixieContext.INSTANCE.showToast("暂时无法开启闪光灯");
                    return;
                }
                btnFlash.setChecked(!btnFlash.isChecked());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 如果需要增加权限判断，可以加在这里
     */
    protected void initAlbumAction(CheckedEnableImageView btnAlbum) {
        btnAlbum.setOnClickListener(view -> {
            PhotoWrapperKt.getPhotoContent(this);
        });
    }

    private void initData(Intent intent) {
        if (intent.hasExtra(RouterConstants.INTENT_EXTRA_VALUE_QRCODE_SCAN_SOUND)) {
            opensound = intent.getBooleanExtra(RouterConstants.INTENT_EXTRA_VALUE_QRCODE_SCAN_SOUND, true);
        }
        if (intent.hasExtra(RouterConstants.INTENT_EXTRA_VALUE_QRCODE_SCAN_VIBRATE)) {
            openvibrate = intent.getBooleanExtra(RouterConstants.INTENT_EXTRA_VALUE_QRCODE_SCAN_SOUND, true);
        }
    }

    public void handleDecode(Result result) {
        if (null != result && !TextUtils.isEmpty(result.getText())) {
            playBeepSoundAndVibrate();
            Intent data = new Intent();
            data.putExtra(ZixieActivityRequestCode.INTENT_EXTRA_KEY_QR_SCAN, result.getText());
            setResult(RESULT_OK, data);
        } else {
            setResult(RESULT_CANCELED);
        }
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                ThreadManager.getInstance().runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }
        }, 300L);
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ZixieActivityRequestCode.CHOOSE_PHOTO:
                    handleAlbumPic(data);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 处理选择的图片
     *
     * @param data
     */
    private void handleAlbumPic(Intent data) {
        //获取选中图片的路径
        final Uri uri = data.getData();
        if (mLoading == null) {
            mLoading = new LoadingDialog(this);
        }
        mLoading.setCanCanceled(false);
        ThreadManager.getInstance().runOnUIThread(() -> {
            mLoading.show("识别中，请稍候...");
            Result result = QRCodeDecodingHandler.decodeQRcode(this, uri, 300);
            mLoading.dismiss();
            if (result != null) {
                handleDecode(result);
            } else {
                ZixieContext.INSTANCE.showToast("识别失败");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startScan();
    }

    /**
     * 如果需要增加权限判断，可以加在这里
     */
    protected void startScan() {
        startScanAction();
    }

    protected void startScanAction() {
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.common_qrcode_scanner_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        if (mLoading != null) {
            mLoading.dismiss();
            mLoading = null;
        }
        super.onDestroy();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, getViewfinderView());
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    protected void playBeepSoundAndVibrate() {
        if (opensound && blockAudioPlayerManager != null) {
            blockAudioPlayerManager.play(this, BEEP_VOLUME, R.raw.beep);
        }

        if (openvibrate) {

            Vibrator vibrator = (Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
            if (vibrator != null){
                vibrator.vibrate(VIBRATE_DURATION);
            }
        }
    }

}