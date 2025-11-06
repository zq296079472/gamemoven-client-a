package com.twist.screw.sdk.utils

import android.content.Context
import com.aliyun.sls.android.producer.Log
import com.aliyun.sls.android.producer.LogProducerCallback
import com.aliyun.sls.android.producer.LogProducerClient
import com.aliyun.sls.android.producer.LogProducerConfig
import com.aliyun.sls.android.producer.LogProducerResult
import com.twist.screw.sdk.GameSDK
import org.json.JSONObject
import java.io.File

class AliLogUtils {
    private lateinit var config: LogProducerConfig
    private var client: LogProducerClient? = null
    private val TAG: String = "AliLogSysUtils"

    fun init(context: Context, isProgram: Boolean = false) {
        try {
            val endpoint = GameSDK.getConfig().aliLogEndpoint
            val project = GameSDK.getConfig().aliLogProject
            val logstore =
                if (isProgram) GameSDK.getConfig().aliLogProgramLogstore else GameSDK.getConfig().aliLogLogstore
            config = LogProducerConfig(context, endpoint, project, logstore)
            config.setTopic("example_topic")
            config.addTag("example", "example_tag")
            config.setDropDelayLog(0)
            config.setDropUnauthorizedLog(0)

            requestAccessKey()

            initConfig(context, isProgram)
            val callback = LogProducerCallback { resultCode, _, errorMessage, _, _ ->
                val result = LogProducerResult.fromInt(resultCode)

                when (result) {
                    LogProducerResult.LOG_PRODUCER_SEND_UNAUTHORIZED,
                    LogProducerResult.LOG_PRODUCER_PARAMETERS_INVALID -> {
                        requestAccessKey()
                    }

                    LogProducerResult.LOG_PRODUCER_WRITE_ERROR,
                    LogProducerResult.LOG_PRODUCER_PERSISTENT_ERROR -> {
                        safeInit(context)
                    }

                    LogProducerResult.LOG_PRODUCER_SEND_NETWORK_ERROR -> {
                    }

                    LogProducerResult.LOG_PRODUCER_DROP_ERROR -> {
                    }

                    LogProducerResult.LOG_PRODUCER_SEND_SERVER_ERROR,
                    LogProducerResult.LOG_PRODUCER_SEND_QUOTA_ERROR -> {
                    }

                    null -> {
                    }

                    else -> {
                    }
                }
            }

            client = LogProducerClient(config, callback)
        } catch (e: Throwable) {
            e.printStackTrace()
            CrashReporter.recordException(e)
        }
    }

    @Synchronized
    private fun safeInit(context: Context, isProgram: Boolean = false) {
        try {
            client?.destroyLogProducer()
            client = null
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            init(context.applicationContext, isProgram)
        }
    }

    private fun initConfig(context: Context, isProgram: Boolean = false) {
        config.setPersistent(1)
        val persistentFilePath: String =
            context.filesDir.path + File.separator + "log_data" + (if (isProgram) "_1" else "_2")
        config.setPersistentFilePath(persistentFilePath)
        config.setPersistentMaxFileCount(10)
        config.setPersistentMaxFileSize(5 * 1024 * 1024)
        config.setPersistentMaxLogCount(65536)
    }

    private fun requestAccessKey() {
        updateAccessKey(
            GameSDK.getConfig().aliLogAccessKeyID,
            GameSDK.getConfig().aliLogAccessKeySecret,
            null
        )
    }

    private fun updateAccessKey(
        accessKeyId: String,
        accessKeySecret: String,
        securityToken: String?
    ) {
        if (null != securityToken && "" != securityToken) {
            config.resetSecurityToken(accessKeyId, accessKeySecret, securityToken)
        } else {
            config.setAccessKeyId(accessKeyId)
            config.setAccessKeySecret(accessKeySecret)
        }
    }

    fun addLog(params: MutableMap<String?, Any?>) {
        if (client == null) {
            android.util.Log.w(TAG, "AliLogSysUtils client is null")
            return
        }
        val log: Log = Log()
        val content = JSONObject()
        params.forEach { (t, u) ->
            if (t != null && u != null) {
                content.put(t, u)
            }
        }
        log.putContent("content", content)
        client?.addLog(log)
    }
}