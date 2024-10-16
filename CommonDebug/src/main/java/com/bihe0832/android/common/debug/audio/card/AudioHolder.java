package com.bihe0832.android.common.debug.audio.card;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.bihe0832.android.common.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.lib.audio.wav.WaveFileReader;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.utils.time.DateUtil;
import java.io.File;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-11-21.
 *         Description: Description
 */

public class AudioHolder extends CardBaseHolder {

    public AudioHolder(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void initView() {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void initData(CardBaseModule item) {
        AudioData data = (AudioData) item;
        String filePath = data.filePath;
        File file = new File(filePath);
//        String title =
//                FileUtils.INSTANCE.getFileName(filePath) + "   |  " + FileUtils.INSTANCE.getFileLength(file.length());
        String title = FileUtils.INSTANCE.getFileName(filePath) + "  |  " + DateUtil.getDateEN(file.lastModified());
        ((TextView) getView(R.id.audio_title)).setText(title);
        WaveFileReader waveFileReader = new WaveFileReader(filePath);
        if (waveFileReader.isSuccess()){
            String fileLength = "文件大小：" + FileUtils.INSTANCE.getFileLength(file.length());
            ((TextView) getView(R.id.audio_desc)).setText(
                    fileLength + "，" + waveFileReader.toShowString() + "，" + data.amplitude);
            TextView result = (TextView) getView(R.id.audio_recognise);
            if (TextUtils.isEmpty(data.recogniseResult)) {
                result.setVisibility(View.GONE);
            } else {
                result.setText(data.recogniseResult);
                result.setVisibility(View.VISIBLE);
            }

            addOnLongClickListener(R.id.audio_icon);
            addOnLongClickListener(R.id.audio_title);
            addOnLongClickListener(R.id.audio_desc);

            addOnClickListener(R.id.audio_icon);
            addOnClickListener(R.id.audio_title);
            addOnClickListener(R.id.audio_desc);
        }else {
            ((TextView) getView(R.id.audio_desc)).setText("音频文件异常，解析失败，请检查音频格式");
        }
    }
}
