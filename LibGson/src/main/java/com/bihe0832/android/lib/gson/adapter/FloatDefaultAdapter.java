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
public class FloatDefaultAdapter implements JsonSerializer<Float>, JsonDeserializer<Float> {
    @Override
    public Float deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context){
        try {
            return ConvertUtils.parseFloat(json.getAsString(),0);
        } catch (Exception ignore) {
            ignore.printStackTrace();
            return 0f;
        }
    }

    @Override
    public JsonElement serialize(Float src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src);
    }
}