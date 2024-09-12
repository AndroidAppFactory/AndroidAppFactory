package com.bihe0832.android.lib.pinyin.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.ahocorasick.trie.Emit;

/**
 * 正向最大匹配选择器
 */

public final class ForwardLongestSelector implements SegmentationSelector {

    public static final Engine.EmitComparator HIT_COMPARATOR = new Engine.EmitComparator();

    @Override
    public List<Emit> select(final Collection<Emit> emits) {
        if (emits == null) {
            return null;
        }

        List<Emit> results = new ArrayList<Emit>(emits);
        Collections.sort(results, HIT_COMPARATOR);
        int endValueToRemove = -1;
        Set<Emit> emitToRemove = new TreeSet<Emit>();
        for (Emit emit : results) {
            if (emit.getStart() > endValueToRemove && emit.getEnd() > endValueToRemove) {
                endValueToRemove = emit.getEnd();
            } else {
                emitToRemove.add(emit);
            }
        }

        results.removeAll(emitToRemove);

        return results;
    }
}
