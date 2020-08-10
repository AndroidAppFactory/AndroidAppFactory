package com.bihe0832.android.lib.gson;

import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2020/8/10.
 * Description: 通过 @JsonAdapter 注解，部分字段不解析
 */
public class RawStringJsonAdapter extends TypeAdapter<String> {

    @Override
    public void write(JsonWriter out, String value){
        try {
            out.jsonValue(value);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public String read(JsonReader in){
        try {
            return new JsonParser().parse(in).toString();
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}