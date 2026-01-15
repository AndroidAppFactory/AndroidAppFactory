package com.bihe0832.android.framework.image;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.util.Log;

import com.bihe0832.android.framework.ZixieContext;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.bumptech.glide.load.engine.bitmap_recycle.LruArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

/**
 * @author zixie code@bihe0832.com Created on 2020/12/9.
 */
@GlideModule
public class AAFAppGlideModule extends AppGlideModule {

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        super.applyOptions(context, builder);
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context).setMemoryCacheScreens(2).build();

        //内存缓存配置
        int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
        builder.setMemoryCache(new LruResourceCache(defaultMemoryCacheSize / 2));

        //创建 Bitmap 池
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();
        if (defaultBitmapPoolSize > 0) {
            builder.setBitmapPool(new LruBitmapPool(defaultBitmapPoolSize / 2));
        } else {
            builder.setBitmapPool(new BitmapPoolAdapter());
        }

        int defaultArrayPoolSize = calculator.getArrayPoolSizeInBytes();
        builder.setArrayPool(new LruArrayPool(defaultArrayPoolSize / 2));

        // 磁盘缓存
        int diskCacheSizeBytes = 500 * 1024 * 1024;
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, "glide_cache", diskCacheSizeBytes));

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo info = new MemoryInfo();
        if (null != activityManager) {
            activityManager.getMemoryInfo(info);
        }
        // 图片压缩质量
        builder.setDefaultRequestOptions(
                new RequestOptions()
                        .downsample(DownsampleStrategy.CENTER_INSIDE)//将图片缩小至目标大小
                        .format(info.lowMemory ? DecodeFormat.PREFER_RGB_565 : DecodeFormat.PREFER_ARGB_8888)
                        .disallowHardwareConfig()
        );

        int logLevel = Log.ERROR;
        if (ZixieContext.INSTANCE.isOfficial()) {
            logLevel = Log.ERROR;
        } else if (ZixieContext.INSTANCE.enableLog()) {
            logLevel = Log.DEBUG;
        } else {
            logLevel = Log.WARN;
        }
        builder.setLogLevel(logLevel);
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        super.registerComponents(context, glide, registry);
    }
}