package com.bihe0832.android.lib.audio.record.wrapper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import com.bihe0832.android.lib.audio.record.AudioRecordManager;
import com.bihe0832.android.lib.audio.record.core.AudioChunk;
import com.bihe0832.android.lib.permission.PermissionManager;
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2;
import java.util.Arrays;
import java.util.List;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2024/9/6.
 *         Description:
 */
public class AAFAudioTools {

    public static final List permissionList = Arrays.asList(Manifest.permission.RECORD_AUDIO);

    public static final String permissionGroupID = Manifest.permission.RECORD_AUDIO;
    public static final int audioSource = MediaRecorder.AudioSource.MIC;
    public static final int sampleRateInHz = 16000;
    public static final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    public static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;


    @SuppressLint("MissingPermission")
    public static void init() {
        AudioRecordManager.INSTANCE.init(audioSource, sampleRateInHz, channelConfig, audioFormat, 0.1F);
    }

    public static void addRecordScene(String scene, String permissionDesc, String permissionSceneDesc) {
        PermissionManager.INSTANCE.addPermissionGroup(scene, permissionGroupID, permissionList);
        PermissionManager.INSTANCE.addPermissionGroupDesc(scene, permissionGroupID, permissionDesc);
        PermissionManager.INSTANCE.addPermissionGroupScene(scene, permissionGroupID, permissionSceneDesc);
    }

    public static boolean startRecord(Activity activity, final String scene,
            final AudioChunk.OnAudioChunkPulledListener listener) {
        return AudioRecordManager.INSTANCE.startRecord(activity, scene,
                PermissionManager.INSTANCE.getPermissionScene(scene, permissionGroupID, false, false), listener);
    }

    public static void startRecordPermissionCheck(Activity activity, String scene,
            PermissionManager.OnPermissionResult listener) {
        PermissionManager.INSTANCE.checkPermission(activity, scene, true, PermissionsActivityV2.class, listener,
                PermissionManager.INSTANCE.getPermissionsByGroupID(scene, permissionGroupID));
    }

    public boolean startRecord(String scene, AudioChunk.OnAudioChunkPulledListener listener) {
        return AudioRecordManager.INSTANCE.startRecord(scene, listener);
    }

    public void stopRecord(Context context, String scene) {
        AudioRecordManager.INSTANCE.stopRecord(context, scene);
    }
}
