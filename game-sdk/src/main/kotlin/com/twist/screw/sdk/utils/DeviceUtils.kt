package com.twist.screw.sdk.utils

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresPermission
import com.twist.screw.sdk.GameSDK
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

object DeviceUtils {
    private const val SP_FILE_NAME = "zd_device_config"
    private const val DEVICE_ID = "zd_device_id"
    private const val DEVICE_ID_SAVE_DIR = "backups/.system"

    private const val ENABLE_OLD_PATH_MIGRATION = true

    private val DEVICE_ID_SAVE_FILE: String
        get() = "elitykculzddeviceId_${GameSDK.getContext().packageName}"

    val deviceId: String
        get() {
            val sharedPreferences: SharedPreferences =
                GameSDK.getContext().getSharedPreferences(
                    GameSDK.getContext().getPackageName() + SP_FILE_NAME,
                    Context.MODE_PRIVATE
                )
            var deviceId = getString(sharedPreferences)

            if (deviceId.isEmpty()) {
                deviceId = deviceIdFromFile
                if (!deviceId.isEmpty()) {
                    putString(sharedPreferences, deviceId)
                } else {
                    deviceId = generateDeviceId()
                    putString(sharedPreferences, deviceId)
                    saveDeviceIdToFile(deviceId)
                }
            }

            return deviceId
        }

    private fun getString(sharedPreferences: SharedPreferences): String {
        return sharedPreferences.getString(DEVICE_ID, "")!!
    }

    private fun putString(sharedPreferences: SharedPreferences, value: String) {
        sharedPreferences.edit().putString(DEVICE_ID, value).apply()
    }

    private fun getDeviceIdFile(): File? {
        val context = GameSDK.getContext()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val sharedDir = File(
                Environment.getExternalStorageDirectory(),
                DEVICE_ID_SAVE_DIR
            )
            return File(sharedDir, DEVICE_ID_SAVE_FILE)
        }

        val externalFilesDir = context.getExternalFilesDir(null) ?: return null
        val dir = File(externalFilesDir, DEVICE_ID_SAVE_DIR)
        return File(dir, DEVICE_ID_SAVE_FILE)
    }

    private fun saveDeviceIdToFile(deviceId: String) {
        try {
            val deviceIdFile = getDeviceIdFile() ?: return
            deviceIdFile.parentFile?.mkdirs()
            FileUtils.saveByteByFile(
                deviceId.toByteArray(StandardCharsets.UTF_8),
                deviceIdFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val deviceIdFromFile: String
        get() {
            val deviceIdFile = getDeviceIdFile()
            if (deviceIdFile != null && deviceIdFile.exists()) {
                try {
                    val deviceIdData: ByteArray = FileUtils.fileToByte(deviceIdFile)
                    if (deviceIdData.size > 0) {
                        val deviceId = String(deviceIdData, StandardCharsets.UTF_8)
                        if (isValidDeviceId(deviceId)) {
                            return deviceId
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && ENABLE_OLD_PATH_MIGRATION) {
                return migrateFromOldPath()
            }

            return ""
        }

    private fun migrateFromOldPath(): String {
        try {
            val oldFile = File(
                Environment.getExternalStorageDirectory(),
                "backups/.system/elitykculzddeviceId"  // 旧版本的固定文件名（没有包名）
            )

            if (oldFile.exists()) {
                val deviceIdData: ByteArray = FileUtils.fileToByte(oldFile)
                if (deviceIdData.size > 0) {
                    val deviceId = String(deviceIdData, StandardCharsets.UTF_8)

                    if (isValidDeviceId(deviceId)) {
                        saveDeviceIdToFile(deviceId)


                        return deviceId
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun isValidDeviceId(deviceId: String): Boolean {
        if (deviceId.length != 32) {
            return false
        }
        return deviceId.all { it in '0'..'9' || it in 'a'..'f' }
    }

    private fun generateDeviceId(): String {
        var rawId = Settings.Secure.getString(
            GameSDK.getContext().getContentResolver(),
            Settings.Secure.ANDROID_ID
        )

        if (rawId == null || rawId.isEmpty()) {
            rawId = UUID.randomUUID().toString()
        }

        val withPackage = rawId + GameSDK.getContext().getPackageName()
        return md5(withPackage)
    }

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        val builder = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            builder.append(String.format(Locale.US, "%02x", b))
        }
        return builder.toString()
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isUsingVpn(): Boolean {
        val connectivityManager = GameSDK.getContext()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        }

        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_VPN
    }

    val statusBarHeight: Int
        get() {
            val context: Context = GameSDK.getContext()
            val resources = context.resources
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        }

    val navigationBarHeight: Int
        get() {
            val context: Context = GameSDK.getContext()
            val resources = context.resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        }
}