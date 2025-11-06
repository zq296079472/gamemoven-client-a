package com.twist.screw.sdk.analytics.ext

import android.os.Bundle

fun Map<String?, Any?>.toBundle(): Bundle {
    val bundle = Bundle()
    for (entry in entries.filter { it.key != null && it.value != null }) {
        val key: String = entry.key!!
        when (val value: Any = entry.value!!) {
            is String -> {
                bundle.putString(key, value)
            }

            is Int -> {
                bundle.putInt(key, value)
            }

            is Long -> {
                bundle.putLong(key, value)
            }

            is Double -> {
                bundle.putDouble(key, value)
            }

            else -> {
                throw Exception("Not Supported Type:${value.javaClass}")
            }
        }
    }
    return bundle
}
