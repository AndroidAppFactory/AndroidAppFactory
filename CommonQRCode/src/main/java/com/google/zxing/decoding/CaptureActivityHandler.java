/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.decoding;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.bihe0832.android.lib.media.Media;
import com.bihe0832.android.lib.media.image.BitmapUtil;
import com.google.zxing.MessageConstants;
import com.google.zxing.Result;
import com.google.zxing.activity.BaseCaptureActivity;
import com.google.zxing.camera.CameraManager;
import com.google.zxing.view.ViewfinderResultPointCallback;
import com.google.zxing.view.ViewfinderView;


/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
public final class CaptureActivityHandler extends Handler {

    private static final String TAG = CaptureActivityHandler.class.getSimpleName();

    private final BaseCaptureActivity activity;
    private final DecodeThread decodeThread;
    private State state;

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    public CaptureActivityHandler(BaseCaptureActivity activity, ViewfinderView viewfinderView) {
        this.activity = activity;
        decodeThread = new DecodeThread(activity, new ViewfinderResultPointCallback(viewfinderView));
        decodeThread.start();
        state = State.SUCCESS;
        // Start ourselves capturing previews and decoding.
        CameraManager.get().startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case MessageConstants.MSG_AUTO_FOCUS:
                //Log.d(TAG, "Got auto-focus message");
                // When one auto focus pass finishes, start another. This is the closest thing to
                // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
                if (state == State.PREVIEW) {
                    CameraManager.get().requestAutoFocus(this, MessageConstants.MSG_AUTO_FOCUS);
                }
                break;
            case MessageConstants.MSG_RESTART_PREVIEW:
                Log.d(TAG, "Got restart preview message");
                restartPreviewAndDecode();
                break;
            case MessageConstants.MSG_DECODE_SUCCEEDED:
                Log.d(TAG, "Got decode succeeded message");
                state = State.SUCCESS;
                Bundle bundle = message.getData();

                /***********************************************************************/
//                Bitmap barcode = bundle == null ? null : (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
//                String path = BitmapUtil.saveBitmap(activity, barcode);
//                Media.addToPhotos(activity, path);
                activity.handleDecode((Result) message.obj);
                /***********************************************************************/
                break;
            case MessageConstants.MSG_DECODE_FAILED:
                // We're decoding as fast as possible, so when one decode fails, start another.
                state = State.PREVIEW;
                CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), MessageConstants.MSG_DECODE);
                break;
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        CameraManager.get().stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), MessageConstants.MSG_QUIT);
        quit.sendToTarget();
        try {
            decodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(MessageConstants.MSG_DECODE_SUCCEEDED);
        removeMessages(MessageConstants.MSG_DECODE_FAILED);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), MessageConstants.MSG_DECODE);
            CameraManager.get().requestAutoFocus(this, MessageConstants.MSG_AUTO_FOCUS);
            activity.drawViewfinder();
        }
    }

}
