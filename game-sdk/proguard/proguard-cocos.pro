# Cocos Creator引擎混淆规则
# 防止运行时NoSuchMethodError

# 保留所有Cocos相关类
-keep class com.cocos.** { *; }
-keep class org.cocos2dx.** { *; }
-keep class com.cocos.lib.** { *; }
-keep class com.cocos.service.** { *; }

# 特别保留JS桥接类（关键!防止混淆导致运行时错误）
-keepclassmembers class com.cocos.lib.CocosJavascriptJavaBridge {
    public static <methods>;
}

-keepclassmembers class com.cocos.lib.CocosHelper {
    public static <methods>;
}

# 保留Native方法
-keepclasseswithmembernames class * {
    native <methods>;
}

