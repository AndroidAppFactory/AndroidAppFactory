package com.bihe0832.android.lib.audio.wav;

import com.bihe0832.android.lib.audio.AudioRecordConfig;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.time.TimeUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * WAV 文件解析器
 *
 * 该类用于解析 WAV 格式的音频文件，提取其中的元数据信息，包括：
 * - 采样率（Sample Rate）
 * - 声道数（Channels）
 * - 采样位数（Bits Per Sample）
 * - 数据长度（Data Length）
 * - 音频时长（Duration）
 *
 * 支持解析带有扩展块（LIST、fact 等）的 WAV 文件。
 *
 * @author maple
 * @author zixie code@bihe0832.com
 * @time 2018/2/1 下午4:06
 */
public class WaveFileReader {

    /** 数据长度（采样点数） */
    private int len = 0;

    /** 声道数（1=单声道，2=立体声） */
    private int numChannels = 0;

    /** 采样率（Hz） */
    private long sampleRate = 0;

    /** 采样位数（8 或 16 bit） */
    private int bitsPerSample = 0;

    /** 音频时长（毫秒） */
    private int duration = 0;

    /** 文件输入流 */
    private FileInputStream fis = null;

    /** 缓冲输入流 */
    private BufferedInputStream bis = null;

    /** 是否解析成功 */
    private boolean isSuccess = false;

    /**
     * 构造函数
     *
     * @param filename WAV 文件的完整路径
     */
    public WaveFileReader(String filename) {
        this.initReader(filename);
    }

    /**
     * 判断是否解析 WAV 文件成功
     *
     * @return true 表示解析成功，false 表示解析失败
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * 获取每个采样的编码长度
     *
     * @return 采样位数（8 或 16 bit）
     */
    public int getBitPerSample() {
        return this.bitsPerSample;
    }

    /**
     * 获取采样率
     *
     * @return 采样率（Hz）
     */
    public long getSampleRate() {
        return this.sampleRate;
    }

    /**
     * 获取声道数
     *
     * @return 声道数（1=单声道，2=立体声）
     */
    public int getNumChannels() {
        return this.numChannels;
    }

    /**
     * 获取数据长度
     *
     * @return 采样点数（总共采样多少个）
     */
    public int getDataLen() {
        return this.len;
    }


    /**
     * 获取音频时长
     *
     * @return 音频时长（毫秒）
     */
    public int getDuration() {
        return duration;
    }

    /**
     * 转换为字符串表示（简洁版）
     *
     * @return 包含所有音频参数的字符串
     */
    @Override
    public String toString() {
        return "采样率：" + getSampleRate() + "；声道数：" + getNumChannels() + "；编码长度：" + getBitPerSample() + "；数据长度："
                + getDataLen() + "；音频长度：" + getDuration();
    }


    /**
     * 转换为字符串表示（友好显示版）
     *
     * 该方法会将数据长度和时长格式化为更易读的格式。
     *
     * @return 格式化后的字符串
     */
    public String toShowString() {
        return "采样率：" + getSampleRate() + "；声道数：" + getNumChannels() + "；编码长度：" + getBitPerSample() + "；数据长度："
                + FileUtils.INSTANCE.getFileLength(getDataLen()) + "；音频时长：" + TimeUtil.formatSecondsTo00(
                getDuration() / 1000);
    }


