package com.bihe0832.android.base.compose.debug.reflection;

import android.util.Log;

/**
 * Description: Description
 * ATest.test1("test1test1", "", "", "", "", "", "");
 * ATestProxy.a("test1test1test1");
 * ATestProxy.b("test2btest2b", "test2btest2b");
 * ATestProxy.c("test2btest2c", "test2btest2c");
 */
public class ATest {

    public static void test1(final String pv_type, final String gameid, final String video_id, final String list_id,
            final String view_time, final String error_no, final String info) {
        Log.d("ATest", pv_type);
    }

    public static void test2(final String rp_type, final String gameid, final String video_id, final String list_id,
            final String view_time, final String first_time, final String video_time, final String error_no) {
        Log.d("ATest", rp_type);
    }

    public static void test3(final String login_type, final String error_no) {
        Log.d("ATest", login_type);
    }
}
