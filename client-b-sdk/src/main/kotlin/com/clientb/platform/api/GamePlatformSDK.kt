package com.clientb.platform.api

import android.app.Application
import android.content.Context
import com.twist.screw.sdk.GameSDK
import com.twist.screw.sdk.IAliLogParamsBuilder
import com.twist.screw.sdk.ICocosCallback
import com.twist.screw.sdk.IUICallback
import com.twist.screw.sdk.SDKConfig
import com.twist.screw.sdk.bridge.IConverterDelegate

/**
 * GamePlatform Pro - 专为Client B定制的专业游戏平台SDK
 * 
 * @version 1.0.0
 * @author GamePlatform Pro Team
 */
class GamePlatformSDK private constructor() {
    
    companion object {
        @Volatile
        private var instance: GamePlatformSDK? = null
        
        /**
         * 获取SDK单例
         */
        @JvmStatic
        fun getInstance(): GamePlatformSDK {
            return instance ?: synchronized(this) {
                instance ?: GamePlatformSDK().also { instance = it }
            }
        }
    }
    
    /**
     * 初始化GamePlatform Pro SDK
     */
    fun init(
        app: Application,
        config: SDKConfig,
        cocosCallback: ICocosCallback? = null,
        uiCallback: IUICallback? = null,
        aliLogParamsBuilder: IAliLogParamsBuilder? = null,
        converterDelegate: IConverterDelegate? = null
    ) {
        GameSDK.init(
            app = app,
            config = config,
            cocosCallback = cocosCallback,
            uiCallback = uiCallback,
            aliLogParamsBuilder = aliLogParamsBuilder,
            converterDelegate = converterDelegate
        )
    }
    
    /**
     * SDK是否已初始化
     */
    val isInitialized: Boolean
        get() = GameSDK.isInitialized
    
    /**
     * 获取Application上下文
     */
    fun getContext(): Context = GameSDK.getContext()
    
    /**
     * 获取SDK配置
     */
    fun getConfig(): SDKConfig = GameSDK.getConfig()
    
    /**
     * 获取Cocos回调
     */
    fun getCocosCallback(): ICocosCallback? = GameSDK.getCocosCallback()
    
    /**
     * 获取UI回调
     */
    fun getUICallback(): IUICallback? = GameSDK.getUICallback()
    
    /**
     * 获取AliLog参数构建器
     */
    fun getAliLogParamsBuilder(): IAliLogParamsBuilder? = GameSDK.getAliLogParamsBuilder()
    
    /**
     * 获取转换器委托
     */
    fun getConverterDelegate(): IConverterDelegate? = GameSDK.getConverterDelegate()
}
