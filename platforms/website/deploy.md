# 部署到 Vercel 指南

## 方法一：通过 Vercel CLI 部署

1. 安装 Vercel CLI：
```bash
npm i -g vercel
```

2. 登录 Vercel：
```bash
vercel login
```

3. 在项目目录中部署：
```bash
cd platforms/website
vercel
```

4. 按照提示完成部署配置

## 方法二：通过 GitHub 自动部署

1. 将代码推送到 GitHub 仓库

2. 访问 [vercel.com](https://vercel.com) 并登录

3. 点击 "New Project"

4. 导入你的 GitHub 仓库

5. 选择 `platforms/website` 目录

6. 框架预设选择 "Next.js"

7. 点击 "Deploy"

## 方法三：通过 Vercel Dashboard 手动部署

1. 在 Vercel Dashboard 中创建新项目

2. 选择 "Import Git Repository"

3. 选择你的仓库和 `platforms/website` 目录

4. 配置项目设置：
   - Framework Preset: Next.js
   - Root Directory: platforms/website
   - Build Command: npm run build
   - Output Directory: .next

5. 点击 "Deploy"

## 环境变量配置

如果需要配置环境变量，可以在 Vercel Dashboard 的项目设置中添加。

## 自定义域名

部署完成后，可以在项目设置中添加自定义域名。

## 自动部署

每次推送到 GitHub 主分支时，Vercel 会自动重新部署。

## 故障排除

如果遇到部署问题：

1. 检查 `package.json` 中的脚本配置
2. 确保所有依赖都已安装
3. 查看 Vercel 构建日志
4. 检查 TypeScript 配置
