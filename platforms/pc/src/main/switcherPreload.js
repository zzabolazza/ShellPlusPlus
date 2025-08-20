// 应用切换器预加载脚本
const { contextBridge, ipcRenderer } = require('electron');

// 暴露给渲染进程的API
contextBridge.exposeInMainWorld('electronAPI', {
  // 获取当前打开的应用（带详细信息）
  getOpenApps: () => ipcRenderer.invoke('get-open-apps-with-details'),

  // 切换到指定索引的应用
  switchToAppByIndex: (index) => ipcRenderer.invoke('switch-to-app-by-index', index),

  // 关闭应用切换器
  closeAppSwitcher: () => ipcRenderer.invoke('close-app-switcher'),

  // 确认当前选择
  confirmSelection: () => ipcRenderer.invoke('confirm-app-selection'),

  // 切换到下一个应用
  switchToNextApp: () => ipcRenderer.invoke('switch-to-next-app'),

  // 切换到上一个应用
  switchToPrevApp: () => ipcRenderer.invoke('switch-to-prev-app'),

  // 关闭指定索引的应用
  closeAppByIndex: (index) => ipcRenderer.invoke('close-app-by-index', index),

  // 监听打开的应用列表更新
  onOpenAppsList: (callback) => {
    ipcRenderer.on('open-apps-list', (event, data) => {
      callback(data);
    });
  },

  // 监听选中应用更新
  onSelectedAppUpdate: (callback) => {
    ipcRenderer.on('update-selected-app', (event, index) => {
      callback(index);
    });
  }
});
