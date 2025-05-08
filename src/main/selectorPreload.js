// 应用选择器预加载脚本
const { contextBridge, ipcRenderer } = require('electron');

// 暴露给渲染进程的API
contextBridge.exposeInMainWorld('electronAPI', {
  // 获取所有应用
  getApps: () => ipcRenderer.invoke('get-apps'),
  
  // 打开应用
  openApp: (appName) => ipcRenderer.invoke('open-app', appName),
  
  // 添加应用
  addApp: (app) => ipcRenderer.invoke('add-app', app),
  
  // 关闭选择器
  closeSelector: () => ipcRenderer.invoke('close-selector'),
  
  // 监听应用列表更新
  onAppsList: (callback) => {
    ipcRenderer.on('apps-list', (event, apps) => {
      callback(apps);
    });
  }
});
