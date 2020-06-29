package com.bihe0832.android.lib.utils;

import android.support.v4.util.ArrayMap;
import android.support.v4.util.ArraySet;

import java.util.Map;
import java.util.Set;

public final class CollectionCompat {

    private static final int BASE_SIZE = 4;
    private static final int TWICE_BASE_SIZE = BASE_SIZE << 1;

    @SuppressWarnings("unused")
    public static <K, V> Map<K, V> createMap() {
        return createMap(0);
    }

    public static <K, V> Map<K, V> createMap(int capacity) {
        return new ArrayMap<>(adjustCapacity(capacity));
    }

    public static <E> Set<E> createSet() {
        return createSet(0);
    }

    public static <E> Set<E> createSet(int capacity) {
        return new ArraySet<>(adjustCapacity(capacity));
    }

    private static int adjustCapacity(int capacity) {
        if (BASE_SIZE >= capacity) {
            return BASE_SIZE;
        }
        if (TWICE_BASE_SIZE >= capacity) {
            return TWICE_BASE_SIZE;
        }
        return capacity;
    }
}
