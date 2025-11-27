package com.bihe0832.android.lib.audio.wav;

import android.media.AudioFormat;
import com.bihe0832.android.lib.audio.AudioRecordConfig;

/**
 * WAV 文件头生成工具类
 *
 * WAV 文件格式结构：
 * 1. RIFF 区块（12 字节）：文件标识和文件大小
 * 2. FORMAT 区块（24 字节）：音频格式信息（采样率、声道数、位深等）
 * 3. DATA 区块（8 字节）：音频数据标识和数据大小
 * 总计：44 字节
 *
 * @author maple
 * @author zixie code@bihe0832.com
 * @time 2018/4/10
 */
public class WavHeader {

    /** WAV 录音配置参数 */
    private final AudioRecordConfig config;

    /** 音频数据总长度（不包含文件头） */
    private final long totalAudioLength;

    /**
     * 构造函数
     *
     * @param config 录音参数配置
     * @param totalAudioLength 音频数据总长度（字节），不包含 WAV 文件头
     */
    public WavHeader(AudioRecordConfig config, long totalAudioLength) {
        this.config = config;
        this.totalAudioLength = totalAudioLength;
    }

    /**
     * 生成 WAV 文件头的字节数组
     *
     * 该方法会根据配置参数生成符合 WAV 格式规范的 44 字节文件头。
     *
     * @return WAV 文件头的字节数组，长度为 44 字节
     */
    public byte[] toBytes() {
        long sampleRateInHz = config.getSampleRateInHz();
        int channels = config.getChannels();
        byte bitsPerSample = config.bitsPerSample();
        // 计算每秒数据字节数：采样率 × 声道数 × 采样位数 / 8
        long byteRate = sampleRateInHz * channels * bitsPerSample / 8;
        com.bihe0832.android.lib.log.ZLog.d("WavHeader", "toBytes() params: totalAudioLen=" + totalAudioLength 
                + ", totalDataLen=" + (totalAudioLength + 36) 
                + ", sampleRate=" + sampleRateInHz 
                + ", channels=" + channels 
                + ", byteRate=" + byteRate 
                + ", bitsPerSample=" + bitsPerSample);
        return wavFileHeader(
                totalAudioLength,
                totalAudioLength + 36,  // 文件总长度 - 8
                sampleRateInHz,
                channels,
                byteRate,
                bitsPerSample
        );
    }

    /**
     * 生成 WAV 文件头的字节数组（内部方法）
     *
     * WAV 文件头结构（总计 44 字节）：
     * - RIFF 区块（12 字节）：文件标识、文件大小、文件类型
     * - FORMAT 区块（24 字节）：格式信息（采样率、声道数、位深等）
     * - DATA 区块（8 字节）：数据标识和数据大小
     *
     * @param totalAudioLen 音频数据总长度（字节）
     * @param totalDataLen 文件总长度 - 8（字节）
     * @param longSampleRate 采样率（Hz）
     * @param channels 声道数（1=单声道，2=立体声）
     * @param byteRate 每秒数据字节数（声道数 × 采样率 × 采样位数 / 8）
     * @param bitsPerSample 采样位数（8 或 16 bit）
     * @return WAV 文件头的字节数组，长度为 44 字节
     */
    private byte[] wavFileHeader(long totalAudioLen, long totalDataLen, long longSampleRate,
            int channels, long byteRate, byte bitsPerSample) {
        
        byte[] header = new byte[44];

        // ========== RIFF 区块（12 字节） ==========
        // [0-3] 文档标识: 大写字符串 "RIFF"，标明该文件为有效的 RIFF 格式文档
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        // [4-7] 文件数据长度: 从下一个字段首地址开始到文件末尾的总字节数
        //           该值 = fileSize - 8（小端序）
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);

        // [8-11] 文件格式类型: 所有 WAV 格式的文件此处为字符串 "WAVE"
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';

        // ========== FORMAT 区块（24 字节） ==========
        // [12-15] 格式块标识: 小写字符串 "fmt "（注意有一个空格）
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';

        // [16-19] 格式块长度: 取决于编码格式，PCM 格式为 16
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;

        // [20-21] AudioFormat（音频格式）: 常见的 PCM 音频数据的值为 1
        header[20] = 1;
        header[21] = 0;

        // [22-23] NumChannels（声道数）: 1=单声道，2=立体声
        header[22] = (byte) channels;
        header[23] = 0;

        // [24-27] SampleRate（采样率）: 每个声道单位时间采样次数（小端序）
        //         常用的采样频率有 8000, 11025, 16000, 22050, 44100 Hz
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);

        // [28-31] ByteRate（数据传输速率）: 每秒数据字节数（小端序）
        //         该数值 = 声道数 × 采样率 × 采样位数 / 8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);

        // [32-33] BlockAlign（数据块对齐）: 采样帧大小
        //         该数值 = 声道数 × 采样位数 / 8
        header[32] = (byte) (channels * (bitsPerSample / 8));
        header[33] = 0;

        // [34-35] BitsPerSample（采样位数）: 每个采样存储的 bit 数
        //         常见的位数有 8、16、32
        header[34] = bitsPerSample;
        header[35] = 0;

        // ========== DATA 区块（8 字节） ==========
        // [36-39] 标识: 小写字符串 "data"，标示头结束，开始数据区域
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';

        // [40-43] 音频数据长度: PCM 数据的字节数（小端序）
        //         N = ByteRate × seconds
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        return header;
    }
}
