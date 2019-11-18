package com.bihe0832.android.lib.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.bihe0832.android.lib.utils.ConvertUtils;

import java.lang.reflect.Type;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-18.
 * Description: Description
 */
public class LongDefaultAdapter implements JsonSerializer<Long>, JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context){
        try {
            return ConvertUtils.parseLong(json.getAsString(),0);
        } catch (Exception ignore) {
            ignore.printStackTrace();
            return 0L;
        }
    }

    @Override
    public JsonElement serialize(Long src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src);
    }
}