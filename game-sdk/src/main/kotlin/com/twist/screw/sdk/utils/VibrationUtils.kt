package com.twist.screw.sdk.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import com.twist.screw.sdk.GameSDK

object VibrationUtils {

    fun vibrate(type: VibratorType) {
        val context: Context = GameSDK.getContext()
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val lightVibration = VibrationEffect.createOneShot(200, 50)  // 持续时间 200ms，振幅 50
        val mediumVibration = VibrationEffect.createOneShot(200, 127) // 振幅 127
        val heavyVibration = VibrationEffect.createOneShot(200, 255) // 振幅 255
        when (type) {
            VibratorType.Light -> vibrator.vibrate(lightVibration)
            VibratorType.Medium -> vibrator.vibrate(mediumVibration)
            VibratorType.Heavy -> vibrator.vibrate(heavyVibration)
        }
    }
}

public enum class VibratorType {
    Light, Medium, Heavy;
}