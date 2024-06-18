package com.bihe0832.android.common.qrcode.core;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import androidx.camera.view.PreviewView;
import com.bihe0832.android.common.photos.PhotoWrapperKt;
import com.bihe0832.android.common.qrcode.R;
import com.bihe0832.android.common.qrcode.view.ViewfinderView;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.lib.camera.scan.CameraScan.OnScanResultCallback;
import com.bihe0832.android.lib.camera.scan.analyze.Analyzer;
import com.bihe0832.android.lib.camera.scan.analyze.QRCodeAnalyzer;
import com.bihe0832.android.lib.camera.scan.config.DecodeConfig;
import com.bihe0832.android.lib.media.image.CheckedEnableImageView;
import com.bihe0832.android.lib.qrcode.QRCodeDecodingHandler;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.ui.dialog.impl.LoadingDialog;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.bihe0832.lib.audio.player.block.AudioPLayerManager;
import com.google.zxing.Result;

public class BaseScanActivity extends BaseCaptureActivity {

    public static final int BITMAP_WIDTH = 600;
    private static final long VIBRATE_DURATION = 300L;
    private static final float BEEP_VOLUME = 0.80f;
    private LoadingDialog mLoading = null;
    private AudioPLayerManager blockAudioPlayerManager = null;

    protected boolean opensound = true;
    protected boolean openvibrate = true;
    protected boolean onlyQRCode = false;

    @Override
    public int getLayoutId() {
        return R.layout.common_bihe0832_qrcode_activity_scanner;
    }

    @Override
    public ViewfinderView getViewfinderView() {
        return findViewById(R.id.common_qrcode_finder_View);
    }

    @Override
    public PreviewView getPreviewView() {
        return findViewById(R.id.common_qrcode_preview);
    }

    @Override
    public CheckedEnableImageView getFlashlightView() {
        return findViewById(R.id.common_qrcode_flash);
    }

    public CheckedEnableImageView getAlbumView() {
        return findViewById(R.id.common_qrcode_album);
    }

    public View getBackView() {
        return findViewById(R.id.common_qrcode_scanner_back);
    }

    @Override
    public OnScanResultCallback getOnScanResultCallback() {
        return new OnScanResultCallback() {
            @Override
            public void onScanResultCallback(Result result) {
                if (result != null) {
                    getCameraScan().setAnalyzeImage(false);
                    handleDecode(result);
                }
            }

            @Override
            public void onScanResultFailure() {

            }
        };
    }

    protected Analyzer createAnalyzer() {
        if (onlyQRCode) {
            //初始化解码配置
            DecodeConfig decodeConfig = createDecodeConfig();
            return new QRCodeAnalyzer(decodeConfig);
        } else {
            return super.createAnalyzer();
        }
    }


    @Override
    public void initUI() {
        initData(getIntent());
        super.initUI();
        View backView = getBackView();
        if (backView != null) {
            backView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBack();
                }
            });
        }

        if (opensound) {
            blockAudioPlayerManager = new AudioPLayerManager();
        }
        CheckedEnableImageView btnAlbum = getAlbumView();
        if (btnAlbum != null) {
            initAlbumAction(btnAlbum);
        }
    }

    private void initData(Intent intent) {
        if (intent.hasExtra(RouterConstants.INTENT_EXTRA_KEY_QRCODE_SCAN_SOUND)) {
            opensound = ConvertUtils.parseBoolean(
                    intent.getStringExtra(RouterConstants.INTENT_EXTRA_KEY_QRCODE_SCAN_SOUND), opensound);
        }
        if (intent.hasExtra(RouterConstants.INTENT_EXTRA_KEY_QRCODE_SCAN_VIBRATE)) {
            openvibrate = ConvertUtils.parseBoolean(
                    intent.getStringExtra(RouterConstants.INTENT_EXTRA_KEY_QRCODE_SCAN_SOUND), openvibrate);
        }

        if (intent.hasExtra(RouterConstants.INTENT_EXTRA_KEY_QRCODE_ONLY)) {
            onlyQRCode = ConvertUtils.parseBoolean(
                    intent.getStringExtra(RouterConstants.INTENT_EXTRA_KEY_QRCODE_ONLY), onlyQRCode);
        }
    }

    protected void initAlbumAction(CheckedEnableImageView btnAlbum) {
        btnAlbum.setOnClickListener(view -> {
            PhotoWrapperKt.getPhotoContent(this);
        });
    }

    @Override
    public void onBack() {
        handleDecode(null);
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
            if (requestCode == ZixieActivityRequestCode.CHOOSE_PHOTO) {
                handleAlbumPic(data);
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
            Result result = QRCodeDecodingHandler.decodeCode(this, uri, BITMAP_WIDTH, BITMAP_WIDTH);
            mLoading.dismiss();
            if (result != null) {
                handleDecode(result);
            } else {
                ZixieContext.INSTANCE.showToast("识别失败");
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mLoading != null) {
            mLoading.dismiss();
            mLoading = null;
        }
        super.onDestroy();
    }

    protected void playBeepSoundAndVibrate() {
        if (opensound && blockAudioPlayerManager != null) {
            blockAudioPlayerManager.play(this, BEEP_VOLUME, R.raw.beep);
        }

        if (openvibrate) {

            Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(VIBRATE_DURATION);
            }
        }
    }
}
