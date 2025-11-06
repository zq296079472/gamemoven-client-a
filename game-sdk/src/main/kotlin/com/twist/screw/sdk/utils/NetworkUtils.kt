package com.twist.screw.sdk.utils

import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {

    fun isActuallyOnline(): Boolean {
        return try {
            val urlc = URL("https://clients3.google.com/generate_204")
                .openConnection() as HttpURLConnection
            urlc.setRequestProperty("User-Agent", "Android")
            urlc.setRequestProperty("Connection", "close")
            urlc.connectTimeout = 3000
            urlc.readTimeout = 3000
            urlc.connect()
            urlc.responseCode == 204
        } catch (e: Exception) {
            false
        }
    }
}
