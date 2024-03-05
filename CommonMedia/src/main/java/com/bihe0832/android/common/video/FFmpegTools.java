package com.bihe0832.android.common.video;

import android.media.MediaMetadataRetriever;
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.bihe0832.android.common.media.MediaTools;
import com.bihe0832.android.framework.file.AAFFileWrapper;
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.media.image.BitmapUtil;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MD5;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    public static int executeFFmpegCommand(final String command) {
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

    public static void convertAudioWithImageToVideo(int width, int height, String audioPath, long coverDuration,
            List<String> images,
            AAFDataCallback<String> callback) {
        ThreadManager.getInstance().start(() -> {
            try {
                ArrayList<String> realImageList = new ArrayList<>();
                String mergeFileFolder = FileUtils.INSTANCE.getFolderPathWithSeparator(
                        AAFFileWrapper.INSTANCE.getTempFolder("aaf_video_merge"));
                for (String file : images) {
                    if (FileUtils.INSTANCE.checkFileExist(file)) {
                        String newFile = mergeFileFolder + MD5.getFileMD5(file) + ".jpg";
                        BitmapUtil.saveBitmapWithPath(BitmapUtil.getLocalBitmap(file, width, height), newFile);
                        realImageList.add(newFile);
                    } else {
                        ZLog.e("!!! merge video, File not exist:" + file);
                    }
                }

                String videoPath = AAFFileWrapper.INSTANCE.getCacheVideoPath(".mp4");
                try {
                    File videoFile = new File(videoPath);
                    videoFile.createNewFile();
                }catch (Exception e){
                    e.printStackTrace();
                }

                // 获取音频时长
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(audioPath);
                long audioDuration = ConvertUtils.parseLong(
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION), 0L);
                long coverImageDuration = coverDuration;
                long remainingImageDuration = 0;
                if (realImageList.size() > 1) {
                    if (audioDuration < coverDuration) {
                        coverImageDuration = audioDuration;
                    } else {
                        remainingImageDuration =
                                (audioDuration - coverImageDuration) / (realImageList.size() - 1); // 其余图片持续时间（毫秒）
                    }
                } else {
                    coverImageDuration = audioDuration;
                }

                // 生成输入文件参数
                StringBuilder inputArgsBuilder = new StringBuilder();
                inputArgsBuilder.append(String.format("-loop 1 -i %s ", realImageList.get(0))); // 封面图
                for (int i = 1; i < realImageList.size(); i++) {
                    inputArgsBuilder.append(String.format("-loop 1 -i %s ", realImageList.get(i)));
                }
                String inputArgs = inputArgsBuilder.toString();

                // 设置滤镜
                StringBuilder filterArgsBuilder = new StringBuilder();
                for (int i = 0; i < realImageList.size(); i++) {
                    filterArgsBuilder.append(String.format(
                            "[%d:v]scale=iw*min(%d/iw\\,%d/ih):ih*min(%d/iw\\,%d/ih),pad=%d:%d:(%d-iw)/2:(%d-ih)/2,trim=duration=%.2f[v%d];",
                            i, width, height, width, height, width, height, width,
                            height, (i == 0 ? coverImageDuration : remainingImageDuration) / 1000.0, i));
                }
                for (int i = 0; i < realImageList.size(); i++) {
                    filterArgsBuilder.append(String.format("[v%d]", i));
                }
                filterArgsBuilder.append(String.format("concat=n=%d:v=1:a=0[v]", realImageList.size()));
                String filterComplex = filterArgsBuilder.toString();

                /**
                 * -b:v 2000k（视频比特率为 2000 kbps）、
                 * -c:v libopenh264 将视频编解码器设置为 H.264
                 * -pix_fmt yuv420p 将像素格式设置为 YUV 4:2:0，这是许多设备和应用程序所支持的格式。
                 * -c:a aac（音频编码器为 AAC）、
                 * -strict -2 允许使用实验性 AAC 编码器
                 * -movflags +faststart 添加 faststart 标志以优化视频流的播放
                 * -b:a 192k（音频比特率为 192 kbps）
                 */

                String command = String.format(
                        "-y %s -i %s -filter_complex \"%s\" -map \"[v]\" -map %d:a -shortest -b:v 2000k -c:v libopenh264 -pix_fmt yuv420p -c:a aac -strict -2 -b:a 192k -movflags +faststart %s",
                        inputArgs, audioPath, filterComplex, realImageList.size(), videoPath);
                int result = FFmpegTools.executeFFmpegCommand(command);
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

    public static void convertAudioWithImageToVideo(int width, int height, String audioPath, String imagePath,
            AAFDataCallback<String> callback) {
        try {
            ArrayList<String> realImageList = new ArrayList<>();
            realImageList.add(imagePath);
            convertAudioWithImageToVideo(width, height, audioPath, 0, realImageList, callback);
        } catch (Exception e) {
            callback.onError(-1, "executeFFmpegCommand exception:" + e);
        }
    }

    public static void convertAudioWithImageCoverToVideo(String imagePath, String audioPath,
            AAFDataCallback<String> callback) {
        ThreadManager.getInstance().start(() -> {
            try {

                String videoPath = AAFFileWrapper.INSTANCE.getCacheVideoPath(".mp4");
                String command = String.format(
                        "-y -r 1 -loop 1 -i %s -i %s -c:v libopenh264 -shortest -b:v 2000k -c:v libopenh264 -pix_fmt yuv420p -c:a aac -strict -2 -b:a 192k -movflags +faststart %s",
                        imagePath, audioPath, videoPath);
                int result = FFmpegTools.executeFFmpegCommand(command);
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