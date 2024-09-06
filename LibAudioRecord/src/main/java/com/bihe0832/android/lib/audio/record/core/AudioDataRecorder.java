package com.bihe0832.android.lib.audio.record.core;

import android.annotation.SuppressLint;
import android.media.AudioRecord;
import androidx.annotation.RequiresPermission;
import com.bihe0832.android.lib.audio.AudioRecordConfig;
import com.bihe0832.android.lib.log.ZLog;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base Recorder (Only record the original audio data.)
 *
 * @author maple
 * @time 2018/4/10.
 */
public class AudioDataRecorder implements Recorder {

    private static final String TAG = "AudioDataRecorder";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AudioChunk.OnAudioChunkPulledListener mOnAudioChunkPulledListener;
    protected AudioRecordConfig config = new AudioRecordConfig();
    protected int bufferSizeInBytes;
    private AudioRecord audioRecord;
    private volatile boolean isRecording = false;
    private float mInterval = 0.1f;


    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    public AudioDataRecorder(AudioRecordConfig config, Float interval,
            AudioChunk.OnAudioChunkPulledListener audioChunkListener)
            throws IllegalArgumentException {
        this.config = config;
        this.mInterval = interval;
        this.mOnAudioChunkPulledListener = audioChunkListener;
        // 计算缓冲区大小
        this.bufferSizeInBytes = AudioRecord.getMinBufferSize(
                config.getSampleRateInHz(),
                config.getChannelConfig(),
                config.getAudioFormat()
        );
        if (audioRecord == null) {
            audioRecord = new AudioRecord(config.getAudioSource(), config.getSampleRateInHz(),
                    config.getChannelConfig(), config.getAudioFormat(), bufferSizeInBytes);
        }
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new IllegalStateException("AudioRecord 初始化失败，请检查是否有RECORD_AUDIO权限。" +
                    "或者使用了系统APP才能用的配置项（MediaRecorder.AudioSource.REMOTE_SUBMIX 等），" +
                    "或者使用了该设备不支持的配置项。");
        }
    }

    public AudioRecordConfig getConfig() {
        return config;
    }

    @Override
    public void startRecord() {
        ZLog.d(TAG, "AudioDataRecorder startRecord");
        if (isRecording) {
            return;
        }
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                startRecordData();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void startRecordData() {
        ZLog.d(TAG, "AudioDataRecorder startRecordData");
        if (audioRecord == null) {
            audioRecord = new AudioRecord(config.getAudioSource(), config.getSampleRateInHz(),
                    config.getChannelConfig(), config.getAudioFormat(), bufferSizeInBytes);
        }
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new IllegalStateException("AudioRecord 初始化失败，请检查是否有RECORD_AUDIO权限。" +
                    "或者使用了系统APP才能用的配置项（MediaRecorder.AudioSource.REMOTE_SUBMIX 等），" +
                    "或者使用了该设备不支持的配置项。");
        }
        try {
            audioRecord.startRecording();
            isRecording = true;
            int bufferSize = (int) (mInterval * this.config.getSampleRateInHz());
            while (isRecording) {
                AudioChunk audioChunk = new AudioChunk.Shorts(new short[bufferSize]);
                int count = audioRecord.read(audioChunk.toShorts(), 0, bufferSize);
                mOnAudioChunkPulledListener.onAudioChunkPulled(config, audioChunk, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pauseRecord() {
        ZLog.d(TAG, "AudioDataRecorder pauseRecord");
        isRecording = false;
    }

    @Override
    public void resumeRecord() {
        ZLog.d(TAG, "AudioDataRecorder resumeRecord");
        startRecord();
    }

    @Override
    public void stopRecord() {
        ZLog.d(TAG, "AudioDataRecorder stopRecord");
        pauseRecord();
        try {
            if (audioRecord != null && audioRecord.getState() != AudioRecord.STATE_UNINITIALIZED) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
