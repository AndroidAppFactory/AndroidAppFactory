package com.bihe0832.android.common.network

import android.content.Intent
import com.bihe0832.android.common.network.NetworkChangeEvent


interface NetworkChangeListener {
    fun onNetworkChange(networkChangeEvent: NetworkChangeEvent, intent: Intent?)
}