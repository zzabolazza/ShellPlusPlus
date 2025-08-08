// 主入口文件
const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const electronLog = require('electron-log');

// 导入自定义模块
const AppManager = require('./appManager');
const WindowManager = require('./windowManager');
const AppSwitcher = require('./appSwitcher');
const ShortcutManager = require('./shortcutManager');
const MenuManager = require('./menuManager');

// 配置日志
electronLog.transports.file.level = 'info';
electronLog.info('应用启动');

// 单例应用
const gotTheLock = app.requestSingleInstanceLock();
if (!gotTheLock) {
  app.quit();
  return;
}

// 创建模块实例
let appManager;
let windowManager;
let appSwitcher;
let shortcutManager;
let menuManager;

// 设置IPC处理程序
function setupIpcHandlers() {
  // 窗口控制
  ipcMain.on('minimize-window', () => {
    const mainWindow = windowManager.mainWindow;
    if (mainWindow) mainWindow.minimize();
  });

  ipcMain.on('toggle-maximize-window', () => {
    const mainWindow = windowManager.mainWindow;
    if (!mainWindow) return;

    if (mainWindow.isMaximized()) {
      mainWindow.unmaximize();
    } else {
      mainWindow.maximize();
    }
  });

  ipcMain.on('close-window', () => {
    const mainWindow = windowManager.mainWindow;
    if (mainWindow) mainWindow.close();
  });

  // 应用管理 - 确保这些处理程序只在这里注册一次
  ipcMain.handle('get-apps', () => {
    return appManager.getApps();
  });

  ipcMain.handle('get-open-apps', () => {
    return windowManager.getOpenApps();
  });

  ipcMain.handle('switch-to-app', (_, appName) => {
    const app = appManager.getApps().find(a => a.name === appName);
    if (app) {
      windowManager.switchToApp(app);
      return true;
    }
    return false;
  });

  ipcMain.handle('close-app', (_, appName) => {
    windowManager.closeApp(appName);
    return true;
  });

  ipcMain.handle('show-app-selector', () => {
    appSwitcher.showAppSelector();
    return true;
  });

  // 视图API
  ipcMain.handle('get-current-app-name', (event) => {
    // 查找发送请求的视图对应的应用名称
    for (const [name, view] of windowManager.views.entries()) {
      if (view.webContents.id === event.sender.id) {
        return name;
      }
    }
    return null;
  });
}

// 初始化应用
function initializeApp() {
  appManager = new AppManager();
  windowManager = new WindowManager();
  appSwitcher = new AppSwitcher(appManager, windowManager);
  shortcutManager = new ShortcutManager(appSwitcher, windowManager);

  // 设置IPC处理程序
  setupIpcHandlers();

  // 注册快捷键
  shortcutManager.registerShortcuts();

  // 创建主窗口
  windowManager.createMainWindow();

  // 创建应用程序菜单
  menuManager = new MenuManager(windowManager);
}

// 应用准备就绪
app.whenReady().then(() => {
  electronLog.info('应用已准备就绪');
  initializeApp();
});

// 所有窗口关闭时
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

// 应用激活时（macOS）
app.on('activate', () => {
  if (!windowManager.mainWindow) {
    windowManager.createMainWindow();
  }
});

// 应用退出前
app.on('will-quit', () => {
  // 注销所有快捷键
  if (shortcutManager) {
    shortcutManager.unregisterAllShortcuts();
  }
});

// 处理第二个实例启动
app.on('second-instance', () => {
  // 如果用户尝试运行第二个实例，我们应该聚焦到现有实例
  if (windowManager && windowManager.mainWindow) {
    if (windowManager.mainWindow.isMinimized()) {
      windowManager.mainWindow.restore();
    }
    windowManager.mainWindow.focus();
  }
});


