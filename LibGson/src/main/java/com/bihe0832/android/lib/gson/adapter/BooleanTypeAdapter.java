package com.bihe0832.android.lib.gson.adapter;

import com.bihe0832.android.lib.utils.ConvertUtils;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * @author hardyshi code@bihe0832.com Created on 9/10/21.
 *
 * true TRUE 1 为 true
 */
public class BooleanTypeAdapter extends TypeAdapter<Boolean> {

    @Override
    public void write(JsonWriter out, Boolean value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value);
        }
    }

    @Override
    public Boolean read(JsonReader in) throws IOException {
        JsonToken peek = in.peek();
        switch (peek) {
            case BOOLEAN:
                return in.nextBoolean();
            case NULL:
                in.nextNull();
                return null;
            case NUMBER:
                return in.nextInt() != 0;
            case STRING:
                return ConvertUtils.parseBoolean(in.nextString(), false);
            default:
                throw new JsonParseException("Expected BOOLEAN or NUMBER but was " + peek);
        }
    }
}
