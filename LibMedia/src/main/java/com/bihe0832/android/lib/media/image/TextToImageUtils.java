package com.bihe0832.android.lib.media.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

public class TextToImageUtils {

    public static Bitmap createImageFromText(
            Context context,
            int width, int height, String backgroundColor, int paddingDp,
            Bitmap icon, int iconSize,
            String title, String titleColor, int titleSizeDp, float titleSpacingMult, Alignment titleAlignment,
            int titleTopPadding,
            String text, String textColor, int textSizeDp, float textSpacingMult, int maxLines,
            Alignment contentAlignment,
            int contentTopPadding) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.parseColor(backgroundColor));
            int padding = DisplayUtil.dip2px(context, paddingDp);
            int iconHeight = DisplayUtil.dip2px(context, iconSize);

            // Create title TextPaint
            TextPaint titleTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            titleTextPaint.setColor(Color.parseColor(titleColor));
            titleTextPaint.setTextSize(DisplayUtil.dip2px(context, titleSizeDp));
            titleTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

            // Create a StaticLayout for the title
            StaticLayout.Builder titleBuilder = StaticLayout.Builder.obtain(title, 0, title.length(), titleTextPaint,
                    width - padding);
            titleBuilder.setAlignment(titleAlignment);
            titleBuilder.setLineSpacing(0, titleSpacingMult);
            titleBuilder.setIncludePad(false);
            titleBuilder.setMaxLines(1);
            StaticLayout titleStaticLayout = titleBuilder.build();

            TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.parseColor(textColor));
            textPaint.setTextSize(DisplayUtil.dip2px(context, textSizeDp));
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);

            // Create a StaticLayout with the text and TextPaint
            StaticLayout.Builder builder = StaticLayout.Builder.obtain(
                    text, 0, text.length(), textPaint,
                    width - padding
            );
            builder.setAlignment(contentAlignment);
            builder.setLineSpacing(0, textSpacingMult);
            builder.setIncludePad(false);
            builder.setMaxLines(maxLines);
            builder.setEllipsize(TextUtils.TruncateAt.END);

            StaticLayout staticLayout = builder.build();
            int x = padding / 2;
            int y = (height - staticLayout.getHeight() - titleStaticLayout.getHeight() - iconHeight
                    - (DisplayUtil.dip2px(
                    context, titleTopPadding + contentTopPadding))) / 2;

            canvas.save();
            canvas.translate(x, y);
            Rect iconDestRect = new Rect(0, 0, iconHeight, iconHeight);
            canvas.drawBitmap(icon, null, iconDestRect, null);
            canvas.restore();

            canvas.save();
            canvas.translate(x, y + iconHeight + DisplayUtil.dip2px(
                    context, titleTopPadding));
            titleStaticLayout.draw(canvas);
            canvas.restore();

            canvas.save();
            canvas.translate(x, y + iconHeight + titleStaticLayout.getHeight() + DisplayUtil.dip2px(
                    context, titleTopPadding + contentTopPadding));
            staticLayout.draw(canvas);
            canvas.restore();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}