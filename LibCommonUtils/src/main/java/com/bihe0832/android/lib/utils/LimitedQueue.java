package com.bihe0832.android.lib.utils;

import java.util.LinkedList;

/**
 * @author hardyshi code@bihe0832.com Created on 4/27/21.
 */
public class LimitedQueue<E> extends LinkedList<E> {

    private static final long serialVersionUID = 1L;

    private int limit;

    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > limit) {
            super.remove();
        }
        return true;
    }
}