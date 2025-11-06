package com.twist.screw.sdk.bridge

import android.app.Activity

interface ILifecycleAware {
    fun onResume(context: Activity)
    fun onDestroy()
}