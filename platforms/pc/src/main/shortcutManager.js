// 快捷键管理模块
const { globalShortcut, app } = require('electron');
const electronLog = require('electron-log');

class ShortcutManager {
  constructor(appSwitcher, windowManager) {
    this.appSwitcher = appSwitcher;
    this.windowManager = windowManager;
    this.registeredShortcuts = new Set();
    this.isAltKeyDown = false;
    this.altKeyTimer = null;
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

      // 设置Alt键状态
      this.isAltKeyDown = true;

      // 清除之前的定时器
      if (this.altKeyTimer) {
        clearTimeout(this.altKeyTimer);
      }

      // 设置一个定时器，如果长时间没有收到Alt键释放事件，自动关闭切换器
      this.altKeyTimer = setTimeout(() => {
        if (this.isAltKeyDown) {
          this.isAltKeyDown = false;
          this.appSwitcher.finishAppSwitching();
        }
      }, 3000); // 3000毫秒后自动关闭
    });

    // 监听Alt键释放
    this.setupAltKeyReleaseDetection();
  }

  // 设置Alt键释放检测
  setupAltKeyReleaseDetection() {
    // 在应用准备就绪后设置事件监听
    if (app.isReady()) {
      this.setupAltKeyEvents();
    } else {
      app.once('ready', () => {
        this.setupAltKeyEvents();
      });
    }
  }

  // 设置Alt键事件
  setupAltKeyEvents() {
    // 监听窗口失去焦点事件，可能是Alt键释放导致的
    if (this.windowManager.mainWindow) {
      this.windowManager.mainWindow.on('blur', () => {
        if (this.isAltKeyDown) {
          this.isAltKeyDown = false;
          this.appSwitcher.finishAppSwitching();
        }
      });
    }

    // 注册一个全局快捷键监听Alt键释放
    // 由于Electron不直接支持监听键盘释放事件，我们使用一个技巧：
    // 注册Alt+任意键的快捷键，当这些快捷键不再触发时，可能是Alt键被释放了
    const altCombinations = ['Alt+A', 'Alt+S', 'Alt+D', 'Alt+F'];

    for (const combo of altCombinations) {
      this.registerShortcut(combo, () => {
        // 当任何Alt组合键被按下时，重置定时器
        if (this.altKeyTimer) {
          clearTimeout(this.altKeyTimer);
        }

        this.altKeyTimer = setTimeout(() => {
          // 如果一段时间内没有Alt组合键被按下，认为Alt键已释放
          if (this.isAltKeyDown) {
            this.isAltKeyDown = false;
            this.appSwitcher.finishAppSwitching();
          }
        }, 50); // 50毫秒的延迟
      });
    }
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