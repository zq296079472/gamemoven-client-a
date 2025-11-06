package {{CLIENT_PACKAGE}}

import android.app.Application
import {{BASE_PACKAGE}}.{{SDK_NAME}}Core

/**
 * {{CLIENT_NAME}} SDK
 * Version: {{VERSION}}
 * 
 * 公共API类（对外暴露）
 */
class {{CLIENT_SDK_NAME}} private constructor() {
    
    private val core = {{SDK_NAME}}Core()
    
    companion object {
        @Volatile
        private var instance: {{CLIENT_SDK_NAME}}? = null
        
        /**
         * 获取SDK单例
         */
        @JvmStatic
        fun getInstance(): {{CLIENT_SDK_NAME}} {
            return instance ?: synchronized(this) {
                instance ?: {{CLIENT_SDK_NAME}}().also { instance = it }
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
    
    {{#IF_FEATURE_ANALYTICS}}
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
    {{/IF_FEATURE_ANALYTICS}}
    
    {{#IF_FEATURE_ADS}}
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
    {{/IF_FEATURE_ADS}}
}

