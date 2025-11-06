package com.twist.screw.sdk.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object AppUtils {

    fun md5(s: String): String {
        try {
            val digest = MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()
            val buffer = StringBuilder()
            for (aMessageDigest in messageDigest)
                buffer.append(Integer.toHexString(0xFF and aMessageDigest.toInt()))
            return buffer.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return ""
    }

    fun sdbHash(s: String): Int {
        var hash = 0
        for (c in s.toCharArray()) {
            hash = c.toInt() + (hash shl 6) + (hash shl 16) - hash
        }
        return (hash and Int.MAX_VALUE)
    }

}