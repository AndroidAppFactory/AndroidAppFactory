package com.bihe0832.android.lib.router;

import android.net.Uri;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-07-19.
 * Description: Description
 */
public class UriCompact {

    /**
     * Call Uri#getQueryParameterNames() below api 11.
     *
     * @param uri Uri
     * @return Set
     */
    public static Set<String> getQueryParameterNames(Uri uri) {
        String query = uri.getEncodedQuery();
        if (query == null) {
            return Collections.emptySet();
        }

        Set<String> names = new LinkedHashSet<String>();
        int start = 0;
        do {
            int next = query.indexOf('&', start);
            int end = (next == -1) ? query.length() : next;

            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }

            String name = query.substring(start, separator);
            names.add(Uri.decode(name));
            // Move start to end of name.
            start = end + 1;
        } while (start < query.length());

        return Collections.unmodifiableSet(names);
    }
}
