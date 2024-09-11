package com.bihe0832.android.lib.audio.record.wrapper;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import com.bihe0832.android.lib.audio.AudioRecordConfig;
import com.bihe0832.android.lib.audio.record.AudioRecordManager;
import com.bihe0832.android.lib.audio.record.core.AudioChunk;
import com.bihe0832.android.lib.permission.PermissionManager;
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2;
import java.util.Collections;
import java.util.List;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2024/9/6.
 *         Description:
 */
public class AAFAudioTools {

    public static final List<String> PERMISSION_LIST = Collections.singletonList(permission.RECORD_AUDIO);

    public static final String PERMISSION_GROUP_ID = Manifest.permission.RECORD_AUDIO;

    @SuppressLint("MissingPermission")
    public static void init() {
        AudioRecordManager.INSTANCE.init(AudioRecordConfig.DEFAULT_AUDIO_SOURCE,
                AudioRecordConfig.DEFAULT_SAMPLE_RATE_IN_HZ, AudioRecordConfig.DEFAULT_CHANNEL_CONFIG,
                AudioRecordConfig.DEFAULT_AUDIO_FORMAT, 0.1F);
    }

    public static void addRecordScene(String scene, String permissionDesc, String permissionSceneDesc) {
        PermissionManager.INSTANCE.addPermissionGroup(scene, PERMISSION_GROUP_ID, PERMISSION_LIST);
        PermissionManager.INSTANCE.addPermissionGroupDesc(scene, PERMISSION_GROUP_ID, permissionDesc);
        PermissionManager.INSTANCE.addPermissionGroupScene(scene, PERMISSION_GROUP_ID, permissionSceneDesc);
    }

    public static boolean startRecord(Activity activity, final String scene,
            final AudioChunk.OnAudioChunkPulledListener listener) {
        return AudioRecordManager.INSTANCE.startRecord(activity, scene,
                PermissionManager.INSTANCE.getPermissionScene(scene, PERMISSION_GROUP_ID, false, false), listener);
    }

    public static void startRecordPermissionCheck(Activity activity, String scene,
            PermissionManager.OnPermissionResult listener) {
        PermissionManager.INSTANCE.checkPermission(activity, scene, true, PermissionsActivityV2.class, listener,
                PermissionManager.INSTANCE.getPermissionsByGroupID(scene, PERMISSION_GROUP_ID));
    }

    public boolean startRecord(String scene, AudioChunk.OnAudioChunkPulledListener listener) {
        return AudioRecordManager.INSTANCE.startRecord(scene, listener);
    }

    public void stopRecord(Context context, String scene) {
        AudioRecordManager.INSTANCE.stopRecord(context, scene);
    }
}
