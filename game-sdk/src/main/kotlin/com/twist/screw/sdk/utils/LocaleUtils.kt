package com.twist.screw.sdk.utils

import android.os.Build
import com.google.gson.Gson
import com.twist.screw.sdk.GameSDK
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object LocaleUtils {
    @JvmField
    var gson: Gson = Gson()

    fun getLocale(): Locale {
        var cache = CacheManager.local
        if (cache.isEmpty()) {
            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                GameSDK.getContext().resources.configuration.locales[0]
            } else {
                Locale.getDefault()
            }
            cache = "${locale?.language ?: "en"}:${locale?.country ?: "US"}"
            CacheManager.local = (cache)
        }
        val split = cache.split(':');
        return Locale(split[0], split[1])
    }

    fun getCurrentTime(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val utcTime = ZonedDateTime.now(ZoneId.of("UTC"))
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            return utcTime.format(formatter)
        } else {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val date = calendar.time
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(date)
        }
    }

    fun getCurrentUTCDate(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val utcTime = ZonedDateTime.now(ZoneId.of("UTC"))
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            return utcTime.format(formatter)
        } else {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val date = calendar.time

            val sdf = SimpleDateFormat("yyyyMMdd")
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(date)
        }
    }

}