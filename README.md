# ShellPlusPlus

一个跨平台的Shell增强工具，支持Android和PC平台。

## 项目结构

```
ShellPlusPlus/
├── platforms/
│   ├── android/          # Android平台代码
│   │   ├── app/          # Android应用代码
│   │   ├── build.gradle  # Android构建配置
│   │   └── ...
│   └── pc/              # PC平台代码
│       ├── src/          # PC端源代码
│       ├── package.json  # Node.js依赖配置
│       └── ...
├── README.md             # 项目说明文档
└── ...
```

## 平台说明

### Android平台
- 基于Kotlin和Jetpack Compose开发
- 提供应用切换和手势识别功能
- 支持无障碍服务

### PC平台
- 基于Electron开发
- 提供跨平台桌面应用体验
- 支持应用管理和快捷操作

## 开发说明

### Android开发
```bash
cd platforms/android
./gradlew clean build
```

### PC开发
```bash
cd platforms/pc
npm install
npm start
```

## 许可证

本项目采用MIT许可证。