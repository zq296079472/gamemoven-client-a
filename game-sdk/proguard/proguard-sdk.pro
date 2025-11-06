# ========================================
# Game SDK ProGuard Rules
# ========================================
# 此文件包含SDK自身的混淆规则
# 会自动应用到使用此SDK的APP中
# ========================================

# 基础配置
-optimizationpasses 5
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions

# ========================================
# SDK核心类保留
# ========================================

# 保留SDK主入口
-keep public class com.twist.screw.sdk.GameSDK {
    public *;
}

# 保留SDK配置类
-keep public class com.twist.screw.sdk.SDKConfig { *; }
-keep public class com.twist.screw.sdk.AnalyticsKeysConfig { *; }

# 保留SDK回调接口
-keep public interface com.twist.screw.sdk.ICocosCallback { *; }
-keep public interface com.twist.screw.sdk.IUICallback { *; }
-keep public interface com.twist.screw.sdk.IAliLogParamsBuilder { *; }

# ========================================
# 工具类保留
# ========================================

# 保留所有工具类的公共方法
-keep public class com.twist.screw.sdk.utils.** {
    public *;
}

# CrashReporter - 崩溃上报必须保留
-keep class com.twist.screw.sdk.utils.CrashReporter { *; }
-keep class com.twist.screw.sdk.utils.CocosError { *; }

# DeviceUtils - 设备信息获取
-keep class com.twist.screw.sdk.utils.DeviceUtils {
    public *;
}

# CacheManager - 数据持久化
-keep class com.twist.screw.sdk.utils.CacheManager {
    public *;
}

# ========================================
# 广告模块保留
# ========================================

# 广告管理器
-keep public class com.twist.screw.sdk.ads.AdManager { *; }
-keep public class com.twist.screw.sdk.ads.AdType { *; }

# MAX广告适配器
-keep public class com.twist.screw.sdk.ads.max.** {
    public *;
}

# ========================================
# 分析模块保留
# ========================================

# 分析管理器和工具
-keep public class com.twist.screw.sdk.analytics.AnalyticsManager { *; }
-keep public class com.twist.screw.sdk.analytics.AnalyticsUtils { *; }
-keep public interface com.twist.screw.sdk.analytics.IAnalyticsStrategy { *; }

# 分析策略实现
-keep public class com.twist.screw.sdk.analytics.strategy.** { *; }

# Bundle扩展
-keep public class com.twist.screw.sdk.analytics.ext.** { *; }

# ========================================
# Cocos桥接模块保留
# ========================================

# 转换器管理器和基类
-keep public class com.twist.screw.sdk.bridge.ConverterManager { *; }
-keep public class com.twist.screw.sdk.bridge.BaseConverter { *; }
-keep public interface com.twist.screw.sdk.bridge.IConverterDelegate { *; }
-keep public interface com.twist.screw.sdk.bridge.ILifecycleAware { *; }
-keep public class com.twist.screw.sdk.bridge.LifecycleDispatcher { *; }

# Cocos参数类 - 关键类，不能混淆
-keep public class com.twist.screw.sdk.bridge.cocos.CocosNativeParams {
    public *;
    # 保留所有字段和方法，因为会被Cocos调用
    private *;
}

# ========================================
# 数据模型保留
# ========================================

# 游戏状态记录 - 用于数据持久化
-keep class com.twist.screw.sdk.model.GameStateRecord { *; }

# ========================================
# 网络接收器保留
# ========================================

# 网络状态接收器 - 系统会反射调用
-keep public class com.twist.screw.sdk.receiver.NetworkStateReceiver { *; }

# ========================================
# 序列化和反序列化
# ========================================

# 保留所有包含@Keep注解的类和成员
-keep @androidx.annotation.Keep class * { *; }
-keep class * {
    @androidx.annotation.Keep *;
}

# ========================================
# Kotlin相关
# ========================================

# Kotlin元数据
-keep class kotlin.Metadata { *; }

# Kotlin反射
-keepclassmembers class kotlin.** {
    *;
}

# Kotlin协程
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ========================================
# 通用规则
# ========================================

# 保留所有native方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

