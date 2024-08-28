package com.bihe0832.android.lib.audio.wav;

import android.media.AudioRecord;
import com.bihe0832.android.lib.audio.AudioRecordConfig;
import com.bihe0832.android.lib.log.ZLog;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @describe: 音频 pcm 转 wav 格式 工具
 */
public class PcmToWav {

    private final AudioRecordConfig mAudioRecordConfig;


    /**
     * pcm 转 wav 格式
     *
     * @param sampleRate sample rate、采样率
     * @param channel channel、声道
     * @param audioFormat Audio data format、音频格式
     */
    public PcmToWav(int sampleRate, int channel, int audioFormat) {
        mAudioRecordConfig = new AudioRecordConfig();
        mAudioRecordConfig.setSampleRateInHz(sampleRate);
        mAudioRecordConfig.setChannelConfig(channel);
        mAudioRecordConfig.setAudioFormat(audioFormat);
        ZLog.d("PcmToWav: 采样率:" + mAudioRecordConfig.getSampleRateInHz() + ";声道数:"
                + mAudioRecordConfig.getChannelInInt() + ";编码长度:" + mAudioRecordConfig.bitsPerSample());
    }

    public PcmToWav(AudioRecordConfig config) {
        mAudioRecordConfig = config;
        ZLog.d("PcmToWav: 采样率:" + mAudioRecordConfig.getSampleRateInHz() + ";声道数:"
                + mAudioRecordConfig.getChannelInInt() + ";编码长度:" + mAudioRecordConfig.bitsPerSample());

    }


    /**
     * pcm文件转wav文件
     *
     * @param inFilename 源文件路径
     * @param outFilename 目标文件路径
     */
    public void convertToFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen;
        int mBufferSize = AudioRecord.getMinBufferSize(mAudioRecordConfig.getSampleRateInHz(),
                mAudioRecordConfig.getChannelConfig(), mAudioRecordConfig.getAudioFormat());
        byte[] data = new byte[mBufferSize];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            WavHeader wavHeader = new WavHeader(mAudioRecordConfig, totalAudioLen);
            out.write(wavHeader.toBytes());
            while (in.read(data) != -1) {
                out.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (null != out) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void convertToFile(byte[] pcmData, String outFilename) {
        FileOutputStream out = null;
        long totalAudioLen;
        try {
            out = new FileOutputStream(outFilename);
            totalAudioLen = pcmData.length;
            WavHeader wavHeader = new WavHeader(mAudioRecordConfig, totalAudioLen);
            out.write(wavHeader.toBytes());
            out.write(pcmData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            WaveFileReader reader = new WaveFileReader(outFilename);
            ZLog.d(reader.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
