package com.kobayashi.user.put

import android.content.Context
import android.net.ConnectivityManager

object NetworkManager {
    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        return (networkInfo?.isConnected == true)
    }
}