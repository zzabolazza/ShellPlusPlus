const { contextBridge, ipcRenderer } = require('electron');

// 暴露安全的API给渲染进程
contextBridge.exposeInMainWorld('electronAPI', {
  // 窗口控制
  minimizeWindow: () => ipcRenderer.send('minimize-window'),
  toggleMaximizeWindow: () => ipcRenderer.send('toggle-maximize-window'),
  closeWindow: () => ipcRenderer.send('close-window'),

  // 应用管理
  getOpenApps: () => ipcRenderer.invoke('get-open-apps'),
  switchToApp: (appName) => ipcRenderer.invoke('switch-to-app', appName),
  closeApp: (appName) => ipcRenderer.invoke('close-app', appName),
  showAppSelector: () => ipcRenderer.invoke('show-app-selector'),

  // 事件监听
  onActiveAppChanged: (callback) => {
    ipcRenderer.on('active-app-changed', (_, appName) => callback(appName));
  },
  onAppsChanged: (callback) => {
    ipcRenderer.on('apps-changed', () => callback());
  }
});

// 添加调试信息
console.log('Preload script executed');
