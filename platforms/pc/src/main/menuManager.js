const { Menu, app, ipcMain } = require('electron');
const electronLog = require('electron-log');

class MenuManager {
  constructor(windowManager) {
    this.windowManager = windowManager;
    this.isHelpVisible = false;
    this.createMenu();

    // 注册 IPC 处理程序
    ipcMain.handle('toggle-shortcut-help', (_, show) => {
      this.isHelpVisible = show === undefined ? !this.isHelpVisible : show;
      this.windowManager.mainWindow.webContents.send('toggle-shortcut-help', this.isHelpVisible);
    });
  }

  // 显示快捷键帮助
  showShortcutHelp() {
    this.isHelpVisible = !this.isHelpVisible;
    this.windowManager.mainWindow.webContents.send('toggle-shortcut-help', this.isHelpVisible);
  }

  // 打开设置面板
  openSettings(tab = null) {
    // 发送事件到主窗口显示配置面板
    this.windowManager.mainWindow.webContents.send('toggle-config-panel', true);
  }

  createMenu() {
    const template = [
      {
        label: '设置',
        submenu: [
          {
            label: '偏好设置...',
            accelerator: process.platform === 'darwin' ? 'Cmd+,' : 'Ctrl+,',
            click: () => {
              this.openSettings();
            }
          }
        ]
      },
      {
        label: '帮助',
        submenu: [
          {
            label: '键盘快捷键',
            accelerator: process.platform === 'darwin' ? 'Cmd+/' : 'Ctrl+/',
            click: () => {
              this.showShortcutHelp();
            }
          }
        ]
      }
    ];

    const menu = Menu.buildFromTemplate(template);
    Menu.setApplicationMenu(menu);
    electronLog.info('应用程序菜单已创建');
  }
}

module.exports = MenuManager;
