const { contextBridge, ipcRenderer } = require('electron');

// 暴露安全的API给渲染进程
contextBridge.exposeInMainWorld('electronAPI', {
  // 窗口控制
  minimizeWindow: () => ipcRenderer.send('minimize-window'),
  toggleMaximizeWindow: () => ipcRenderer.send('toggle-maximize-window'),
  closeWindow: () => ipcRenderer.send('close-window'),

  // 应用管理
  getApps: () => ipcRenderer.invoke('get-apps'),
  getOpenApps: () => ipcRenderer.invoke('get-open-apps'),
  switchToApp: (appName) => ipcRenderer.invoke('switch-to-app', appName),
  closeApp: (appName) => ipcRenderer.invoke('close-app', appName),
  showAppSelector: () => ipcRenderer.invoke('show-app-selector'),
  addApp: (app) => ipcRenderer.invoke('add-app', app),
  removeApp: (appName) => ipcRenderer.invoke('remove-app', appName),
  updateApp: (appName, newApp) => ipcRenderer.invoke('update-app', appName, newApp),

  // 事件监听
  onActiveAppChanged: (callback) => {
    ipcRenderer.on('active-app-changed', (_, appName) => callback(appName));
  },
  onAppsChanged: (callback) => {
    ipcRenderer.on('apps-changed', () => callback());
  },
  onToggleShortcutHelp: (callback) => {
    ipcRenderer.on('toggle-shortcut-help', (_, show) => callback(show));
  },
  toggleShortcutHelp: (show) => {
    ipcRenderer.invoke('toggle-shortcut-help', show);
  },

  // 配置管理
  getConfig: (key) => ipcRenderer.invoke('get-config', key),
  setConfig: (key, value) => ipcRenderer.invoke('set-config', key, value),
  updateConfig: (config) => ipcRenderer.invoke('update-config', config),
  resetConfig: () => ipcRenderer.invoke('reset-config'),
  testProxy: (proxyConfig) => ipcRenderer.invoke('test-proxy', proxyConfig),
  
  // 配置面板事件
  onToggleConfigPanel: (callback) => {
    ipcRenderer.on('toggle-config-panel', (_, show) => callback(show));
  }
});

// 添加调试信息
console.log('Preload script executed');
