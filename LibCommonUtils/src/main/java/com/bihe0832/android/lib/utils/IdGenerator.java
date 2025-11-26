package com.bihe0832.android.lib.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程安全的ID生成器
 * 
 * @author zixie code@bihe0832.com
 * Created on 2020-06-11.
 * 
 * Description: 基于 AtomicInteger 实现的线程安全ID生成器，用于生成递增的唯一ID。
 * 适用于需要在多线程环境下生成唯一标识符的场景。
 * 
 * 使用示例：
 * <pre>
 * IdGenerator generator = new IdGenerator(0);
 * int id1 = generator.generate(); // 返回 1
 * int id2 = generator.generate(); // 返回 2
 * </pre>
 */
public class IdGenerator extends AtomicInteger {

    /**
     * 构造函数，初始化ID生成器
     *
     * @param initialValue 初始值，生成的第一个ID将是 initialValue + 1
     */
    public IdGenerator(int initialValue) {
        super(initialValue);
    }

    /**
     * 生成下一个ID
     * 
     * 该方法是线程安全的，可以在多线程环境下并发调用。
     * 每次调用都会返回一个递增的唯一ID。
     *
     * @return 生成的唯一ID（递增）
     */
    public int generate() {
        return super.incrementAndGet();
    }
}