package com.flyco.tablayout.listener;


import androidx.annotation.DrawableRes;

public interface CustomTabEntity {
    String getTabTitle();
    float getTabTextSize();//sp

    @DrawableRes
    int getTabSelectedIcon();

    @DrawableRes
    int getTabUnselectedIcon();
}