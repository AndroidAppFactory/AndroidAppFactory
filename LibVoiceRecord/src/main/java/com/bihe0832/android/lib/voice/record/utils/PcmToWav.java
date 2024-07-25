package com.bihe0832.android.lib.voice.record.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @describe: 音频 pcm 转 wav 格式 工具
 */
public class PcmToWav {

    /**
     * 缓存的音频大小
     */
    private final int mBufferSize;
    /**
     * 采样率
     */
    private final int mSampleRate;
    /**
     * 声道数
     */
    private final int mChannel;

    // 音频数据的传输速率，表示每秒钟处理的字节数。它由采样率、声道数和每个采样的字节数（取决于音频编码格式）决定
    private final int byteRate;


    /**
     * pcm 转 wav 格式
     *
     * @param sampleRate sample rate、采样率
     * @param channel channel、声道
     * @param encoding Audio data format、音频格式
     */
    public PcmToWav(int sampleRate, int channel, int encoding) {
        this.mSampleRate = sampleRate;
        this.mChannel = channel;
        this.mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, encoding);
        this.byteRate = mSampleRate * getChannelInInt(mChannel) * getBytesPerSample(encoding);
    }

    public static int getChannelInInt(int channel) {
        return channel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
    }

    private int getBytesPerSample(int audioFormat) {
        switch (audioFormat) {
            case AudioFormat.ENCODING_PCM_8BIT:
                return 1;
            case AudioFormat.ENCODING_PCM_16BIT:
            case AudioFormat.ENCODING_IEC61937:
            case AudioFormat.ENCODING_DEFAULT:
                return 2;
            case AudioFormat.ENCODING_PCM_24BIT_PACKED:
                return 3;
            case AudioFormat.ENCODING_PCM_FLOAT:
            case AudioFormat.ENCODING_PCM_32BIT:
                return 4;
            case AudioFormat.ENCODING_INVALID:
            default:
                throw new IllegalArgumentException("Bad audio format " + audioFormat);
        }
    }

    /**
     * pcm文件转wav文件
     *
     * @param inFilename 源文件路径
     * @param outFilename 目标文件路径
     */
    public void convert(String inFilename, String outFilename) {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;
        byte[] data = new byte[mBufferSize];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            out.write(getWaveFileHeader(totalAudioLen, totalDataLen, mSampleRate, getChannelInInt(mChannel), byteRate));
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void convert(byte[] pcmData, String outFilename) {
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;
        byte[] data = new byte[mBufferSize];
        try {
            out = new FileOutputStream(outFilename);
            totalAudioLen = pcmData.length;
            totalDataLen = totalAudioLen + 36;
            out.write(getWaveFileHeader(totalAudioLen, totalDataLen, mSampleRate, getChannelInInt(mChannel), byteRate));
            out.write(pcmData);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加入wav文件头
     *
     * @return
     */
    public byte[] getWaveFileHeader(long totalAudioLen, long totalDataLen, long longSampleRate, int channels,
            long byteRate) {
        byte[] header = new byte[44];
        // RIFF/WAVE header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        //WAVE
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        // 'fmt ' chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // 4 bytes: size of 'fmt ' chunk
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // format = 1
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // block align
        header[32] = (byte) (2 * 16 / 8);
        header[33] = 0;
        // bits per sample
        header[34] = 16;
        header[35] = 0;
        //data
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        return header;
    }
}
