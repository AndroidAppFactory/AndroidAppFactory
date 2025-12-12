package com.bihe0832.android.lib.tts.core;

import com.bihe0832.android.lib.utils.IdGenerator;

/**
 * TTS配置常量类
 *
 * 定义TTS相关的配置Key和播放类型常量：
 * - 配置Key：用于持久化存储TTS参数（音调、语速、音量、引擎）
 * - 播放类型：定义不同的语音播放模式（顺序、插队、立即、清空）
 *
 * 播放类型说明：
 * - SPEEAK_TYPE_SEQUENCE(1): 顺序播放，添加到队列末尾
 * - SPEEAK_TYPE_NEXT(2): 插队播放，添加到队列头部
 * - SPEEAK_TYPE_FLUSH(3): 立即播放，打断当前播放
 * - SPEEAK_TYPE_CLEAR(4): 清空队列后播放
 *
 * @author code@bihe0832.com
 * Created on 2024/10/18.
 */
public class TTSConfig {

    public static final String CONFIG_KEY_PITCH = "com.bihe0832.android.lib.tts.pitch";
    public static final String CONFIG_KEY_SPEECH_RATE = "com.bihe0832.android.lib.tts.speech.rate";

    public static final String CONFIG_KEY_SPEECH_VOICE_VOLUME = "com.bihe0832.android.lib.tts.speech.volume";

    public static final String CONFIG_KEY_ENGINE = "com.bihe0832.android.lib.tts.engine";

    public static final int SPEEAK_TYPE_SEQUENCE = 1;
    public static final int SPEEAK_TYPE_NEXT = 2;
    public static final int SPEEAK_TYPE_FLUSH = 3;
    public static final int SPEEAK_TYPE_CLEAR = 4;

    private static IdGenerator sTTSIDGenerator = new IdGenerator(1);

    public static int getTTSID() {
        return sTTSIDGenerator.generate();
    }

}
