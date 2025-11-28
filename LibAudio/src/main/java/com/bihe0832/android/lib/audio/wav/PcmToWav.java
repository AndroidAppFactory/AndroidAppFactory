package com.bihe0832.android.lib.audio.wav;

import android.media.AudioRecord;
import com.bihe0832.android.lib.audio.AudioRecordConfig;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.log.ZLog;
import java.io.BufferedInputStream;
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

        FileInputStream fis = null;
        BufferedInputStream in = null;
        FileOutputStream out = null;
        
        try {
            fis = new FileInputStream(inFilename);
            // 使用 BufferedInputStream 包装，避免某些设备上 read() 返回异常值（如 -2）
            in = new BufferedInputStream(fis);
            out = new FileOutputStream(outFilename);
            
            // 获取 PCM 数据总长度
            long totalAudioLen = fis.getChannel().size();
            
            // 写入 WAV 文件头
            writeWavHeader(out, totalAudioLen);
            
            // 写入 PCM 数据
            writePcmData(in, out);
            
            ZLog.d(AudioRecordConfig.TAG, "convertToFile success: " + outFilename);
        } catch (IOException e) {
            ZLog.e(AudioRecordConfig.TAG, "convertToFile error: " + e.getMessage());
        } finally {
            closeStream(in, "buffered input stream");
            closeStream(fis, "file input stream");
            closeStream(out, "output stream");
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
        try {
            out = new FileOutputStream(outFilename);
            
            // 写入 WAV 文件头
            writeWavHeader(out, pcmData.length);
            
            // 写入 PCM 数据
            out.write(pcmData);
            
            ZLog.d(AudioRecordConfig.TAG, "convertToFile success: " + outFilename + ", size=" + pcmData.length);
            
            // 验证生成的 WAV 文件
            verifyWavFile(outFilename);
        } catch (IOException e) {
            ZLog.e(AudioRecordConfig.TAG, "convertToFile error: " + e.getMessage());
        } finally {
            closeStream(out, "output stream");
        }
    }

    /**
     * 获取有效的缓冲区大小
     * 
     * 如果 AudioRecord.getMinBufferSize() 返回错误值，则使用默认值（1秒音频数据）
     *
     * @return 有效的缓冲区大小（字节）
     */
    private int getValidBufferSize() {
        int bufferSize = AudioRecord.getMinBufferSize(
                mAudioRecordConfig.getSampleRateInHz(),
                mAudioRecordConfig.getChannelConfig(),
                mAudioRecordConfig.getAudioFormat());
        
        // 检查返回值是否有效
        if (bufferSize <= 0) {
            // 计算默认缓冲区大小：1秒的音频数据
            // 公式：采样率 × 声道数 × 每个样本的字节数
            int defaultBufferSize = mAudioRecordConfig.getSampleRateInHz() 
                    * mAudioRecordConfig.getChannels() 
                    * (mAudioRecordConfig.bitsPerSample() / 8);
            ZLog.w(AudioRecordConfig.TAG, "getMinBufferSize 返回无效值: " + bufferSize 
                    + ", 使用默认缓冲区大小: " + defaultBufferSize 
                    + " (采样率=" + mAudioRecordConfig.getSampleRateInHz()
                    + ", 声道=" + mAudioRecordConfig.getChannels()
                    + ", 位深=" + mAudioRecordConfig.bitsPerSample() + "bit)");
            return defaultBufferSize;
        }
        
        return bufferSize;
    }

    /**
     * 写入 WAV 文件头
     *
     * @param out 输出流
     * @param totalAudioLen PCM 数据总长度
     * @throws IOException IO 异常
     */
    private void writeWavHeader(FileOutputStream out, long totalAudioLen) throws IOException {
        WavHeader wavHeader = new WavHeader(mAudioRecordConfig, totalAudioLen);
        out.write(wavHeader.toBytes());
    }

    /**
     * 从输入流读取 PCM 数据并写入输出流
     *
     * @param in 输入流
     * @param out 输出流
     * @throws IOException IO 异常
     */
    private void writePcmData(BufferedInputStream in, FileOutputStream out) throws IOException {
        int bufferSize = getValidBufferSize();
        byte[] data = new byte[bufferSize];
        int readCount;
        while ((readCount = in.read(data)) != -1) {
            out.write(data, 0, readCount);
        }
    }

    /**
     * 验证生成的 WAV 文件
     *
     * @param filename WAV 文件路径
     */
    private void verifyWavFile(String filename) {
        try {
            WaveFileReader reader = new WaveFileReader(filename);
            if (reader.isSuccess()) {
                ZLog.d(AudioRecordConfig.TAG, "WAV 文件验证成功: " + reader.toString());
            } else {
                ZLog.e(AudioRecordConfig.TAG, "WAV 文件验证失败: " + filename);
            }
        } catch (Exception e) {
            ZLog.e(AudioRecordConfig.TAG, "WAV 文件验证异常: " + e.getMessage());
        }
    }

    /**
     * 安全关闭流
     *
     * @param stream 要关闭的流
     * @param streamName 流的名称（用于日志）
     */
    private void closeStream(AutoCloseable stream, String streamName) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                ZLog.e(AudioRecordConfig.TAG, "close " + streamName + " error: " + e.getMessage());
            }
        }
    }
}
