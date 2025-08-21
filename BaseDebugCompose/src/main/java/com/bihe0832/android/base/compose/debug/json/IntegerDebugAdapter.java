package com.bihe0832.android.base.compose.debug.json;

import com.bihe0832.android.lib.utils.ConvertUtils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-18.
 * Description: Description
 */
public class IntegerDebugAdapter implements JsonSerializer<Integer>, JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context){
        try {
            return ConvertUtils.parseInt(json.getAsString(),100);
        } catch (Exception ignore) {
            ignore.printStackTrace();
            return 100;
        }
    }

    @Override
    public JsonElement serialize(Integer src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src);
    }
}