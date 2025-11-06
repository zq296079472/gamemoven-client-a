# ==================== Client A Gaming Platform SDK 混淆规则 ====================
# 生成时间: Thu Nov 06 16:37:33 CST 2025
# SDK版本: 1.0.0

# ==================== 1. 保留公共API ====================
-keep public class com.clienta.game.sdk.ClientAGameSDK {
    public <methods>;
    public <fields>;
}

# ✅ 保留公共接口
-keep public interface com.clienta.game.sdk.** { *; }

# ✅ 保留注解
-keepattributes *Annotation*

# ==================== 2. 混淆内部实现 ====================
-keep,allowobfuscation class com.gaming.platform.core.** { *; }
-keep,allowobfuscation class com.clienta.game.internal.** { *; }

# ==================== 3. 激进混淆选项 ====================
-overloadaggressively
-repackageclasses ''
-allowaccessmodification
-mergeinterfacesaggressively
-optimizationpasses 5

# ==================== 4. 自定义混淆字典 ====================
# 注意：混淆字典文件需要在AAR包中才能使用
# 暂时移除字典引用，使用ProGuard默认混淆策略
# TODO: 如需自定义字典，需将字典文件打包进AAR
# -obfuscationdictionary clients/dict-clienta.txt
# -classobfuscationdictionary clients/dict-clienta.txt
# -packageobfuscationdictionary clients/dict-clienta.txt

# ==================== 5. 自定义Seed ====================
-adaptclassstrings clienta_unique_seed_2024_v1

# ==================== 6. 移除调试信息 ====================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# ==================== 7. Kotlin相关 ====================
-keep class kotlin.Metadata { *; }
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ==================== 8. Android组件保留 ====================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ==================== 9. Cocos引擎保护（关键!避免运行时NoSuchMethodError）====================
-keep class com.cocos.** { *; }
-keep class org.cocos2dx.** { *; }
-keep class com.cocos.lib.** { *; }
-keep class com.cocos.service.** { *; }

# 特别保留JS桥接类（防止混淆导致运行时错误）
-keepclassmembers class com.cocos.lib.CocosJavascriptJavaBridge {
    public static <methods>;
}
-keepclassmembers class com.cocos.lib.CocosHelper {
    public static <methods>;
}

# ==================== 10. 第三方SDK规则 ====================
-dontwarn **

# ==================== 11. 保留注解标记 ====================
-keep class * {
    @androidx.annotation.Keep *;
}

-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
