package com.bihe0832.android.lib.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-18.
 * Description: Description
 */
public class StringNullAdapter extends TypeAdapter<String> {
    @Override
    public String read(JsonReader reader){
        try {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return "";//原先是返回Null，这里改为返回空字符串
            }

            String jsonStr = reader.nextString();
            if(jsonStr.equals("null")) {
                return "";
            }else {
                return jsonStr;
            }
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void write(JsonWriter writer, String value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }
        writer.value(value);
    }
}