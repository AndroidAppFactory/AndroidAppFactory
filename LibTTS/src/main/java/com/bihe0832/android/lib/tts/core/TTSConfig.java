package com.bihe0832.android.lib.tts.core;

import com.bihe0832.android.lib.utils.IdGenerator;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2024/10/18.
 *         Description:
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
