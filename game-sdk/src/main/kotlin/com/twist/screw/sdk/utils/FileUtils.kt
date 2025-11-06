package com.twist.screw.sdk.utils

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {
    fun fileToByte(file: File?): ByteArray {
        if (file == null || !file.exists()) {
            return ByteArray(0)
        }
        var buffer = ByteArray(0)
        var fis: FileInputStream? = null
        var bos: ByteArrayOutputStream? = null
        try {
            fis = FileInputStream(file)
            bos = ByteArrayOutputStream()
            val b = ByteArray(1024)
            var n = fis!!.read(b)
            while (n != -1) {
                bos.write(b, 0, n)
                n = fis!!.read(b)
            }
            fis!!.close()
            bos.close()
            buffer = bos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fis?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                bos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return buffer
    }

    fun saveByteByFile(data: ByteArray?, file: File?) {
        if (file == null || data == null) {
            return
        }

        var fileOutputStream: FileOutputStream? = null
        try {
            val parentDir: File = file.getParentFile()
            if (parentDir != null && !parentDir.exists()) {
                val success: Boolean = parentDir.mkdirs()
                if (!success) {
                    System.err.println("Failed to create directory: " + parentDir.getAbsolutePath())
                    return
                }
            }

            if (!file.exists()) {
                val created: Boolean = file.createNewFile()
                if (!created) {
                    System.err.println("Failed to create file: " + file.getAbsolutePath())
                    return
                }
            }

            fileOutputStream = FileOutputStream(file)
            fileOutputStream!!.write(data)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}