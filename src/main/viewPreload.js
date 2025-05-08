const { contextBridge, ipcRenderer } = require('electron');

// 暴露安全的API给视图内容
contextBridge.exposeInMainWorld('shellPlusPlus', {
  // 可以添加一些视图特定的API
  getAppName: () => ipcRenderer.invoke('get-current-app-name')
});