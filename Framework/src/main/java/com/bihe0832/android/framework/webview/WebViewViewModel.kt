package com.bihe0832.android.framework.webview

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class WebViewViewModel : ViewModel() {

    private val titleLiveData by lazy { MutableLiveData<String>() }


    fun getTitleLiveData(): LiveData<String> {
        return titleLiveData
    }

    fun setTitleString(s: String){
        titleLiveData.value = s
    }

}