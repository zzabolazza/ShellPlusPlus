// 窗口管理模块
const { BrowserWindow, BrowserView } = require('electron');
const path = require('path');
const electronLog = require('electron-log');

class WindowManager {
  constructor() {
    this.mainWindow = null;
    this.views = new Map(); // 存储所有打开的视图
    this.activeView = null; // 当前活跃视图
    this.minimizedViews = new Set(); // 存储最小化的视图
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
      icon: path.join(__dirname, '../assets/icons/icon.png'),
      backgroundColor: '#f5f5f5' // 设置主窗口背景色，与main.html的body背景色一致
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

    // 设置默认背景色，避免透明背景
    // 使用多种方法确保背景色生效
    view.setBackgroundColor('#ffffff');
    
    view.webContents.once('dom-ready', () => {
      view.webContents.insertCSS(`
        html, body {
          background-color: #ffffff !important;
          margin: 0;
          padding: 0;
        }
      `);
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

    // 如果要激活的视图是最小化状态，将其从最小化状态中移除
    if (this.minimizedViews.has(name)) {
      this.minimizedViews.delete(name);
      electronLog.info(`已将视图 ${name} 从最小化状态中恢复`);
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

    // 获取窗口内容尺寸
    const contentSize = this.mainWindow.getContentSize();

    // 设置固定的底部边距，解决底部内容被遮挡的问题
    const bottomMargin = 0; // 根据实际情况调整这个值，如果底部仍被遮挡，可以增加这个值

    this.activeView.setBounds({
        x: 0,
        y: 0, // 从内容区顶部开始
        width: contentSize[0],
        height: contentSize[1] - bottomMargin
    });

    electronLog.info(`调整视图大小为: ${contentSize[0]}x${contentSize[1] - bottomMargin}`);
  }

  // 获取所有打开的应用名称
  getOpenApps() {
    // 返回所有应用，包括最小化的
    return Array.from(this.views.keys());
  }

  // 获取应用的状态信息
  getAppStatus(appName) {
    return {
      isMinimized: this.minimizedViews.has(appName),
      isActive: this.views.get(appName) === this.activeView
    };
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

  // 关闭当前活跃的视图
  closeActiveView() {
    if (!this.activeView) return;

    // 找到当前活跃视图的名称
    let activeName = null;
    for (const [name, view] of this.views.entries()) {
      if (view === this.activeView) {
        activeName = name;
        break;
      }
    }

    if (activeName) {
      this.closeApp(activeName);
      electronLog.info('已关闭当前活跃视图');
      return true;
    }

    return false;
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
    this.minimizedViews.clear();

    electronLog.info('已关闭所有应用');
  }

  // 最小化当前活跃视图
  minimizeActiveView() {
    if (!this.activeView) {
      return false;
    }

    // 获取当前活跃视图的名称
    let currentAppName = null;
    let currentIndex = -1;
    const allApps = Array.from(this.views.keys());
    
    for (let i = 0; i < allApps.length; i++) {
      if (this.views.get(allApps[i]) === this.activeView) {
        currentAppName = allApps[i];
        currentIndex = i;
        break;
      }
    }

    // 从主窗口移除视图并标记为最小化
    this.mainWindow.removeBrowserView(this.activeView);
    this.minimizedViews.add(currentAppName);
    this.activeView = null;

    // 从当前位置开始，寻找下一个未最小化的视图
    for (let i = 1; i < allApps.length; i++) {
      const nextIndex = (currentIndex + i) % allApps.length;
      const nextAppName = allApps[nextIndex];
      
      if (!this.minimizedViews.has(nextAppName)) {
        const nextView = this.views.get(nextAppName);
        this.activateView(nextAppName, nextView);
        electronLog.info(`已最小化 ${currentAppName} 并切换到下一个未最小化的视图 ${nextAppName}`);
        return true;
      }
    }

    // 如果所有视图都已最小化，显示主窗口
    if (this.mainWindow) {
      this.mainWindow.focus();
      electronLog.info(`已最小化 ${currentAppName}，所有视图已最小化，显示主窗口`);
    }

    return true;
  }

  // 恢复最小化的视图
  restoreMinimizedView() {
    // 如果没有最小化的视图，返回false
    if (this.minimizedViews.size === 0) {
      return false;
    }

    // 获取第一个最小化的视图
    const minimizedViewName = Array.from(this.minimizedViews)[0];
    const view = this.views.get(minimizedViewName);

    if (view) {
      // 从最小化集合中移除
      this.minimizedViews.delete(minimizedViewName);
      // 激活视图
      this.activateView(minimizedViewName, view);
      electronLog.info(`已恢复最小化视图: ${minimizedViewName}`);
      return true;
    }

    return false;
  }

  // 刷新当前活跃视图
  refreshActiveView() {
    if (this.activeView) {
      this.activeView.webContents.reload();
      electronLog.info('已刷新当前视图');
      return true;
    }
    return false;
  }
}

module.exports = WindowManager;
