// 快捷键管理模块
const { globalShortcut } = require('electron');
const electronLog = require('electron-log');

class ShortcutManager {
  constructor(appSwitcher, windowManager) {
    this.appSwitcher = appSwitcher;
    this.windowManager = windowManager;
    this.registeredShortcuts = new Set();
  }

  // 注册所有快捷键
  registerShortcuts() {
    this.registerShortcut('Alt+Q', () => {
      electronLog.info('Alt+Q pressed - Opening app selector');
      this.appSwitcher.showAppSelector();
    });

    this.registerShortcut('Alt+W', () => {
      electronLog.info('Alt+W pressed - Closing active view');
      this.windowManager.closeActiveView();
    });

    this.registerShortcut('Alt+Z', () => {
      electronLog.info('Alt+Z pressed - Minimizing current view');
      // 最小化当前活跃视图
      // 如果还有未最小化的窗口，切换到下一个未最小化的窗口
      // 如果所有窗口都最小化了，显示主窗口
      this.windowManager.minimizeActiveView();
    });

    this.registerShortcut('Alt+R', () => {
      electronLog.info('Alt+R pressed - Refreshing current view');
      this.windowManager.refreshActiveView();
    });

    this.registerShortcut('Alt+D', () => {
      electronLog.info('Alt+D pressed - Toggling Developer Tools');
      const activeView = this.windowManager.activeView;
      if (activeView) {
        activeView.webContents.toggleDevTools();
      } else if (this.windowManager.mainWindow) {
        this.windowManager.mainWindow.webContents.toggleDevTools();
      }
    });

    this.registerShortcut('Alt+T', () => {
      electronLog.info('Alt+T pressed - Switching between open apps');
      this.appSwitcher.switchToNextApp();
    });
  }

  // 注册单个快捷键
  registerShortcut(accelerator, callback) {
    try {
      const success = globalShortcut.register(accelerator, callback);
      if (success) {
        this.registeredShortcuts.add(accelerator);
        electronLog.info(`Registered shortcut: ${accelerator}`);
      } else {
        electronLog.error(`Failed to register shortcut: ${accelerator}`);
      }
    } catch (error) {
      electronLog.error(`Error registering shortcut ${accelerator}:`, error);
    }
  }

  // 注销所有快捷键
  unregisterAllShortcuts() {
    for (const shortcut of this.registeredShortcuts) {
      try {
        globalShortcut.unregister(shortcut);
        electronLog.info(`Unregistered shortcut: ${shortcut}`);
      } catch (error) {
        electronLog.error(`Error unregistering shortcut ${shortcut}:`, error);
      }
    }
    this.registeredShortcuts.clear();
  }
}

module.exports = ShortcutManager;