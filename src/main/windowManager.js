// 窗口管理模块
const { BrowserWindow, BrowserView } = require('electron');
const path = require('path');
const electronLog = require('electron-log');

class WindowManager {
  constructor() {
    this.mainWindow = null;
    this.views = new Map(); // 存储所有打开的视图
    this.activeView = null; // 当前活跃视图
  }

  // 创建主窗口
  createMainWindow() {
    if (this.mainWindow && !this.mainWindow.isDestroyed()) {
      this.mainWindow.focus();
      return this.mainWindow;
    }

    // 创建主窗口
    this.mainWindow = new BrowserWindow({
      width: 1200,
      height: 800,
      webPreferences: {
        preload: path.join(__dirname, 'preload.js'),
        nodeIntegration: false,
        contextIsolation: true,
        sandbox: true
      },
      title: 'Shell++',
      frame: true, // 使用系统标准窗口框架
      icon: path.join(__dirname, '../assets/icons/icon.png')
    });

    // 增加最大监听器数量，避免内存泄漏警告
    this.mainWindow.setMaxListeners(20);

    // 加载主窗口界面
    const mainHtmlPath = path.join(__dirname, '../renderer/main.html');
    console.log('Loading main HTML from:', mainHtmlPath);
    this.mainWindow.loadFile(mainHtmlPath);

    // 打开开发者工具以便调试
    // this.mainWindow.webContents.openDevTools();

    // 监听窗口大小变化事件
    this.mainWindow.on('resize', () => {
      this.resizeActiveView();
    });

    // 窗口关闭时清理资源
    this.mainWindow.on('closed', () => {
      this.mainWindow = null;
      this.views.clear();
      this.activeView = null;
    });

    electronLog.info('主窗口已创建');
    return this.mainWindow;
  }

  // 创建或切换到应用视图
  switchToApp(appInfo) {
    const { name, url } = appInfo;

    // 确保主窗口存在
    if (!this.mainWindow || this.mainWindow.isDestroyed()) {
      this.createMainWindow();
    }

    // 检查视图是否已存在
    if (this.views.has(name)) {
      const existingView = this.views.get(name);
      this.activateView(name, existingView);
      return;
    }

    // 创建新视图
    const view = new BrowserView({
      webPreferences: {
        preload: path.join(__dirname, 'viewPreload.js'),
        nodeIntegration: false,
        contextIsolation: true,
        sandbox: true
      }
    });

    // 加载URL
    view.webContents.loadURL(url).catch(err => {
      electronLog.error(`加载URL失败 ${url}:`, err);
    });

    // 存储视图
    this.views.set(name, view);

    // 激活视图
    this.activateView(name, view);

    electronLog.info(`已创建并激活视图: ${name}`);
  }

  // 激活视图
  activateView(name, view) {
    // 移除当前活跃视图
    if (this.activeView) {
      this.mainWindow.removeBrowserView(this.activeView);
    }

    // 设置新的活跃视图
    this.mainWindow.addBrowserView(view);
    this.activeView = view;
    this.resizeActiveView();

    // 通知渲染进程更新活跃应用
    this.mainWindow.webContents.send('active-app-changed', name);

    electronLog.info(`已激活视图: ${name}`);
  }

  // 调整活跃视图大小
  resizeActiveView() {
    if (!this.mainWindow || !this.activeView) return;

    const bounds = this.mainWindow.getBounds();

    // 获取系统窗口框架高度
    // 在Windows上，标准窗口框架高度通常为32px
    // 在macOS上，标准窗口框架高度通常为28px
    // 在Linux上，这取决于窗口管理器，但通常在28-32px之间
    const frameHeight = 0;

    this.activeView.setBounds({
      x: 0,
      y: frameHeight,
      width: bounds.width,
      height: bounds.height - frameHeight
    });

    console.log('Resized active view to:', {
      width: bounds.width,
      height: bounds.height - frameHeight
    });
  }

  // 获取所有打开的应用名称
  getOpenApps() {
    return Array.from(this.views.keys());
  }

  // 切换到下一个应用
  switchToNextApp() {
    const apps = this.getOpenApps();
    if (apps.length <= 1) return;

    let currentIndex = -1;
    if (this.activeView) {
      // 找到当前活跃应用的索引
      for (let i = 0; i < apps.length; i++) {
        if (this.views.get(apps[i]) === this.activeView) {
          currentIndex = i;
          break;
        }
      }
    }

    // 计算下一个应用的索引
    const nextIndex = (currentIndex + 1) % apps.length;
    const nextApp = apps[nextIndex];

    // 激活下一个应用
    this.activateView(nextApp, this.views.get(nextApp));
  }

  // 关闭应用
  closeApp(name) {
    if (!this.views.has(name)) return;

    const view = this.views.get(name);

    // 如果是当前活跃视图，先移除
    if (this.activeView === view) {
      this.mainWindow.removeBrowserView(view);
      this.activeView = null;

      // 如果还有其他视图，切换到第一个
      const apps = this.getOpenApps();
      if (apps.length > 1) {
        const nextApp = apps[0] === name ? apps[1] : apps[0];
        this.activateView(nextApp, this.views.get(nextApp));
      }
    }

    // 销毁视图并从集合中移除
    view.webContents.destroy();
    this.views.delete(name);

    electronLog.info(`已关闭应用: ${name}`);
  }

  // 关闭所有应用
  closeAllApps() {
    for (const [name, view] of this.views.entries()) {
      if (this.activeView === view) {
        this.mainWindow.removeBrowserView(view);
      }
      view.webContents.destroy();
    }

    this.views.clear();
    this.activeView = null;

    electronLog.info('已关闭所有应用');
  }
}

module.exports = WindowManager;
