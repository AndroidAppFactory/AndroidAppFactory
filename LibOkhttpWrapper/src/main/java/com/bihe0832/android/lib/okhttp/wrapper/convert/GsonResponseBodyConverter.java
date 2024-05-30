package com.bihe0832.android.lib.okhttp.wrapper.convert;

import com.bihe0832.android.lib.gson.JsonHelper;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2024/5/30.
 *         Description:
 */
class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private final TypeAdapter<T> adapter;

    GsonResponseBodyConverter(TypeAdapter<T> adapter) {
        this.adapter = adapter;
    }

    public T convert(ResponseBody value) throws IOException {
        JsonReader jsonReader = JsonHelper.INSTANCE.getGson().newJsonReader(value.charStream());

        Object var4;
        try {
            T result = this.adapter.read(jsonReader);
            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonIOException("JSON document was not fully consumed.");
            }

            var4 = result;
        } finally {
            value.close();
        }

        return (T) var4;
    }
}
