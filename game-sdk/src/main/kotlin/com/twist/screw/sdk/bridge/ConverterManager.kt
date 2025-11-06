package com.twist.screw.sdk.bridge

/**
 * Tiger转换器管理器（SDK框架层）
 *
 * 管理所有Tiger转换器的注册和获取
 * 具体的转换器实现由APP层提供
 */
class ConverterManager private constructor() {
    companion object {
        @Volatile
        private var instance: ConverterManager? = null

        @JvmStatic
        fun getInstance(): ConverterManager {
            if (instance == null) {
                instance = ConverterManager()
            }
            return instance!!
        }
    }

    private var delegate: IConverterDelegate? = null

    /**
     * 设置转换器委托（可选，如果不设置会自动创建）
     */
    fun setDelegate(delegate: IConverterDelegate) {
        this.delegate = delegate
    }

    /**
     * 添加转换器（自动从GameSDK获取delegate）
     */
    fun addAdapterConvert(): Map<String, BaseConverter> {
        // 如果没有设置delegate，从GameSDK获取
        if (delegate == null) {
            delegate = com.twist.screw.sdk.GameSDK.getConverterDelegate()
        }
        return delegate?.addAdapterConvert() ?: emptyMap()
    }

    /**
     * 获取所有转换器
     */
    fun getConvert(): Map<String, BaseConverter> {
        return delegate?.getConvertMap() ?: emptyMap()
    }
}

/**
 * 转换器委托接口
 * APP层需要实现此接口来提供具体的转换器
 */
interface IConverterDelegate {
    /**
     * 添加并返回所有转换器
     */
    fun addAdapterConvert(): Map<String, BaseConverter>

    /**
     * 获取转换器Map
     */
    fun getConvertMap(): Map<String, BaseConverter>
}
