#!/usr/bin/env python3
"""
SDK Factory 构建工具
用于自动化构建和发布定制化SDK
"""

import os
import sys
import subprocess
import yaml
import argparse
from pathlib import Path
from datetime import datetime

class Colors:
    """终端颜色"""
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'

class SDKBuilder:
    def __init__(self, root_dir):
        self.root_dir = Path(root_dir)
        self.clients_dir = self.root_dir / "clients"
        self.generated_dir = self.root_dir / "generated"
        self.gradle_wrapper = self.root_dir / "gradlew"
        
        if not self.gradle_wrapper.exists():
            print(f"{Colors.FAIL}❌ gradlew not found in {root_dir}{Colors.ENDC}")
            sys.exit(1)
    
    def load_client_config(self, client_id):
        """加载客户配置"""
        config_file = self.clients_dir / f"{client_id}.yaml"
        if not config_file.exists():
            raise FileNotFoundError(
                f"{Colors.FAIL}❌ 配置文件不存在: {config_file}{Colors.ENDC}"
            )
        
        with open(config_file, 'r', encoding='utf-8') as f:
            return yaml.safe_load(f)
    
    def validate_config(self, config):
        """验证配置完整性"""
        required_keys = [
            ("client", "id"),
            ("client", "name"),
            ("packages", "base"),
            ("packages", "public"),
            ("classes", "main"),
            ("maven", "groupId"),
            ("maven", "artifactId"),
            ("maven", "version"),
            ("maven", "repository")
        ]
        
        for keys in required_keys:
            value = config
            for key in keys:
                value = value.get(key, {})
                if not value:
                    raise ValueError(
                        f"{Colors.FAIL}❌ 缺少配置项: {'.'.join(keys)}{Colors.ENDC}"
                    )
        
        print(f"{Colors.OKGREEN}✅ 配置验证通过{Colors.ENDC}")
        return True
    
    def clean(self, client_id):
        """清理旧的生成文件"""
        client_dir = self.generated_dir / f"{client_id}-sdk"
        if client_dir.exists():
            import shutil
            shutil.rmtree(client_dir)
            print(f"{Colors.OKCYAN}🗑️  清理完成: {client_dir}{Colors.ENDC}")
    
    def generate(self, client_id=None):
        """生成源码"""
        print(f"\n{Colors.HEADER}📝 生成源码...{Colors.ENDC}")
        
        cmd = [str(self.gradle_wrapper), "generateClientSources"]
        if client_id:
            cmd.append(f"-Pclient={client_id}")
        
        result = subprocess.run(
            cmd,
            cwd=self.root_dir,
            capture_output=True,
            text=True
        )
        
        if result.returncode != 0:
            print(f"{Colors.FAIL}❌ 生成失败:{Colors.ENDC}")
            print(result.stderr)
            sys.exit(1)
        
        print(result.stdout)
        print(f"{Colors.OKGREEN}✅ 源码生成成功{Colors.ENDC}")
    
    def generate_proguard(self):
        """生成ProGuard规则"""
        print(f"\n{Colors.HEADER}🔒 生成混淆规则...{Colors.ENDC}")
        
        result = subprocess.run(
            [str(self.gradle_wrapper), "generateProguardRules"],
            cwd=self.root_dir,
            capture_output=True,
            text=True
        )
        
        if result.returncode != 0:
            print(f"{Colors.FAIL}❌ 生成失败:{Colors.ENDC}")
            print(result.stderr)
            sys.exit(1)
        
        print(result.stdout)
        print(f"{Colors.OKGREEN}✅ 混淆规则生成成功{Colors.ENDC}")
    
    def build(self, client_id):
        """构建SDK"""
        print(f"\n{Colors.HEADER}🔨 构建 {client_id} SDK...{Colors.ENDC}")
        
        task_name = f"assemble{self._capitalize(client_id)}Release"
        
        result = subprocess.run(
            [str(self.gradle_wrapper), task_name, "--stacktrace"],
            cwd=self.root_dir
        )
        
        if result.returncode != 0:
            print(f"{Colors.FAIL}❌ 构建失败{Colors.ENDC}")
            sys.exit(1)
        
        print(f"{Colors.OKGREEN}✅ 构建成功{Colors.ENDC}")
    
    def publish(self, client_id, dry_run=False):
        """发布到Maven"""
        print(f"\n{Colors.HEADER}📦 发布 {client_id} SDK 到Maven...{Colors.ENDC}")
        
        if dry_run:
            print(f"{Colors.WARNING}⚠️  DRY RUN 模式，发布到本地Maven{Colors.ENDC}")
            task_name = "publishToMavenLocal"
        else:
            task_name = f"publish{self._capitalize(client_id)}ReleasePublicationTo{self._capitalize(client_id)}Repository"
        
        result = subprocess.run(
            [str(self.gradle_wrapper), task_name],
            cwd=self.root_dir
        )
        
        if result.returncode != 0:
            print(f"{Colors.FAIL}❌ 发布失败{Colors.ENDC}")
            sys.exit(1)
        
        print(f"{Colors.OKGREEN}✅ 发布成功{Colors.ENDC}")
    
    def build_all(self, clients=None):
        """构建所有客户或指定客户列表"""
        if clients is None:
            # 自动发现所有客户
            clients = [
                f.stem for f in self.clients_dir.glob("*.yaml") 
                if f.stem != "versions"
            ]
        
        print(f"\n{Colors.BOLD}{'='*60}{Colors.ENDC}")
        print(f"{Colors.BOLD}SDK Factory - 批量构建{Colors.ENDC}")
        print(f"{Colors.BOLD}{'='*60}{Colors.ENDC}")
        print(f"客户数量: {len(clients)}")
        print(f"客户列表: {', '.join(clients)}")
        print(f"{'='*60}\n")
        
        success_count = 0
        failed_clients = []
        
        for client_id in clients:
            try:
                print(f"\n{Colors.HEADER}{'='*60}{Colors.ENDC}")
                print(f"{Colors.HEADER}开始构建: {client_id}{Colors.ENDC}")
                print(f"{Colors.HEADER}{'='*60}{Colors.ENDC}")
                
                config = self.load_client_config(client_id)
                self.validate_config(config)
                self.clean(client_id)
                
                success_count += 1
                print(f"{Colors.OKGREEN}✅ {client_id} 预处理完成{Colors.ENDC}")
                
            except Exception as e:
                print(f"{Colors.FAIL}❌ {client_id} 失败: {e}{Colors.ENDC}")
                failed_clients.append(client_id)
        
        # 统一生成源码
        self.generate()
        self.generate_proguard()
        
        # 构建每个客户
        for client_id in clients:
            if client_id not in failed_clients:
                try:
                    self.build(client_id)
                except Exception as e:
                    print(f"{Colors.FAIL}❌ {client_id} 构建失败: {e}{Colors.ENDC}")
                    failed_clients.append(client_id)
        
        # 输出总结
        print(f"\n{Colors.BOLD}{'='*60}{Colors.ENDC}")
        print(f"{Colors.BOLD}构建总结{Colors.ENDC}")
        print(f"{Colors.BOLD}{'='*60}{Colors.ENDC}")
        print(f"{Colors.OKGREEN}✅ 成功: {success_count - len(failed_clients)}/{len(clients)}{Colors.ENDC}")
        
        if failed_clients:
            print(f"{Colors.FAIL}❌ 失败: {', '.join(failed_clients)}{Colors.ENDC}")
        
        print(f"{'='*60}\n")
    
    def list_clients(self):
        """列出所有配置的客户"""
        print(f"\n{Colors.BOLD}{'='*60}{Colors.ENDC}")
        print(f"{Colors.BOLD}已配置的客户列表{Colors.ENDC}")
        print(f"{Colors.BOLD}{'='*60}{Colors.ENDC}\n")
        
        clients = [
            f.stem for f in self.clients_dir.glob("*.yaml") 
            if f.stem != "versions"
        ]
        
        for client_id in clients:
            config = self.load_client_config(client_id)
            client = config.get("client", {})
            maven = config.get("maven", {})
            packages = config.get("packages", {})
            
            print(f"{Colors.OKBLUE}📦 {client.get('name', 'Unknown')} ({client_id}){Colors.ENDC}")
            print(f"   包名: {packages.get('public', 'N/A')}")
            print(f"   Maven: {maven.get('groupId', 'N/A')}:{maven.get('artifactId', 'N/A')}:{maven.get('version', 'N/A')}")
            print(f"   仓库: {maven.get('repository', 'N/A')}")
            print()
        
        print(f"{'='*60}\n")
    
    def _capitalize(self, s):
        """首字母大写"""
        return s[0].upper() + s[1:] if s else s