    /**
     * 初始化读取器并解析 WAV 文件
     *
     * 该方法会解析 WAV 文件的所有区块，包括 RIFF、FORMAT、DATA 以及可能的扩展块。
     *
     * @param filename WAV 文件的完整路径
     */
    private void initReader(String filename) {
        try {
            File file = new File(filename);
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);

            // --- RIFF区块 ---
            String riffFlag = readString(4);
            if (!"RIFF".equals(riffFlag)) {
                ZLog.e(AudioRecordConfig.TAG, "RIFF miss, " + filename + " is not a wave file.");
                throw new IllegalArgumentException("RIFF miss, " + filename + " is not a wave file.");
            }
            long chunkSize = readLong();// 文件数据长度: 该值 = fileSize - 8。
            if (chunkSize != file.length() - 8) {
                ZLog.e(AudioRecordConfig.TAG,
                        "chunkSize error,  " + filename + " chunkSize is " + chunkSize + ", but file length is:"
                                + file.length());
            }
            String waveFlag = readString(4);
            if (!"WAVE".equals(waveFlag)) {
                ZLog.e(AudioRecordConfig.TAG, "WAVE miss, " + filename + " is not a wave file.");
                throw new IllegalArgumentException("WAVE miss, " + filename + " is not a wave file.");
            }

            // --- FORMAT区块 ---
            String fmtFlag = readString(4);
            if (!"fmt ".equals(fmtFlag)) {
                ZLog.e(AudioRecordConfig.TAG, "fmt miss, " + filename + " is not a wave file.");
                throw new IllegalArgumentException("fmt miss, " + filename + " is not a wave file.");
            }
            long subChunk1Size = readLong();// 格式块长度: 取决于编码格式，可以是 16、18、20、40 等
            int audioFormat = readInt();// AudioFormat(音频格式): 常见的 PCM 音频数据的值为1。
            this.numChannels = readInt();// NumChannels(声道数): 1：单声道，2：双声道/立体声
            this.sampleRate = readLong();// SampleRate(采样率): 每个声道单位时间采样次数。常用的采样频率有 11025, 22050 和 44100 kHz。
            long byteRate = readLong();// ByteRate(数据传输速率): 每秒数据字节数，该数值为:声道数×采样频率×采样位数/8。
            int blockAlign = readInt();// BlockAlign(数据块对齐): 采样帧大小。该数值为:声道数×采样位数/8。
            this.bitsPerSample = readInt();// BitsPerSample(采样位数): 每个采样存储的bit数。常见的位数有 8、16

            byte[] chunkId = new byte[4];
            byte[] chunkSizeBytes = new byte[4];

            boolean needCheck = true;
            while (bis.read(chunkId) != -1 && needCheck) {
                String chunkIdStr = new String(chunkId);
                // 读取当前块的大小
                bis.read(chunkSizeBytes);
                int tempChunkSize = ByteBuffer.wrap(chunkSizeBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

                switch (chunkIdStr) {
                    case "LIST":
                        // 处理 "LIST" 块
                        // 在此处解析扩展数据
                        bis.skip(tempChunkSize);
                        break;
                    case "fact":
                        // 处理 "fact" 块
                        // 在此处解析扩展数据
                        bis.skip(tempChunkSize);
                        break;
                    case "data":
                        // 读取音频数据
                        long audioLength = tempChunkSize;
                        //时长（毫秒） = 数据大小（字节） * 1000f  / (采样率 * 位深度 / 8 * 声道数)
                        duration = (int) (audioLength * 1000f / byteRate);
                        this.len = (int) (audioLength / (this.bitsPerSample * 8) / this.numChannels);
                        needCheck = false;
                        break;
                    // 在此处处理音频数据（audioData）
                    default:
                        // 跳过未知块
                        bis.skip(tempChunkSize);
                        break;
                }
            }

            isSuccess = true;
        } catch (Exception e) {
            ZLog.e(AudioRecordConfig.TAG, "initReader error: " + e.getMessage());
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e1) {
                ZLog.e(AudioRecordConfig.TAG, "close stream error: " + e1.getMessage());
            }
        }
    }

    /**
     * 从流中读取指定长度的字符串
     *
     * @param len 要读取的字节数
     * @return 读取到的字符串
     */
    private String readString(int len) {
        byte[] buf = new byte[len];
        try {
            if (bis.read(buf) != len) {
                throw new IOException("no more data!!!");
            }
        } catch (IOException e) {
            ZLog.e(AudioRecordConfig.TAG, "readString error: " + e.getMessage());
        }
        return new String(buf);
    }

    /**
     * 从流中读取 2 字节的整数（小端序）
     *
     * @return 读取到的整数值
     */
    private int readInt() {
        byte[] buf = new byte[2];
        int res = 0;
        try {
            if (bis.read(buf) != 2) {
                throw new IOException("no more data!!!");
            }
            // 小端序转换：低字节在前，高字节在后
            res = (buf[0] & 0x000000FF) | (((int) buf[1]) << 8);
        } catch (IOException e) {
            ZLog.e(AudioRecordConfig.TAG, "readInt error: " + e.getMessage());
        }
        return res;
    }

    /**
     * 从流中读取 4 字节的长整数（小端序）
     *
     * @return 读取到的长整数值
     */
    private long readLong() {
        long res = 0;
        try {
            long[] l = new long[4];
            for (int i = 0; i < 4; ++i) {
                l[i] = bis.read();
                if (l[i] == -1) {
                    throw new IOException("no more data!!!");
                }
            }
            // 小端序转换：低字节在前，高字节在后
            res = l[0] | (l[1] << 8) | (l[2] << 16) | (l[3] << 24);
        } catch (IOException e) {
            ZLog.e(AudioRecordConfig.TAG, "readLong error: " + e.getMessage());
        }
        return res;
    }
}
