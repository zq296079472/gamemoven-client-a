# ========================================
# Firebase SDK ProGuard Rules
# ========================================

# Firebase核心
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Firebase Analytics
-keep class com.google.android.gms.measurement.** { *; }
-dontwarn com.google.android.gms.measurement.**

# Firebase Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keep public class * extends java.lang.Exception

# Firebase Crashlytics NDK
-keep class com.google.firebase.crashlytics.ndk.** { *; }

# 保留自定义异常类
-keep class com.twist.screw.sdk.utils.CocosError { *; }

# Firebase远程配置
-keep class com.google.firebase.remoteconfig.** { *; }
-dontwarn com.google.firebase.remoteconfig.**

# Firebase动态链接
-keep class com.google.firebase.dynamiclinks.** { *; }
-dontwarn com.google.firebase.dynamiclinks.**