def main():
    parser = argparse.ArgumentParser(
        description="SDK Factory - 自动化SDK构建和发布工具",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
示例:
  # 构建单个客户
  python build_sdk.py --client client-a
  
  # 构建并发布（本地测试）
  python build_sdk.py --client client-a --publish --dry-run
  
  # 构建并发布到远程Maven
  python build_sdk.py --client client-a --publish
  
  # 构建所有客户
  python build_sdk.py --all
  
  # 列出所有客户
  python build_sdk.py --list
        """
    )
    
    parser.add_argument(
        "--client", 
        help="客户ID (e.g. client-a)"
    )
    parser.add_argument(
        "--all", 
        action="store_true", 
        help="构建所有客户"
    )
    parser.add_argument(
        "--publish", 
        action="store_true", 
        help="发布到Maven仓库"
    )
    parser.add_argument(
        "--dry-run", 
        action="store_true", 
        help="模拟发布（发布到本地Maven测试）"
    )
    parser.add_argument(
        "--list", 
        action="store_true", 
        help="列出所有配置的客户"
    )
    
    args = parser.parse_args()
    
    # 获取脚本所在目录的父目录作为root
    script_dir = Path(__file__).parent
    root_dir = script_dir.parent
    
    builder = SDKBuilder(root_dir)
    
    # 列出客户
    if args.list:
        builder.list_clients()
        return
    
    # 构建所有客户
    if args.all:
        builder.build_all()
        
        if args.publish:
            clients = [
                f.stem for f in builder.clients_dir.glob("*.yaml") 
                if f.stem != "versions"
            ]
            for client_id in clients:
                try:
                    builder.publish(client_id, dry_run=args.dry_run)
                except Exception as e:
                    print(f"{Colors.FAIL}❌ {client_id} 发布失败: {e}{Colors.ENDC}")
        return
    
    # 构建单个客户
    if args.client:
        print(f"\n{Colors.BOLD}{'='*60}{Colors.ENDC}")
        print(f"{Colors.BOLD}SDK Factory - 构建客户: {args.client}{Colors.ENDC}")
        print(f"{Colors.BOLD}{'='*60}{Colors.ENDC}")
        print(f"时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"{'='*60}\n")
        
        config = builder.load_client_config(args.client)
        builder.validate_config(config)
        builder.clean(args.client)
        builder.generate(args.client)
        builder.generate_proguard()
        builder.build(args.client)
        
        if args.publish:
            builder.publish(args.client, dry_run=args.dry_run)
        
        print(f"\n{Colors.OKGREEN}{'='*60}{Colors.ENDC}")
        print(f"{Colors.OKGREEN}✅ {args.client} 构建完成！{Colors.ENDC}")
        print(f"{Colors.OKGREEN}{'='*60}{Colors.ENDC}\n")
        
        return
    
    # 没有指定任何选项，显示帮助
    parser.print_help()

if __name__ == "__main__":
    main()

