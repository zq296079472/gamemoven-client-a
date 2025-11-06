package com.gaming.platform.core

import android.app.Application
import android.content.Context
import android.util.Log
import com.gaming.platform.core.analytics.AnalyticsEngine
import com.gaming.platform.core.ads.AdEngine

/**
 * Client A Gaming Platform SDK Core
 * Version: 1.0.0
 * 
 * 核心SDK实现（内部使用，不对外暴露）
 */
internal class GameCoreCore {
    
    private val TAG = "GameCoreCore"
    
    private var isInitialized = false
    
    
    private var _analytics: AnalyticsEngine? = null
    
    
    
    private var _ads: AdEngine? = null
    
    
    /**
     * 初始化SDK核心
     */
    fun init(application: Application) {
        if (isInitialized) {
            Log.w(TAG, "SDK already initialized")
            return
        }
        
        Log.i(TAG, "Initializing Client A Gaming Platform SDK v1.0.0")
        
        
        _analytics = AnalyticsEngine()
        _analytics?.init(application)
        
        
        
        _ads = AdEngine()
        _ads?.init(application)
        
        
        isInitialized = true
        
        Log.i(TAG, "Client A Gaming Platform SDK initialized successfully")
    }
    
    /**
     * 获取分析模块
     */
    
    fun getAnalytics(): AnalyticsEngine {
        checkInitialized()
        return _analytics ?: throw IllegalStateException("Analytics not initialized")
    }
    
    
    /**
     * 获取广告模块
     */
    
    fun getAds(): AdEngine {
        checkInitialized()
        return _ads ?: throw IllegalStateException("Ads not initialized")
    }
    
    
    /**
     * 检查是否已初始化
     */
    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("SDK not initialized. Call init() first.")
        }
    }
}

