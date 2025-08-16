# Shell++

Shell++ 是一个现代化的桌面应用管理器，让您可以在一个窗口中管理和切换多个Web应用程序。

## 功能特性

### 应用管理
- ✨ 添加和管理Web应用程序
- 🚀 快速启动和切换应用
- 📱 美观的图标网格界面
- 🔄 自动获取网站图标

### 键盘快捷键
- `Alt + Q` - 打开应用选择器
- `Alt + T` - 应用切换器（循环切换）
- `Alt + W` - 关闭当前应用
- `Alt + Z` - 最小化当前应用
- `Alt + R` - 刷新当前应用
- `Alt + D` - 开发者工具
- `Ctrl + /` - 显示快捷键帮助

### 配置设置 🆕
- ⚙️ 完整的设置界面
- 🌐 **网络代理配置** - 支持 HTTP/HTTPS/SOCKS4/SOCKS5 代理
- 🎨 界面主题设置
- 🌍 多语言支持
- 🚀 启动选项配置

## 网络代理功能

Shell++ 现在支持完整的网络代理配置：

### 支持的代理类型
- **HTTP** - 标准HTTP代理
- **HTTPS** - 安全HTTPS代理  
- **SOCKS4** - SOCKS4代理协议
- **SOCKS5** - SOCKS5代理协议（推荐）

### 代理功能
- ✅ 代理服务器认证（用户名/密码）
- ✅ 代理绕过列表（支持通配符）
- ✅ 连接测试功能
- ✅ 实时配置应用
- ✅ 配置持久化保存

### 如何使用代理
1. 点击菜单栏 **设置** → **偏好设置**
2. 切换到 **网络代理** 标签页
3. 启用代理并填写服务器信息
4. 点击 **测试连接** 验证配置
5. 点击 **保存设置** 应用配置

## 安装和运行

### 开发环境
```bash
# 安装依赖
npm install

# 启动应用
npm start

# 打包应用
npm run package-win    # Windows
npm run package-mac    # macOS  
npm run package-linux  # Linux
```

### 系统要求
- Node.js 16+
- Electron 35+
- Windows 10+, macOS 10.14+, 或 Ubuntu 18.04+

## 项目结构

```
src/
├── main/                 # 主进程代码
│   ├── index.js         # 应用入口
│   ├── configManager.js # 配置管理器 🆕
│   ├── menuManager.js   # 菜单管理
│   ├── windowManager.js # 窗口管理
│   └── ...
├── renderer/            # 渲染进程页面
│   ├── main.html       # 主界面
│   ├── config.html     # 配置界面 🆕
│   └── ...
└── assets/             # 资源文件
    └── icons/          # 应用图标
```

## 配置文件

配置文件自动保存在：
- **Windows**: `%APPDATA%/Shell++/config.json`
- **macOS**: `~/Library/Application Support/Shell++/config.json`
- **Linux**: `~/.config/Shell++/config.json`

## 贡献

欢迎提交Issue和Pull Request来帮助改进Shell++！

## 许可证

MIT License