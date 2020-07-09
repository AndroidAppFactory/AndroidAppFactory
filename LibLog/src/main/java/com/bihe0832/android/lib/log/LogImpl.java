package com.bihe0832.android.lib.log;

public interface LogImpl {

    String getName();

    void i(String tag, String msg);

    void d(String tag, String msg);

    void w(String tag, String msg);

    void e(String tag, String msg);

    void info(String tag, String msg);
}