package com.bihe0832.android.common.webview.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WebViewViewModel : ViewModel() {

    private val titleLiveData by lazy { MutableLiveData<String>() }


    fun getTitleLiveData(): LiveData<String> {
        return titleLiveData
    }

    fun setTitleString(s: String){
        titleLiveData.value = s
    }

}