package com.bihe0832.android.test.module.json;

import com.bihe0832.android.lib.gson.adapter.RawStringJsonAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

public class JsonTest {

	@SerializedName("key")
	private int key;
	@SerializedName("value")
	@JsonAdapter(RawStringJsonAdapter.class)
	private String data = "";

	public int getKey(){
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}
}