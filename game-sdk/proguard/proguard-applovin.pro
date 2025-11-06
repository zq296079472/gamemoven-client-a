# ========================================
# AppLovin MAX SDK ProGuard Rules
# ========================================

# AppLovin核心
-keep class com.applovin.** { *; }
-keepclassmembers class com.applovin.** { *; }
-dontwarn com.applovin.**

# AppLovin适配器
-keep class com.applovin.mediation.** { *; }
-keepclassmembers class com.applovin.mediation.** { *; }

# AppLovin广告加载器
-keepclassmembers class * implements com.applovin.mediation.MaxAdListener {
    *;
}
-keepclassmembers class * implements com.applovin.mediation.MaxAdViewAdListener {
    *;
}
-keepclassmembers class * implements com.applovin.mediation.MaxRewardedAdListener {
    *;
}
-keepclassmembers class * implements com.applovin.mediation.MaxAdRevenueListener {
    *;
}

# AppLovin原生广告
-keep class com.applovin.mediation.nativeAds.** { *; }

# Google UMP (User Messaging Platform) - AppLovin需要
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

