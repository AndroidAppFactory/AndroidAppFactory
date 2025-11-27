package com.bihe0832.android.lib.audio;

import android.media.AudioFormat;
import android.media.MediaRecorder;

/**
 * 录音参数配置类
 *
 * 该类封装了 Android 音频录制的各项参数配置，包括：
 * - 音频源（麦克风、语音识别等）
 * - 采样率（常用 8000/16000/44100 Hz）
 * - 声道数（单声道/立体声）
 * - 音频格式（8bit/16bit/Float）
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/10/28.
 * Description: 录音参数配置类，用于 AudioRecord 和 WAV 文件生成
 */
public class AudioRecordConfig {

    /** 默认音频源：麦克风 */
    public static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    /** 默认采样率：16000 Hz（适用于语音识别） */
    public static final int DEFAULT_SAMPLE_RATE_IN_HZ = 16000;

    /** 默认声道配置：单声道 */
    public static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    /** 默认音频格式：16bit PCM */
    public static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    /** 音频处理相关的日志 TAG */
    public static final String TAG = "Audio";

    /**
     * 音频源，详见 {@link MediaRecorder.AudioSource}
     */
    private int audioSource = DEFAULT_AUDIO_SOURCE;

    /**
     * 采样率 赫兹
     * - 44100Hz 所有设备均可用
     * - 22050Hz  16000Hz  11025Hz
     */
    private int sampleRateInHz = DEFAULT_SAMPLE_RATE_IN_HZ;

    /**
     * 声道配置（AudioFormat 常量，主数据源）
     * 这是声道配置的主数据源，用于调用 Android API（如 AudioRecord.getMinBufferSize）
     * channels 字段会根据此值自动计算得到
     */
    private int channelConfig = DEFAULT_CHANNEL_CONFIG;

    /**
     * 声道数（直接使用整数表示，派生属性）
     * - 1: 单声道
     * - 2: 立体声
     * - 更多声道数根据实际需求
     * 
     * 此字段根据 channelConfig 自动计算得到，保证初始化时就与 channelConfig 同步
     */
    private int channels;

    /**
     * 音频数据格式
     * - {@link AudioFormat#ENCODING_PCM_8BIT},每个样本8位
     * - {@link AudioFormat#ENCODING_PCM_16BIT},每个样本16位，保证所有设备支持
     * - {@link AudioFormat#ENCODING_PCM_FLOAT},每个样本 单精度Float
     */
    private int audioFormat = DEFAULT_AUDIO_FORMAT;


    /**
     * 默认构造函数
     *
     * 使用默认参数初始化：
     * - 音频源：麦克风
     * - 采样率：16000 Hz
     * - 声道：单声道
     * - 格式：16bit PCM
     */
    public AudioRecordConfig() {
        // 根据 channelConfig 初始化 channels
        this.channels = convertChannelConfigToChannels(this.channelConfig);
    }

    /**
     * 带参数的构造函数
     *
     * @param audioSource 音频源，详见 {@link MediaRecorder.AudioSource}
     * @param sampleRateInHz 采样率（Hz），常用值：8000、16000、44100
     * @param channels 声道数：1=单声道，2=立体声
     * @param audioFormat 音频格式，详见 {@link AudioFormat}
     */
    public AudioRecordConfig(int audioSource, int sampleRateInHz, int channels, int audioFormat) {
        this.audioSource = audioSource;
        this.sampleRateInHz = sampleRateInHz;
        this.audioFormat = audioFormat;
        // 使用 setChannels 来同步更新 channelConfig
        setChannels(channels);
    }

