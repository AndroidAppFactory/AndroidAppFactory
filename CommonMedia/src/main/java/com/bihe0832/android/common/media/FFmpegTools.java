package com.bihe0832.android.common.media;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.bihe0832.android.framework.file.AAFFileWrapper;
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2023/9/19.
 *         Description:
 */
public class FFmpegTools {

    public static int executeFFmpegCommand(final String[] command) {
        // 执行 FFmpeg 命令
        int result = FFmpeg.execute(command);
        System.gc();
        if (result == Config.RETURN_CODE_SUCCESS) {
            ZLog.i(MediaTools.TAG, "FFmpeg  execute 成功");
        } else {
            ZLog.i(MediaTools.TAG, "FFmpeg  execute 失败");
        }
        return result;
    }

    public static void convertAudioWithImageToVideo(int width, int height, String audioPath, String imagePath,
            AAFDataCallback<String> callback) {
        ThreadManager.getInstance().start(() -> {
            try {
                String videoPath = AAFFileWrapper.INSTANCE.getCacheVideoPath(".mp4");
                String[] combineCommand = {"-y", "-loop", "1", "-i", imagePath, "-i", audioPath, "-vf",
                        "scale=" + width + ":" + height, "-b:v", "2000k", "-c:a", "aac", "-b:a", "192k", "-pix_fmt",
                        "yuv420p", "-shortest", videoPath};
                int result = FFmpegTools.executeFFmpegCommand(combineCommand);
                if (result == Config.RETURN_CODE_SUCCESS) {
                    callback.onSuccess(videoPath);
                } else {
                    callback.onError(result, "executeFFmpegCommand failed");
                }
            } catch (Exception e) {
                callback.onError(-1, "executeFFmpegCommand exception:" + e);
            }
        });
    }
}