package com.bihe0832.android.lib.utils;

/**
 * 键值对数据结构
 * 
 * @author zixie code@bihe0832.com
 * Created on 2017-07-24.
 * 
 * Description: 简单的键值对数据结构，用于存储整数键和字符串值的映射关系。
 * 适用于需要临时存储键值对数据的场景。
 * 
 * 使用示例：
 * <pre>
 * KVPair pair = new KVPair(1, "value1");
 * System.out.println(pair.toString()); // 输出: key:1;value:value1
 * </pre>
 */
public class KVPair {
    
    /** 键（整数类型） */
    public int key;
    
    /** 值（字符串类型） */
    public String value;

    /**
     * 默认构造函数
     */
    public KVPair() {
    }

    /**
     * 构造函数，初始化键值对
     *
     * @param key 键
     * @param value 值
     */
    public KVPair(int key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 返回键值对的字符串表示
     *
     * @return 格式为 "key:键;value:值" 的字符串
     */
    @Override
    public String toString() {
        return "key:" + key + ";value:" + value;
    }
}
