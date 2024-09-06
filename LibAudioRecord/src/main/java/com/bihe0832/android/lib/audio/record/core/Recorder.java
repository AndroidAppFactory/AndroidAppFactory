package com.bihe0832.android.lib.audio.record.core;

/**
 * 录音接口
 * <p>
 * 实现该接口的类将提供:开始「startRecording」、暂停「pauseRecording」、继续「resumeRecording」、停止「stopRecording」方法。
 *
 * @author maple
 * @time 2018/4/10.
 */
public interface Recorder {

    /**
     * 开始，请确保当前app有 RECORD_AUDIO 权限
     */
    void startRecord();

    /**
     * 暂停
     */
    void pauseRecord();

    /**
     * 继续
     */
    void resumeRecord();

    /**
     * 停止
     */
    void stopRecord();

}
