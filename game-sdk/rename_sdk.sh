#!/bin/bash

# SDK重命名脚本
SDK_PATH="/Users/xiongshi/Desktop/luosi001/screws/build/android/proj/game-sdk/src/main/kotlin/com/twist/screw"

cd "$SDK_PATH" || exit 1

# 创建utils包并重命名工具类
cp master/SysSheepUtils/NetworkAntCheckerSysSheepUtilsc.kt sdk/utils/NetworkUtils.kt
cp master/SysSheepUtils/DeviceBeeSysSheepUtils.kt sdk/utils/DeviceUtils.kt
cp master/SysSheepUtils/CacheBeeMgrSysSheepUtils.kt sdk/utils/CacheManager.kt
cp master/SysSheepUtils/LocalAntSysSheepUtils.kt sdk/utils/LocaleUtils.kt
cp master/SysSheepUtils/LauncherAntSysSheepUtilsc.kt sdk/utils/LauncherUtils.kt
cp master/SysSheepUtils/VibratorAntSysSheepUtils.kt sdk/utils/VibrationUtils.kt
cp master/SysSheepUtils/AliLogBeeSysSheepUtils.kt sdk/utils/AliLogUtils.kt
cp master/SysSheepUtils/FileAntSysSheepUtils.kt sdk/utils/FileUtils.kt
cp master/SysSheepUtils/AppBeeSysSheepUtils.kt sdk/utils/AppUtils.kt
cp master/SysSheepUtils/PendingAntIntentSysSheepUtils.kt sdk/utils/IntentUtils.kt
cp master/SysSheepUtils/CrashBeeSysSheepUtilsc.kt sdk/utils/CrashReporter.kt

# 更新utils包中文件的package和object/class名
cd sdk/utils
sed -i '' 's/package com\.twist\.screw\.master\.SysSheepUtils/package com.twist.screw.sdk.utils/g' *.kt
sed -i '' 's/object NetworkAntCheckerSysSheepUtilsc/object NetworkUtils/g' NetworkUtils.kt
sed -i '' 's/object DeviceBeeSysSheepUtils/object DeviceUtils/g' DeviceUtils.kt
sed -i '' 's/object CacheBeeMgrSysSheepUtils/object CacheManager/g' CacheManager.kt
sed -i '' 's/object LocalAntSysSheepUtils/object LocaleUtils/g' LocaleUtils.kt
sed -i '' 's/object LauncherAntSysSheepUtilsc/object LauncherUtils/g' LauncherUtils.kt
sed -i '' 's/object VibratorAntSysSheepUtils/object VibrationUtils/g' VibrationUtils.kt
sed -i '' 's/class AliLogBeeSysSheepUtils/class AliLogUtils/g' AliLogUtils.kt
sed -i '' 's/object FileAntSysSheepUtils/object FileUtils/g' FileUtils.kt
sed -i '' 's/object AppBeeSysSheepUtils/object AppUtils/g' AppUtils.kt
sed -i '' 's/object PendingAntIntentSysSheepUtils/object IntentUtils/g' IntentUtils.kt
sed -i '' 's/object CrashBeeSysSheepUtilsc/object CrashReporter/g' CrashReporter.kt

echo "工具类重命名完成"

