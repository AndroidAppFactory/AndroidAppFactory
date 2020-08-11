package com.bihe0832.android.lib.gson.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.bihe0832.android.lib.utils.ConvertUtils;

import java.lang.reflect.Type;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-18.
 * Description: Description
 */
public class BooleanDefaultAdapter implements JsonSerializer<Boolean>, JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context){
        try {
            return ConvertUtils.parseBoolean(json.getAsString(),false);
        } catch (Exception ignore) {
            ignore.printStackTrace();
            return false;
        }
    }

    @Override
    public JsonElement serialize(Boolean src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src);
    }
}