package com.bihe0832.android.base.compose.debug.ipc;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author zixie code@bihe0832.com Created on 5/19/21.
 */
public class TestObjectForIPC implements Parcelable {

    public int ret = 0;
    public int flag = 0;
    public String msg = "";

    public TestObjectForIPC() {

    }
    protected TestObjectForIPC(Parcel in) {
        ret = in.readInt();
        flag = in.readInt();
        msg = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ret);
        dest.writeInt(flag);
        dest.writeString(msg);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TestObjectForIPC> CREATOR = new Creator<TestObjectForIPC>() {
        @Override
        public TestObjectForIPC createFromParcel(Parcel in) {
            return new TestObjectForIPC(in);
        }

        @Override
        public TestObjectForIPC[] newArray(int size) {
            return new TestObjectForIPC[size];
        }
    };

    @Override
    public String toString() {
        return "TestObjectForIPC{" +
                "ret=" + ret +
                ", flag=" + flag +
                ", msg='" + msg + '\'' +
                '}';
    }
}