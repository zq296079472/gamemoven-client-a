package com.twist.screw.sdk.utils

import android.util.Log
import androidx.annotation.Keep
import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashReporter {
    fun recordException(err: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(err)
    }

    fun recordCocosException(stackTrace: String?, msg: String?, reason: String?) {
        val crashlytics = FirebaseCrashlytics.getInstance();
        val exception: CocosError
        if (!reason.isNullOrEmpty()) {
            crashlytics.setCustomKey("cocos_reason", reason);
            exception = CocosError("$msg .Error thrown $reason . ")
        } else {
            exception = CocosError(msg ?: "")
        }
        crashlytics.setCustomKey("cocos_exception", msg ?: "")
        val elements = ArrayList<StackTraceElement>()
        val stacks = stackTrace?.split(";") ?: emptyList();
        for (errorElement in stacks) {
            val info = errorElement.split(":")
            if (info.size >= 4) {
                elements.add(
                    StackTraceElement(
                        info[2],
                        info[3],
                        info[0],
                        info[1].toIntOrNull() ?: 0
                    )
                );
            }
        }
        exception.stackTrace = elements.toTypedArray()
        crashlytics.recordException(exception)
        Log.e("CrashUtil", "Cocos Exception ${exception.stackTrace}", exception)
    }

    fun log(msg: String) {
        FirebaseCrashlytics.getInstance().log(msg)
    }
}


@Keep
class CocosError(message: String) : Exception(message)
