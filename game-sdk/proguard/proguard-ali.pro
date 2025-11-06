# ========================================
# 阿里云日志服务 (AliLog/SLS) ProGuard Rules
# ========================================
# 重要：AliLog使用了JNI和反射，必须保留所有相关类

# 核心Producer类 - 完整保留
-keep class com.aliyun.sls.android.producer.** { *; }
-keepclassmembers class com.aliyun.sls.android.producer.** { *; }

# 保留所有接口
-keep interface com.aliyun.sls.android.producer.** { *; }

# 保留LogProducerClient及其内部类
-keep class com.aliyun.sls.android.producer.LogProducerClient { *; }
-keep class com.aliyun.sls.android.producer.LogProducerClient$* { *; }

# 保留LogProducerConfig
-keep class com.aliyun.sls.android.producer.LogProducerConfig { *; }
-keep class com.aliyun.sls.android.producer.LogProducerConfig$* { *; }

# 保留LogProducerCallback
-keep class com.aliyun.sls.android.producer.LogProducerCallback { *; }
-keepclassmembers class * implements com.aliyun.sls.android.producer.LogProducerCallback {
    *;
}

# 保留Log相关类
-keep class com.aliyun.sls.android.producer.Log { *; }
-keep class com.aliyun.sls.android.producer.LogProducerException { *; }
-keep class com.aliyun.sls.android.producer.LogProducerResult { *; }

# 保留所有native方法（JNI调用）
-keepclasseswithmembernames class com.aliyun.sls.android.producer.** {
    native <methods>;
}

# 保留枚举类
-keepclassmembers class com.aliyun.sls.android.producer.** extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 不混淆类名（重要！）
-keepnames class com.aliyun.sls.android.producer.** { *; }

# 不警告找不到的类（可能是可选依赖）
-dontwarn com.aliyun.sls.android.producer.**