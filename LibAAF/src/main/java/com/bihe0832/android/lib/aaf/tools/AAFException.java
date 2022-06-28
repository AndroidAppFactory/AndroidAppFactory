package com.bihe0832.android.lib.aaf.tools;

/**
 * @author zixie code@bihe0832.com Created on 2020/12/10.
 */
public class AAFException extends Exception {

    public AAFException(String message) {
        super("\n\nAAF throw a Exception: " + message + " \n\n find more info from: https://github.com/bihe0832/AndroidAppFactory");
    }
}
