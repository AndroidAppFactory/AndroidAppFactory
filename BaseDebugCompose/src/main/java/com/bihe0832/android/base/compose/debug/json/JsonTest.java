package com.bihe0832.android.base.compose.debug.json;

import com.google.gson.annotations.SerializedName;

public class JsonTest {

    @SerializedName("key")
    private int key;

    @SerializedName("value1")
    private String data1 = "";

    @SerializedName("value2")
    private boolean data2 = false;

    @SerializedName("value3")
    private String data3 = "";

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setData3(String data3) {
        this.data3 = data3;
    }

    @Override
    public String toString() {
        return "JsonTest{" +
                "key=" + key +
                ", data1='" + data1 + '\'' +
                ", data2=" + data2 +
                '}';
    }
}