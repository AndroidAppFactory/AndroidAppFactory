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
 * wav文件 解析器
 *
 * @author maple
 * @time 2018/2/1 下午4:06
 */
public class WaveFileReader {

    private int len = 0;

    private int numChannels = 0;
    private long sampleRate = 0;
    private int bitsPerSample = 0;

    private int duration = 0;
    private FileInputStream fis = null;
    private BufferedInputStream bis = null;
    private boolean isSuccess = false;

    public WaveFileReader(String filename) {
        this.initReader(filename);
    }

    /**
     * 判断是否创建wav读取器成功
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * 获取每个采样的编码长度，8bit或者16bit
     */
    public int getBitPerSample() {
        return this.bitsPerSample;
    }

    /**
     * 获取采样率
     */
    public long getSampleRate() {
        return this.sampleRate;
    }

    /**
     * 获取声道个数，1代表单声道 2代表立体声
     */
    public int getNumChannels() {
        return this.numChannels;
    }

    /**
     * 获取数据长度，也就是一共采样多少个
     */
    public int getDataLen() {
        return this.len;
    }


    /**
     * 获取音频时长
     */
    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "采样率：" + getSampleRate() + "；声道数：" + getNumChannels() + "；编码长度：" + getBitPerSample() + "；数据长度："
                + getDataLen() + "；音频长度：" + getDuration();
    }


    public String toShowString() {
        return "采样率：" + getSampleRate() + "；声道数：" + getNumChannels() + "；编码长度：" + getBitPerSample() + "；数据长度："
                + FileUtils.INSTANCE.getFileLength(getDataLen()) + "；音频时长：" + TimeUtil.formatSecondsTo00(
                getDuration() / 1000);
    }


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
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private String readString(int len) {
        byte[] buf = new byte[len];
        try {
            if (bis.read(buf) != len) {
                throw new IOException("no more data!!!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(buf);
    }

    private int readInt() {
        byte[] buf = new byte[2];
        int res = 0;
        try {
            if (bis.read(buf) != 2) {
                throw new IOException("no more data!!!");
            }
            res = (buf[0] & 0x000000FF) | (((int) buf[1]) << 8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

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
            res = l[0] | (l[1] << 8) | (l[2] << 16) | (l[3] << 24);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
