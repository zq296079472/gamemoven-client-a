#!/bin/bash
# 自动生成客户SDK脚本
# 用法: ./generate-client-sdk.sh client-b

set -e

CLIENT_ID=$1

if [ -z "$CLIENT_ID" ]; then
    echo "❌ 错误: 请指定客户ID"
    echo "用法: $0 <client-id>"
    echo "示例: $0 client-b"
    exit 1
fi

CONFIG_FILE="sdk-factory/clients/${CLIENT_ID}.yaml"
if [ ! -f "$CONFIG_FILE" ]; then
    echo "❌ 错误: 配置文件不存在: $CONFIG_FILE"
    exit 1
fi

echo "📋 读取配置: $CONFIG_FILE"

# 使用Python解析YAML并生成SDK
python3 - <<EOF
import yaml
import os
import shutil
from pathlib import Path

# 读取配置
with open('$CONFIG_FILE', 'r', encoding='utf-8') as f:
    config = yaml.safe_load(f)

client_id = config['client']['id']
sdk_name = config['classes']['main']
base_package = config['packages']['base']
public_package = config['packages']['public']
group_id = config['maven']['groupId']
artifact_id = config['maven']['artifactId']
version = config['maven']['version']

print(f"✅ 客户ID: {client_id}")
print(f"✅ SDK类名: {sdk_name}")
print(f"✅ 公开包名: {public_package}")
print(f"✅ Maven坐标: {group_id}:{artifact_id}:{version}")

# 创建目标目录
target_dir = Path(f'{client_id}-sdk')
if target_dir.exists():
    print(f"⚠️  目标目录已存在，将被覆盖: {target_dir}")
    shutil.rmtree(target_dir)

# 复制client-a-sdk作为模板
shutil.copytree('client-a-sdk', target_dir)
print(f"✅ 已复制模板到: {target_dir}")

# 替换源码文件中的占位符
src_dir = target_dir / 'src/main/kotlin'

# 删除原有的包结构
if (src_dir / 'com/clienta').exists():
    shutil.rmtree(src_dir / 'com/clienta')
if (src_dir / 'com/gaming').exists():
    shutil.rmtree(src_dir / 'com/gaming')

# 创建新的包结构
public_package_path = src_dir / public_package.replace('.', '/')
public_package_path.mkdir(parents=True, exist_ok=True)

# 生成主SDK类
main_sdk_content = f'''package {public_package}

import android.app.Application
import android.content.Context
import com.twist.screw.sdk.GameSDK
import com.twist.screw.sdk.IAliLogParamsBuilder
import com.twist.screw.sdk.ICocosCallback
import com.twist.screw.sdk.IUICallback
import com.twist.screw.sdk.SDKConfig
import com.twist.screw.sdk.bridge.IConverterDelegate

/**
 * {config['client']['name']} - {config['client']['description']}
 * 
 * @version {version}
 * @author {config['branding']['display_name']} Team
 */
class {sdk_name} private constructor() {{
    
    companion object {{
        @Volatile
        private var instance: {sdk_name}? = null
        
        /**
         * 获取SDK单例
         */
        @JvmStatic
        fun getInstance(): {sdk_name} {{
            return instance ?: synchronized(this) {{
                instance ?: {sdk_name}().also {{ instance = it }}
            }}
        }}
    }}
    
    /**
     * 初始化{config['branding']['display_name']} SDK
     */
    fun init(
        app: Application,
        config: SDKConfig,
        cocosCallback: ICocosCallback? = null,
        uiCallback: IUICallback? = null,
        aliLogParamsBuilder: IAliLogParamsBuilder? = null,
        converterDelegate: IConverterDelegate? = null
    ) {{
        GameSDK.init(
            app = app,
            config = config,
            cocosCallback = cocosCallback,
            uiCallback = uiCallback,
            aliLogParamsBuilder = aliLogParamsBuilder,
            converterDelegate = converterDelegate
        )
    }}
    
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
}}
'''

# 写入主SDK类
main_sdk_file = public_package_path / f'{sdk_name}.kt'
with open(main_sdk_file, 'w', encoding='utf-8') as f:
    f.write(main_sdk_content)

print(f"✅ 已生成主SDK类: {main_sdk_file}")

# 更新build.gradle
build_gradle = target_dir / 'build.gradle'
with open(build_gradle, 'r', encoding='utf-8') as f:
    content = f.read()

# 替换配置
content = content.replace('namespace "com.clienta.game.sdk"', f'namespace "{public_package}"')
content = content.replace("groupId = 'com.clienta'", f"groupId = '{group_id}'")
content = content.replace("artifactId = 'client-a-sdk'", f"artifactId = '{artifact_id}'")
content = content.replace("version = '1.0.9'", f"version = '{version}'")
content = content.replace("name = 'Client A Game SDK'", f"name = '{config['maven']['pom']['name']}'")
content = content.replace("description = 'Client A Gaming Platform SDK'", f"description = '{config['maven']['pom']['description']}'")

with open(build_gradle, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"✅ 已更新build.gradle")

# 更新settings.gradle.kts
settings_file = Path('settings.gradle.kts')
with open(settings_file, 'r', encoding='utf-8') as f:
    settings_content = f.read()

if f'include(":{client_id}-sdk")' not in settings_content:
    # 在include(":game-sdk")后面添加新模块
    settings_content = settings_content.replace(
        'include(":game-sdk")',
        f'include(":game-sdk")\\ninclude(":{client_id}-sdk")'
    )
    with open(settings_file, 'w', encoding='utf-8') as f:
        f.write(settings_content)
    print(f"✅ 已添加模块到settings.gradle.kts")

print(f"")
print(f"🎉 客户{client_id}的SDK已生成!")
print(f"")
print(f"📦 生成的SDK:")
print(f"   - 模块目录: {target_dir}")
print(f"   - 主类: {public_package}.{sdk_name}")
print(f"   - Maven坐标: {group_id}:{artifact_id}:{version}")
print(f"")
print(f"🚀 下一步:")
print(f"   1. cd {target_dir.parent}")
print(f"   2. ./gradlew :{client_id}-sdk:assembleRelease")
print(f"   3. ./gradlew :{client_id}-sdk:publishToMavenLocal")
print(f"")

EOF

echo ""
echo "✅ 客户B的SDK生成完成!"

