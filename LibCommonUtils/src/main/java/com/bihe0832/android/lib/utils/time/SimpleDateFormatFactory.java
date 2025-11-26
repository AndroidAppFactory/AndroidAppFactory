package com.bihe0832.android.lib.utils.time;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * SimpleDateFormat 工厂类（线程安全）
 * 
 * @author zixie code@bihe0832.com
 * Created on 2023-05-26.
 * 
 * Description: 提供线程安全的 SimpleDateFormat 实例获取方法。
 * SimpleDateFormat 本身不是线程安全的，直接共享使用会导致并发问题。
 * 该工厂类使用 ThreadLocal 为每个线程维护独立的 SimpleDateFormat 实例池，
 * 既保证了线程安全，又避免了重复创建实例的开销。
 * 
 * 使用场景：
 * - 多线程环境下需要频繁使用 SimpleDateFormat 进行日期格式化
 * - 需要使用多种不同的日期格式
 * 
 * 使用示例：
 * <pre>
 * SimpleDateFormat sdf = SimpleDateFormatFactory.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 * String dateStr = sdf.format(new Date());
 * </pre>
 */
public class SimpleDateFormatFactory {
    
    /**
     * 线程本地存储的 SimpleDateFormat 实例池
     * 每个线程维护自己的 Map，key 为 "locale pattern" 格式，value 为 SimpleDateFormat 实例
     */
    private static final ThreadLocal<Map<String, SimpleDateFormat>> pool = new ThreadLocal<Map<String, SimpleDateFormat>>() {
        @Override
        protected Map<String, SimpleDateFormat> initialValue() {
            return new HashMap<>();
        }
    };

    /**
     * 获取指定格式和语言环境的 SimpleDateFormat 实例
     * 
     * 该方法是线程安全的，每个线程会获取到独立的 SimpleDateFormat 实例。
     * 对于相同的 pattern 和 locale 组合，同一线程内会复用已创建的实例。
     *
     * @param pattern 日期格式模式，如 "yyyy-MM-dd HH:mm:ss"
     * @param locale 语言环境，如 Locale.US、Locale.CHINA
     * @return 线程安全的 SimpleDateFormat 实例
     */
    public static SimpleDateFormat getSimpleDateFormat(String pattern, Locale locale) {
        // 使用 locale 和 pattern 组合作为 key
        String key = locale.toString() + " " + pattern;
        Map<String, SimpleDateFormat> formatMap = pool.get();
        SimpleDateFormat sdf = formatMap.get(key);
        
        // 如果当前线程还没有该格式的实例，则创建并缓存
        if (sdf == null) {
            sdf = new SimpleDateFormat(pattern, locale);
            formatMap.put(key, sdf);
        }
        return sdf;
    }

    /**
     * 获取指定格式的 SimpleDateFormat 实例（默认使用 Locale.US）
     * 
     * 该方法是线程安全的，每个线程会获取到独立的 SimpleDateFormat 实例。
     *
     * @param pattern 日期格式模式，如 "yyyy-MM-dd HH:mm:ss"
     * @return 线程安全的 SimpleDateFormat 实例（使用 Locale.US）
     */
    public static SimpleDateFormat getSimpleDateFormat(String pattern) {
        return getSimpleDateFormat(pattern, Locale.US);
    }
}