package com.bihe0832.android.lib.utils;

import java.util.LinkedList;

/**
 * 有限容量队列
 * 
 * @author zixie code@bihe0832.com
 * Created on 2021-04-27.
 * 
 * Description: 基于 LinkedList 实现的有限容量队列，当队列元素数量超过限制时，
 * 会自动移除最早添加的元素（FIFO - First In First Out）。
 * 适用于需要保持固定大小的缓存队列场景，如日志缓存、历史记录等。
 * 
 * 使用示例：
 * <pre>
 * LimitedQueue&lt;String&gt; queue = new LimitedQueue&lt;&gt;(3);
 * queue.add("A"); // 队列: [A]
 * queue.add("B"); // 队列: [A, B]
 * queue.add("C"); // 队列: [A, B, C]
 * queue.add("D"); // 队列: [B, C, D] (A被移除)
 * </pre>
 * 
 * @param <E> 队列元素类型
 */
public class LimitedQueue<E> extends LinkedList<E> {

    private static final long serialVersionUID = 1L;

    /** 队列容量限制 */
    private int limit;

    /**
     * 构造函数，创建指定容量的有限队列
     *
     * @param limit 队列容量限制（必须 > 0）
     */
    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    /**
     * 添加元素到队列末尾
     * 
     * 如果添加后队列大小超过限制，会自动移除队列头部的元素，
     * 直到队列大小不超过限制。
     *
     * @param o 要添加的元素
     * @return 始终返回 true
     */
    @Override
    public boolean add(E o) {
        super.add(o);
        // 如果超过容量限制，移除最早的元素
        while (size() > limit) {
            super.remove();
        }
        return true;
    }
}