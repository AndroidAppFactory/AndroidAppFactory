package com.bihe0832.android.lib.audio.record.core;

import com.bihe0832.android.lib.audio.AudioUtils;
import com.bihe0832.android.lib.audio.AudioRecordConfig;

/**
 * An AudioChunk is a audio data wrapper.
 * 音频数据包装器
 *
 * @author maple
 * @time 2018/4/10.
 */
public interface AudioChunk {

    double REFERENCE = 0.6;

    static double getMaxAmplitude(short[] data) {
        int nMaxAmp = 0;
        for (short sh : data) {
            if (sh > nMaxAmp) {
                nMaxAmp = sh;
            }
        }
        if (nMaxAmp > 0) {
            return Math.abs(20 * Math.log10(nMaxAmp / REFERENCE));
        } else {
            return 0;
        }
    }

    // 获取最大峰值(振幅)
    double maxAmplitude();

    // 获取byte类型数据
    byte[] toBytes();

    // 获取short类型数据
    short[] toShorts();

    /**
     * 设置【音频数据块】拉取监听器
     */
    interface OnAudioChunkPulledListener {

        /**
         * 拉取 音频原始数据
         *
         * @param audioChunk 音频数据块
         */
        void onAudioChunkPulled(AudioRecordConfig config, AudioChunk audioChunk, int dataLength);
    }

    abstract class AbstractAudioChunk implements AudioChunk {


        @Override
        public double maxAmplitude() {
            return getMaxAmplitude(toShorts());
        }
    }

    /**
     * byte类型数据包装器
     */
    class Bytes extends AbstractAudioChunk {

        private final byte[] bytes;

        Bytes(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public byte[] toBytes() {
            return bytes;
        }

        @Override
        public short[] toShorts() {
            return AudioUtils.INSTANCE.byteArrayToShortArray(bytes, bytes.length);
        }
    }

    /**
     * short类型数据包装器
     */
    class Shorts extends AbstractAudioChunk {

        private final short[] shorts;

        Shorts(short[] bytes) {
            this.shorts = bytes;
        }

        @Override
        public byte[] toBytes() {
            return AudioUtils.INSTANCE.shortArrayToByteArray(shorts);
        }

        @Override
        public short[] toShorts() {
            return shorts;
        }

    }
}
