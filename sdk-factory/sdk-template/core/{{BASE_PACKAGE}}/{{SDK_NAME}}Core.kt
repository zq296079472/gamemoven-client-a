package {{BASE_PACKAGE}}

import android.app.Application
import android.content.Context
import android.util.Log
import {{BASE_PACKAGE}}.analytics.{{ANALYTICS_CLASS}}
import {{BASE_PACKAGE}}.ads.{{ADS_CLASS}}

/**
 * {{CLIENT_NAME}} SDK Core
 * Version: {{VERSION}}
 * 
 * 核心SDK实现（内部使用，不对外暴露）
 */
internal class {{SDK_NAME}}Core {
    
    private val TAG = "{{SDK_NAME}}Core"
    
    private var isInitialized = false
    
    {{#IF_FEATURE_ANALYTICS}}
    private var _analytics: {{ANALYTICS_CLASS}}? = null
    {{/IF_FEATURE_ANALYTICS}}
    
    {{#IF_FEATURE_ADS}}
    private var _ads: {{ADS_CLASS}}? = null
    {{/IF_FEATURE_ADS}}
    
    /**
     * 初始化SDK核心
     */
    fun init(application: Application) {
        if (isInitialized) {
            Log.w(TAG, "SDK already initialized")
            return
        }
        
        Log.i(TAG, "Initializing {{CLIENT_NAME}} SDK v{{VERSION}}")
        
        {{#IF_FEATURE_ANALYTICS}}
        _analytics = {{ANALYTICS_CLASS}}()
        _analytics?.init(application)
        {{/IF_FEATURE_ANALYTICS}}
        
        {{#IF_FEATURE_ADS}}
        _ads = {{ADS_CLASS}}()
        _ads?.init(application)
        {{/IF_FEATURE_ADS}}
        
        isInitialized = true
        
        Log.i(TAG, "{{CLIENT_NAME}} SDK initialized successfully")
    }
    
    /**
     * 获取分析模块
     */
    {{#IF_FEATURE_ANALYTICS}}
    fun getAnalytics(): {{ANALYTICS_CLASS}} {
        checkInitialized()
        return _analytics ?: throw IllegalStateException("Analytics not initialized")
    }
    {{/IF_FEATURE_ANALYTICS}}
    
    /**
     * 获取广告模块
     */
    {{#IF_FEATURE_ADS}}
    fun getAds(): {{ADS_CLASS}} {
        checkInitialized()
        return _ads ?: throw IllegalStateException("Ads not initialized")
    }
    {{/IF_FEATURE_ADS}}
    
    /**
     * 检查是否已初始化
     */
    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("SDK not initialized. Call init() first.")
        }
    }
}

