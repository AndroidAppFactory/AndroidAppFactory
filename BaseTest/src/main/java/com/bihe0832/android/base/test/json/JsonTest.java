package com.bihe0832.android.base.test.json;

import com.bihe0832.android.lib.gson.adapter.BooleanDefaultAdapter;
import com.bihe0832.android.lib.gson.adapter.BooleanTypeAdapter;
import com.bihe0832.android.lib.gson.adapter.RawStringJsonAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

public class JsonTest {

	@SerializedName("key")
	private int key;

	@SerializedName("value1")
	@JsonAdapter(RawStringJsonAdapter.class)
	private String data1 = "";

	@SerializedName("value2")
	@JsonAdapter(BooleanDefaultAdapter.class)
	private boolean data2 = false;

	public int getKey(){
		return key;
	}

	public void setKey(int key) {
		this.key = key;
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