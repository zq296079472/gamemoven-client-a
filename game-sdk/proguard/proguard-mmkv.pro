# ========================================
# MMKV ProGuard Rules
# ========================================

# MMKV核心类
-keep class com.tencent.mmkv.** { *; }
-keepclassmembers class com.tencent.mmkv.** { *; }
-dontwarn com.tencent.mmkv.**

# MMKV JNI
-keep class com.tencent.mmkv.MMKV {
    native <methods>;
    long nativeHandle;
    private static *** onMMKVCRCCheckFail(***);
    private static *** onMMKVFileLengthError(***);
    private static *** mmkvLogImp(...);
    private static *** onContentChangedByOuterProcess(***);
}

# 保留MMKV用于序列化的类
-keep class com.twist.screw.sdk.model.GameStateRecord { *; }

