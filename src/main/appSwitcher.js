// 应用切换器模块
const { BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const electronLog = require('electron-log');

class AppSwitcher {
  constructor(appManager, windowManager) {
    this.appManager = appManager;
    this.windowManager = windowManager;
    this.selectorWindow = null;
    this.switcherWindow = null;
    this.isAltTHeld = false;
    this.selectedAppIndex = 0;
    this.setupIpcHandlers();
  }

  // 设置IPC处理程序 - 只注册应用选择器相关的处理程序
  setupIpcHandlers() {

    // 从选择器打开应用
    ipcMain.handle('open-app', (event, appName) => {
      const app = this.appManager.getApps().find(a => a.name === appName);
      if (app) {
        this.windowManager.switchToApp(app);
        this.closeAppSelector();
        return { success: true };
      }
      return { success: false, error: '找不到应用' };
    });

    // 添加新应用
    ipcMain.handle('add-app', (event, { name, url }) => {
      const success = this.appManager.addApp(name, url);
      if (success) {
        this.closeAppSelector();
        return { success: true };
      }
      return { success: false, error: '应用名称已存在' };
    });

    // 关闭选择器
    ipcMain.handle('close-selector', () => {
      this.closeAppSelector();
      return true;
    });

    // 获取当前打开的应用（带详细信息）
    ipcMain.handle('get-open-apps-with-details', () => {
      return this.windowManager.getOpenApps().map(appName => {
        const app = this.appManager.getApps().find(a => a.name === appName);
        return app || { name: appName, url: '' };
      });
    });

    // 切换到指定应用
    ipcMain.handle('switch-to-app-by-index', (event, index) => {
      this.selectedAppIndex = index;
      this.updateSelectedApp();
      return true;
    });

    // 关闭应用切换器
    ipcMain.handle('close-app-switcher', () => {
      this.closeAppSwitcher();
      return true;
    });

    // 确认当前选择
    ipcMain.handle('confirm-app-selection', () => {
      this.finishAppSwitching();
      return true;
    });

    // 切换到下一个应用
    ipcMain.handle('switch-to-next-app', () => {
      const openApps = this.windowManager.getOpenApps();
      if (openApps.length > 1) {
        this.selectedAppIndex = (this.selectedAppIndex + 1) % openApps.length;
        this.updateSelectedApp();
      }
      return true;
    });

    // 切换到上一个应用
    ipcMain.handle('switch-to-prev-app', () => {
      const openApps = this.windowManager.getOpenApps();
      if (openApps.length > 1) {
        this.selectedAppIndex = (this.selectedAppIndex - 1 + openApps.length) % openApps.length;
        this.updateSelectedApp();
      }
      return true;
    });

    // 关闭指定索引的应用
    ipcMain.handle('close-app-by-index', (event, index) => {
      const openApps = this.windowManager.getOpenApps();
      if (index >= 0 && index < openApps.length) {
        const appName = openApps[index];
        this.windowManager.closeApp(appName);

        // 更新应用列表和选中索引
        const newOpenApps = this.windowManager.getOpenApps();
        if (newOpenApps.length === 0) {
          // 如果没有应用了，关闭切换器
          this.closeAppSwitcher();
        } else {
          // 调整选中索引
          if (this.selectedAppIndex >= newOpenApps.length) {
            this.selectedAppIndex = newOpenApps.length - 1;
          }
          // 重新发送应用列表
          const apps = newOpenApps.map(appName => {
            const app = this.appManager.getApps().find(a => a.name === appName);
            return app || { name: appName, url: '' };
          });
          this.switcherWindow.webContents.send('open-apps-list', {
            apps,
            selectedIndex: this.selectedAppIndex
          });
        }
        return true;
      }
      return false;
    });
  }

  // 显示应用选择器 - 直接使用硬编码的应用列表
  showAppSelector() {
    if (this.selectorWindow && !this.selectorWindow.isDestroyed()) {
      this.selectorWindow.focus();
      return;
    }

    // 确保主窗口存在
    if (!this.windowManager.mainWindow || this.windowManager.mainWindow.isDestroyed()) {
      this.windowManager.createMainWindow();
    }

    this.selectorWindow = new BrowserWindow({
      width: 600,
      height: 500,
      parent: this.windowManager.mainWindow,
      modal: true,
      frame: false,
      resizable: false,
      webPreferences: {
        preload: path.join(__dirname, 'selectorPreload.js'),
        nodeIntegration: false,
        contextIsolation: true,
        sandbox: true
      }
    });

    // 增加最大监听器数量，避免内存泄漏警告
    this.selectorWindow.setMaxListeners(20);

    // 加载应用选择器界面
    this.selectorWindow.loadFile(path.join(__dirname, '../renderer/appSelector.html'));

    // 窗口失去焦点时关闭
    const blurHandler = () => {
      this.closeAppSelector();
    };
    this.selectorWindow.on('blur', blurHandler);

    // 窗口关闭时清除引用和事件监听器
    const closedHandler = () => {
      this.selectorWindow.removeListener('blur', blurHandler);
      this.selectorWindow.removeListener('closed', closedHandler);
      this.selectorWindow = null;
    };
    this.selectorWindow.on('closed', closedHandler);

    // 当应用选择器加载完成后，发送硬编码的应用列表
    const loadHandler = () => {
      // 获取应用列表
      const apps = this.appManager.getApps();
      // 发送到渲染进程
      this.selectorWindow.webContents.send('apps-list', apps);
      // 加载完成后移除事件监听器
      this.selectorWindow.webContents.removeListener('did-finish-load', loadHandler);
    };
    this.selectorWindow.webContents.on('did-finish-load', loadHandler);

    electronLog.info('应用选择器已显示');
  }

  // 关闭应用选择器
  closeAppSelector() {
    if (this.selectorWindow && !this.selectorWindow.isDestroyed()) {
      // 关闭窗口，事件监听器会在closed事件中被清理
      this.selectorWindow.close();
    }
  }

  // 显示应用切换器 (Alt+Tab 风格)
  showAppSwitcher() {
    // 如果已经显示，则不重新创建窗口
    if (this.switcherWindow && !this.switcherWindow.isDestroyed()) {
      return;
    }

    // 获取当前打开的应用
    const openApps = this.windowManager.getOpenApps();
    if (openApps.length <= 1) {
      // 如果只有一个或没有应用，则不显示切换器
      return;
    }

    // 确保主窗口存在
    if (!this.windowManager.mainWindow || this.windowManager.mainWindow.isDestroyed()) {
      this.windowManager.createMainWindow();
    }

    // 获取主窗口的位置和大小
    const mainBounds = this.windowManager.mainWindow.getBounds();

    // 创建应用切换器窗口
    this.switcherWindow = new BrowserWindow({
      width: 600,
      height: 200,
      x: Math.round(mainBounds.x + (mainBounds.width - 600) / 2),
      y: Math.round(mainBounds.y + (mainBounds.height - 200) / 2),
      parent: this.windowManager.mainWindow,
      modal: true,
      frame: false,
      transparent: false,
      resizable: false,
      skipTaskbar: true,
      alwaysOnTop: true,
      webPreferences: {
        preload: path.join(__dirname, 'switcherPreload.js'),
        nodeIntegration: false,
        contextIsolation: true,
        sandbox: true
      }
    });

    // 增加最大监听器数量，避免内存泄漏警告
    this.switcherWindow.setMaxListeners(20);

    // 加载应用切换器界面
    this.switcherWindow.loadFile(path.join(__dirname, '../renderer/appSwitcher.html'));

    // 窗口失去焦点时关闭
    const blurHandler = () => {
      this.closeAppSwitcher();
    };
    this.switcherWindow.on('blur', blurHandler);

    // 窗口关闭时清除引用和事件监听器
    const closedHandler = () => {
      this.switcherWindow.removeListener('blur', blurHandler);
      this.switcherWindow.removeListener('closed', closedHandler);
      this.switcherWindow = null;
      this.isAltTHeld = false;
    };
    this.switcherWindow.on('closed', closedHandler);

    // 当应用切换器加载完成后，发送当前打开的应用列表
    const loadHandler = () => {
      // 查找当前活跃应用的索引
      const activeAppName = this.getActiveAppName();
      const openApps = this.windowManager.getOpenApps();
      this.selectedAppIndex = openApps.indexOf(activeAppName);
      if (this.selectedAppIndex === -1) {
        this.selectedAppIndex = 0;
      }

      // 获取应用详细信息
      const apps = openApps.map(appName => {
        const app = this.appManager.getApps().find(a => a.name === appName);
        return app || { name: appName, url: '' };
      });

      // 发送到渲染进程
      this.switcherWindow.webContents.send('open-apps-list', {
        apps,
        selectedIndex: this.selectedAppIndex
      });

      // 加载完成后移除事件监听器
      this.switcherWindow.webContents.removeListener('did-finish-load', loadHandler);
    };
    this.switcherWindow.webContents.on('did-finish-load', loadHandler);

    this.isAltTHeld = true;
    electronLog.info('应用切换器已显示');
  }

  // 切换到下一个应用
  switchToNextApp() {
    if (!this.isAltTHeld) {
      // 首次按下 Alt+T，显示应用切换器
      this.showAppSwitcher();
    } else if (this.switcherWindow && !this.switcherWindow.isDestroyed()) {
      // 已经显示切换器，切换到下一个应用
      const openApps = this.windowManager.getOpenApps();
      if (openApps.length > 1) {
        // 获取当前选中应用的状态
        const currentAppName = openApps[this.selectedAppIndex];
        const currentStatus = this.windowManager.getAppStatus(currentAppName);

        // 如果当前应用是最小化的，恢复它
        if (currentStatus.isMinimized) {
          // 从最小化集合中移除
          this.windowManager.restoreMinimizedView();
          return;
        }

        // 否则切换到下一个应用
        this.selectedAppIndex = (this.selectedAppIndex + 1) % openApps.length;
        this.updateSelectedApp();
      }
    }
  }

  // 更新选中的应用
  updateSelectedApp() {
    if (this.switcherWindow && !this.switcherWindow.isDestroyed()) {
      this.switcherWindow.webContents.send('update-selected-app', this.selectedAppIndex);
    }
  }

  // 获取当前活跃应用名称
  getActiveAppName() {
    if (!this.windowManager.activeView) return null;

    for (const [name, view] of this.windowManager.views.entries()) {
      if (view === this.windowManager.activeView) {
        return name;
      }
    }

    return null;
  }

  // 完成应用切换 (当Alt键释放时)
  finishAppSwitching() {
    if (!this.isAltTHeld || !this.switcherWindow) {
      return;
    }

    const openApps = this.windowManager.getOpenApps();
    if (openApps.length > this.selectedAppIndex) {
      const appName = openApps[this.selectedAppIndex];
      const app = this.appManager.getApps().find(a => a.name === appName);
      if (app) {
        this.windowManager.activateView(appName, this.windowManager.views.get(appName));
      }
    }

    this.closeAppSwitcher();
  }

  // 关闭应用切换器
  closeAppSwitcher() {
    if (this.switcherWindow && !this.switcherWindow.isDestroyed()) {
      // 关闭窗口，事件监听器会在closed事件中被清理
      this.switcherWindow.close();
    }
    this.isAltTHeld = false;
  }
}

module.exports = AppSwitcher;
