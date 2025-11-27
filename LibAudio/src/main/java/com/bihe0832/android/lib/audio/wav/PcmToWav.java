package com.bihe0832.android.lib.audio.wav;

import android.media.AudioRecord;
import com.bihe0832.android.lib.audio.AudioRecordConfig;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.log.ZLog;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * PCM 转 WAV 格式工具类
 *
 * 该类用于将原始的 PCM 音频数据转换为带文件头的 WAV 格式文件。
 * WAV 文件 = WAV 文件头（44字节） + PCM 音频数据
 *
 * 支持两种转换方式：
 * 1. 从 PCM 文件转换为 WAV 文件
 * 2. 从 PCM 字节数组转换为 WAV 文件
 *
 * @author zixie code@bihe0832.com
 * @describe PCM 转 WAV 格式工具类
 */
public class PcmToWav {

    /** 录音参数配置 */
    private final AudioRecordConfig mAudioRecordConfig;

    /**
     * 构造函数：使用单独的参数初始化
     *
     * @param sampleRate 采样率（Hz），常用值：8000、16000、44100
     * @param channels 声道数：1=单声道，2=立体声
     * @param audioFormat 音频格式，参见 {@link android.media.AudioFormat#ENCODING_PCM_16BIT}
     */
    public PcmToWav(int sampleRate, int channels, int audioFormat) {
        mAudioRecordConfig = new AudioRecordConfig();
        mAudioRecordConfig.setSampleRateInHz(sampleRate);
        mAudioRecordConfig.setChannels(channels);
        mAudioRecordConfig.setAudioFormat(audioFormat);
        ZLog.d(AudioRecordConfig.TAG, "PcmToWav 初始化: 采样率=" + mAudioRecordConfig.getSampleRateInHz()
                + ", 声道数=" + mAudioRecordConfig.getChannels()
                + ", 编码长度=" + mAudioRecordConfig.bitsPerSample() + "bit");
    }

    /**
     * 构造函数：使用配置对象初始化
     *
     * @param config 录音参数配置对象
     * @throws IllegalArgumentException 如果 config 为 null
     */
    public PcmToWav(AudioRecordConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("AudioRecordConfig cannot be null");
        }
        mAudioRecordConfig = config;
        ZLog.d(AudioRecordConfig.TAG, "PcmToWav 初始化: 采样率=" + mAudioRecordConfig.getSampleRateInHz()
                + ", 声道数=" + mAudioRecordConfig.getChannels()
                + ", 编码长度=" + mAudioRecordConfig.bitsPerSample() + "bit");
    }


    /**
     * 将 PCM 文件转换为 WAV 文件
     *
     * 该方法会读取源 PCM 文件，添加 WAV 文件头后写入目标文件。
     *
     * @param inFilename 源 PCM 文件的完整路径
     * @param outFilename 目标 WAV 文件的完整路径
     */
    public void convertToFile(String inFilename, String outFilename) {
        // 参数校验
        if (!FileUtils.INSTANCE.checkFileExist(inFilename)) {
            ZLog.e(AudioRecordConfig.TAG, "convertToFile: input file not exist: " + inFilename);
            return;
        }

        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen;
        // 直接使用配置对象中的 channelConfig
        int mBufferSize = AudioRecord.getMinBufferSize(mAudioRecordConfig.getSampleRateInHz(),
                mAudioRecordConfig.getChannelConfig(), mAudioRecordConfig.getAudioFormat());
        byte[] data = new byte[mBufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            // 获取 PCM 数据总长度
            totalAudioLen = in.getChannel().size();
            // 生成 WAV 文件头
            WavHeader wavHeader = new WavHeader(mAudioRecordConfig, totalAudioLen);
            // 先写入文件头
            out.write(wavHeader.toBytes());
            // 再写入 PCM 数据
            int readCount;
            while ((readCount = in.read(data)) != -1) {
                out.write(data, 0, readCount);
            }
            ZLog.d(AudioRecordConfig.TAG, "convertToFile success: " + outFilename);
        } catch (IOException e) {
            ZLog.e(AudioRecordConfig.TAG, "convertToFile error: " + e.getMessage());
        } finally {
            // 关闭输入流
            if (null != in) {
                try {
                    in.close();
                } catch (Exception e) {
                    ZLog.e(AudioRecordConfig.TAG, "close input stream error: " + e.getMessage());
                }
            }
            // 关闭输出流
            if (null != out) {
                try {
                    out.close();
                } catch (Exception e) {
                    ZLog.e(AudioRecordConfig.TAG, "close output stream error: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 将 PCM 字节数组转换为 WAV 文件
     *
     * 该方法会在 PCM 数据前面添加 WAV 文件头，然后写入目标文件。
     * 转换完成后会验证生成的 WAV 文件是否正确。
     *
     * @param pcmData PCM 音频数据的字节数组
     * @param outFilename 目标 WAV 文件的完整路径
     */
    public void convertToFile(byte[] pcmData, String outFilename) {
        // 参数校验
        if (pcmData == null || pcmData.length == 0) {
            ZLog.e(AudioRecordConfig.TAG, "convertToFile: pcmData is null or empty");
            return;
        }

        FileOutputStream out = null;
        long totalAudioLen;
        try {
            out = new FileOutputStream(outFilename);
            // PCM 数据总长度
            totalAudioLen = pcmData.length;
            // 生成 WAV 文件头
            WavHeader wavHeader = new WavHeader(mAudioRecordConfig, totalAudioLen);
            // 先写入文件头
            out.write(wavHeader.toBytes());
            // 再写入 PCM 数据
            out.write(pcmData);
            ZLog.d(AudioRecordConfig.TAG, "convertToFile success: " + outFilename + ", size=" + pcmData.length);
        } catch (IOException e) {
            ZLog.e(AudioRecordConfig.TAG, "convertToFile error: " + e.getMessage());
        } finally {
            // 关闭输出流
            if (null != out) {
                try {
                    out.close();
                } catch (Exception e) {
                    ZLog.e(AudioRecordConfig.TAG, "close output stream error: " + e.getMessage());
                }
            }
        }

        // 验证生成的 WAV 文件
        try {
            WaveFileReader reader = new WaveFileReader(outFilename);
            if (reader.isSuccess()) {
                ZLog.d(AudioRecordConfig.TAG, "WAV 文件验证成功: " + reader.toString());
            } else {
                ZLog.e(AudioRecordConfig.TAG, "WAV 文件验证失败: " + outFilename);
            }
        } catch (Exception e) {
            ZLog.e(AudioRecordConfig.TAG, "WAV 文件验证异常: " + e.getMessage());
        }
    }
}
