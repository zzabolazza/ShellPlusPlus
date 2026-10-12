// 应用管理模块
const fs = require('fs');
const path = require('path');
const https = require('https');
const { URL } = require('url');
const { app } = require('electron');

const DEFAULT_SOURCE_URL = 'https://raw.githubusercontent.com/zzabolazza/ShellPlusPlus/main/apps.json';
const BUNDLED_APPS = [
  { name: 'ChatGPT', url: 'https://chatgpt.com/' },
  { name: 'Perplexity', url: 'https://www.perplexity.ai/' }
];

class AppManager {
  constructor() {
    this.apps = [];
    this.config = {};
    this.hasSavedApps = false;
    this.configPath = path.join(app.getPath('userData'), 'config.json');
    this.loadApps();
  }

  loadApps() {
    try {
      if (fs.existsSync(this.configPath)) {
        const configData = fs.readFileSync(this.configPath, 'utf8');
        this.config = JSON.parse(configData);
        if (Array.isArray(this.config.apps) && this.config.apps.length > 0) {
          this.apps = this.normalizeApps(this.config.apps);
          this.hasSavedApps = true;
          console.log('从配置文件加载应用列表:', this.configPath);
          return;
        }
      }
    } catch (error) {
      console.error('读取配置文件失败:', error);
    }

    this.apps = BUNDLED_APPS.map(item => ({ ...item }));
    this.hasSavedApps = false;
  }

  getSourceUrl() {
    try {
      if (fs.existsSync(this.configPath)) {
        const disk = JSON.parse(fs.readFileSync(this.configPath, 'utf8'));
        if (typeof disk.appListSource === 'string' && disk.appListSource.trim()) {
          return disk.appListSource.trim();
        }
      }
    } catch (error) {
      console.error('读取应用列表源失败:', error);
    }
    return DEFAULT_SOURCE_URL;
  }

  async syncFromSource() {
    try {
      const apps = this.normalizeApps(await this.downloadApps(this.getSourceUrl()));
      if (apps.length === 0) return false;
      this.apps = apps;
      this.hasSavedApps = true;
      this.saveApps();
      return true;
    } catch (error) {
      console.error('同步应用列表失败:', error);
      if (!this.hasSavedApps) {
        this.apps = BUNDLED_APPS.map(item => ({ ...item }));
      }
      return false;
    }
  }

  downloadApps(sourceUrl) {
    const url = new URL(sourceUrl);
    url.searchParams.set('t', Date.now().toString());
    return new Promise((resolve, reject) => {
      const request = https.get(url, {
        headers: {
          'Cache-Control': 'no-cache',
          Accept: 'application/json'
        }
      }, (response) => {
        if (response.statusCode < 200 || response.statusCode >= 300) {
          response.resume();
          reject(new Error(`应用列表请求失败: ${response.statusCode}`));
          return;
        }
        let body = '';
        response.setEncoding('utf8');
        response.on('data', (chunk) => {
          body += chunk;
        });
        response.on('end', () => {
          try {
            resolve(JSON.parse(body));
          } catch (error) {
            reject(error);
          }
        });
      });
      request.setTimeout(15000, () => {
        request.destroy(new Error('应用列表请求超时'));
      });
      request.on('error', reject);
    });
  }

  normalizeApps(list) {
    if (!Array.isArray(list)) return [];
    return list.filter(item => item && item.name && item.url).map(item => ({
      name: item.name,
      url: item.url,
      packageName: item.packageName
    }));
  }

  saveApps() {
    try {
      const userDataDir = app.getPath('userData');
      if (!fs.existsSync(userDataDir)) {
        fs.mkdirSync(userDataDir, { recursive: true });
      }

      let disk = {};
      if (fs.existsSync(this.configPath)) {
        disk = JSON.parse(fs.readFileSync(this.configPath, 'utf8'));
      }
      disk.apps = this.apps;
      fs.writeFileSync(this.configPath, JSON.stringify(disk, null, 2), 'utf8');
      this.config = disk;
      console.log('配置已保存到文件:', this.configPath);
    } catch (error) {
      console.error('保存配置文件失败:', error);
    }
  }

  // 获取应用列表
  getApps() {
    return this.apps;
  }

  // 获取完整配置
  getConfig() {
    return this.config;
  }

  // 更新配置项
  updateConfig(key, value) {
    this.config[key] = value;
    this.saveApps();
  }

  // 添加应用
  addApp(app) {
    this.apps.push(app);
    this.saveApps();
  }

  // 删除应用
  removeApp(name) {
    this.apps = this.apps.filter(app => app.name !== name);
    this.saveApps();
  }

  // 更新应用
  updateApp(name, newApp) {
    const index = this.apps.findIndex(app => app.name === name);
    if (index !== -1) {
      this.apps[index] = newApp;
      this.saveApps();
    }
  }
}

module.exports = AppManager;