package com.bihe0832.android.lib.ui.image;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * @author hardyshi code@bihe0832.com Created on 2020/12/9.
 */
@GlideModule
public class ZAppGlideModule extends AppGlideModule {
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}