    /**
     * 获取每个采样的位数
     *
     * @return 采样位数：8 或 16，默认 16
     */
    public byte bitsPerSample() {
        if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
            return 16;
        } else if (audioFormat == AudioFormat.ENCODING_PCM_8BIT) {
            return 8;
        } else {
            // 默认返回 16bit
            return 16;
        }
    }

    /**
     * 获取声道数
     *
     * @return 声道数：1=单声道，2=立体声
     */
    public int getChannels() {
        return channels;
    }

    /**
     * 设置声道数
     * 
     * 会自动同步更新 channelConfig 字段，用于调用 Android API
     *
     * @param channels 声道数：1=单声道，2=立体声
     */
    public void setChannels(int channels) {
        this.channels = channels;
        // 自动同步 channelConfig
        this.channelConfig = convertChannelsToChannelConfig(channels);
    }

    /**
     * 获取声道配置（AudioFormat 常量）
     * 
     * 用于调用需要 AudioFormat 常量的 Android API（如 AudioRecord.getMinBufferSize）
     *
     * @return AudioFormat 声道配置常量
     */
    public int getChannelConfig() {
        return channelConfig;
    }

    /**
     * 设置声道配置（AudioFormat 常量）
     * 
     * 支持直接设置 AudioFormat 声道配置常量，会自动同步更新 channels 字段
     * 这样可以支持更多的声道配置（如 CHANNEL_IN_FRONT、CHANNEL_IN_LEFT 等）
     *
     * @param channelConfig AudioFormat 声道配置常量
     */
    public void setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
        // 自动同步 channels
        this.channels = convertChannelConfigToChannels(channelConfig);
    }

    /**
     * 将声道数转换为 AudioFormat 声道配置常量
     * 
     * @param channels 声道数
     * @return AudioFormat 声道配置常量
     */
    private int convertChannelsToChannelConfig(int channels) {
        switch (channels) {
            case 1:
                return AudioFormat.CHANNEL_IN_MONO;
            case 2:
                return AudioFormat.CHANNEL_IN_STEREO;
            default:
                // 默认返回单声道
                return AudioFormat.CHANNEL_IN_MONO;
        }
    }

    /**
     * 将 AudioFormat 声道配置常量转换为声道数
     * 
     * 使用位运算计算实际声道数，支持所有 AudioFormat 声道配置
     * 
     * @param channelConfig AudioFormat 声道配置常量
     * @return 声道数
     */
    private int convertChannelConfigToChannels(int channelConfig) {
        // 使用 Integer.bitCount 计算声道数（计算二进制中 1 的个数）
        // CHANNEL_IN_MONO = 0x10 (1个声道)
        // CHANNEL_IN_STEREO = 0xC (2个声道)
        // CHANNEL_IN_FRONT = 0x10 (1个声道)
        return Integer.bitCount(channelConfig);
    }

    /**
     * 获取音频源
     *
     * @return 音频源配置值
     */
    public int getAudioSource() {
        return audioSource;
    }

    /**
     * 设置音频源
     *
     * @param audioSource 音频源，详见 {@link MediaRecorder.AudioSource}
     */
    public void setAudioSource(int audioSource) {
        this.audioSource = audioSource;
    }

    /**
     * 获取采样率
     *
     * @return 采样率（Hz）
     */
    public int getSampleRateInHz() {
        return sampleRateInHz;
    }

    /**
     * 设置采样率
     *
     * @param sampleRateInHz 采样率（Hz），常用值：8000、16000、44100
     */
    public void setSampleRateInHz(int sampleRateInHz) {
        this.sampleRateInHz = sampleRateInHz;
    }

    /**
     * 获取音频格式
     *
     * @return 音频格式配置值
     */
    public int getAudioFormat() {
        return audioFormat;
    }

    /**
     * 设置音频格式
     *
     * @param audioFormat 音频格式，详见 {@link AudioFormat}
     */
    public void setAudioFormat(int audioFormat) {
        this.audioFormat = audioFormat;
    }

    /**
     * 转换为字符串表社
     *
     * @return 包含所有配置参数的字符串
     */
    @Override
    public String toString() {
        return "录音参数配置: \n{" +
                "audioSource=" + audioSource +
                ", sampleRateInHz=" + sampleRateInHz +
                ", channels=" + channels +
                ", audioFormat=" + audioFormat +
                '}';
    }
}
