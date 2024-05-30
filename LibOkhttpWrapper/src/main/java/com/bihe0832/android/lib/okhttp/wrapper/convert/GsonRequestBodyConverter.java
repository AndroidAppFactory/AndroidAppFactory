package com.bihe0832.android.lib.okhttp.wrapper.convert;

import com.bihe0832.android.lib.gson.JsonHelper;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Converter;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2024/5/30.
 *         Description:
 */
final class GsonRequestBodyConverter<T> implements Converter<T, RequestBody> {

    private final TypeAdapter<T> adapter;

    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    GsonRequestBodyConverter(TypeAdapter<T> adapter) {
        this.adapter = adapter;
    }

    public RequestBody convert(T value) throws IOException {
        Buffer buffer = new Buffer();
        Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
        JsonWriter jsonWriter = JsonHelper.INSTANCE.getGson().newJsonWriter(writer);
        this.adapter.write(jsonWriter, value);
        jsonWriter.close();
        return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
    }
}

