package com.bihe0832.android.common.image.blur;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.renderscript.Allocation;
import android.renderscript.Allocation.MipmapControl;
import android.renderscript.Element;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.RenderScript.RSMessageHandler;
import android.renderscript.ScriptIntrinsicBlur;

/**
 * @author zixie code@bihe0832.com Created on 2022/3/28.
 */
public class RSBlur {
    public RSBlur() {
    }

    @TargetApi(18)
    public static Bitmap blur(Context context, Bitmap bitmap, int radius) throws RSRuntimeException {
        RenderScript rs = null;
        Allocation input = null;
        Allocation output = null;
        ScriptIntrinsicBlur blur = null;

        try {
            rs = RenderScript.create(context);
            rs.setMessageHandler(new RSMessageHandler());
            input = Allocation.createFromBitmap(rs, bitmap, MipmapControl.MIPMAP_NONE, 1);
            output = Allocation.createTyped(rs, input.getType());
            blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            blur.setInput(input);
            blur.setRadius((float)radius);
            blur.forEach(output);
            output.copyTo(bitmap);
        } finally {
            if (rs != null) {
                if (VERSION.SDK_INT >= 23) {
                    RenderScript.releaseAllContexts();
                } else {
                    rs.destroy();
                }
            }

            if (input != null) {
                input.destroy();
            }

            if (output != null) {
                output.destroy();
            }

            if (blur != null) {
                blur.destroy();
            }

        }

        return bitmap;
    }
}
