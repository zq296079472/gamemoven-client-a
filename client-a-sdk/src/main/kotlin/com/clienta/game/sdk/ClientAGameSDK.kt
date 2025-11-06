package com.clienta.game.sdk

import android.app.Application
import com.gaming.platform.core.GameCoreCore

/**
 * Client A Gaming Platform SDK
 * Version: 1.0.0
 * 
 * 公共API类（对外暴露）
 */
class ClientAGameSDK private constructor() {
    
    private val core = GameCoreCore()
    
    companion object {
        @Volatile
        private var instance: ClientAGameSDK? = null
        
        /**
         * 获取SDK单例
         */
        @JvmStatic
        fun getInstance(): ClientAGameSDK {
            return instance ?: synchronized(this) {
                instance ?: ClientAGameSDK().also { instance = it }
            }
        }
    }
    
    /**
     * 初始化SDK
     * 
     * @param application Application实例
     */
    fun init(application: Application) {
        core.init(application)
    }
    
    
    /**
     * 记录事件
     */
    fun logEvent(eventName: String, params: Map<String, Any>? = null) {
        core.getAnalytics().logEvent(eventName, params)
    }
    
    /**
     * 设置用户属性
     */
    fun setUserProperty(propertyName: String, value: String) {
        core.getAnalytics().setUserProperty(propertyName, value)
    }
    
    
    
    /**
     * 加载广告
     */
    fun loadAd(adUnitId: String) {
        core.getAds().loadAd(adUnitId)
    }
    
    /**
     * 展示广告
     */
    fun showAd() {
        core.getAds().showAd()
    }
    
}

