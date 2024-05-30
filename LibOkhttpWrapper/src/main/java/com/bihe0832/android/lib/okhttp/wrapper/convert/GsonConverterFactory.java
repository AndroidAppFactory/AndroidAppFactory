package com.bihe0832.android.lib.okhttp.wrapper.convert;

import com.bihe0832.android.lib.gson.JsonHelper;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2024/5/30.
 *         Description:
 */
public class GsonConverterFactory extends Converter.Factory {


    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = JsonHelper.INSTANCE.getGson().getAdapter(TypeToken.get(type));
        return new GsonResponseBodyConverter(adapter);
    }

    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations,
            Annotation[] methodAnnotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = JsonHelper.INSTANCE.getGson().getAdapter(TypeToken.get(type));
        return new GsonRequestBodyConverter(adapter);
    }
}
