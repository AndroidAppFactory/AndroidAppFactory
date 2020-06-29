package com.bihe0832.android.test.module;

import com.google.gson.annotations.SerializedName;

public class JsonTest {

	@SerializedName("key")
	private int key;

	public int getKey(){
		return key;
	}
}