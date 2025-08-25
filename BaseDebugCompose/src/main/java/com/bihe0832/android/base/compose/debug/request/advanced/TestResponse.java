package com.bihe0832.android.base.compose.debug.request.advanced;


import com.google.gson.annotations.SerializedName;

import static com.bihe0832.android.lib.http.advanced.HttpAdvancedResponseHandler.RET_FAIL;


public class TestResponse {

    @SerializedName("ret")
    public int ret = RET_FAIL;

    @SerializedName("flag")
    public int flag = RET_FAIL;

    @SerializedName("msg")
    public String msg  = "";

    @SerializedName("para")
    public String para = "";

    @Override
    public String toString() {
        return "ret：\n\t" + ret + " \n " +
                "flag：\n\t" + flag + " \n " +
                "msg：\n\t" + msg + " \n " +
                "para：\n\t" + para + " \n ";
    }
}
