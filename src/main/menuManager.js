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

  createMenu() {
    const template = [
      {
        label: 'View',
        submenu: [
          {
            label: 'Reload',
            click: () => {
              const view = this.windowManager.activeView;
              if (view) {
                view.webContents.reload();
              }
            }
          },
          {
            label: 'Force Reload',
            click: () => {
              const view = this.windowManager.activeView;
              if (view) {
                view.webContents.reloadIgnoringCache();
              }
            }
          },
          {
            label: 'Toggle Developer Tools',
            click: () => {
              const view = this.windowManager.activeView;
              if (view) {
                view.webContents.toggleDevTools();
              }
            }
          }
        ]
      },
      {
        label: 'Window',
        submenu: [
          { role: 'minimize' },
          { type: 'separator' },
          { role: 'close' }
        ]
      },
      {
        label: 'Help',
        submenu: [
          {
            label: 'Keyboard Shortcuts',
            accelerator: process.platform === 'darwin' ? 'Cmd+/' : 'Ctrl+/',
            click: () => {
              this.showShortcutHelp();
            }
          },
          { type: 'separator' },
          {
            role: 'about',
            label: 'About Shell++'
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
