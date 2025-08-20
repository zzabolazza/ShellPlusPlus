const { app, ipcMain, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');
const electronLog = require('electron-log');

class ConfigManager {
  constructor() {
    this.configPath = path.join(app.getPath('userData'), 'config.json');
    this.config = this.loadConfig();
    this.setupIpcHandlers();
  }

  // 默认配置
  getDefaultConfig() {
    return {
      proxy: {
        enabled: false,
        type: 'http', // http, https, socks4, socks5
        host: '',
        port: '',
        username: '',
        password: '',
        bypassList: 'localhost,127.0.0.1,*.local'
      }
    };
  }

  // 加载配置
  loadConfig() {
    try {
      if (fs.existsSync(this.configPath)) {
        const configData = fs.readFileSync(this.configPath, 'utf-8');
        const config = JSON.parse(configData);
        // 合并默认配置，确保所有配置项都存在
        return { ...this.getDefaultConfig(), ...config };
      }
    } catch (error) {
      electronLog.error('加载配置文件失败:', error);
    }
    return this.getDefaultConfig();
  }

  // 保存配置
  saveConfig() {
    try {
      const configDir = path.dirname(this.configPath);
      if (!fs.existsSync(configDir)) {
        fs.mkdirSync(configDir, { recursive: true });
      }
      fs.writeFileSync(this.configPath, JSON.stringify(this.config, null, 2));
      electronLog.info('配置已保存');
      return true;
    } catch (error) {
      electronLog.error('保存配置文件失败:', error);
      return false;
    }
  }

  // 获取配置
  getConfig(key = null) {
    if (key) {
      return this.getNestedValue(this.config, key);
    }
    return { ...this.config };
  }

  // 设置配置
  setConfig(key, value) {
    this.setNestedValue(this.config, key, value);
    this.saveConfig();
    this.applyConfig();
  }

  // 批量更新配置
  updateConfig(newConfig) {
    this.config = { ...this.config, ...newConfig };
    this.saveConfig();
    this.applyConfig();
  }

  // 获取嵌套值
  getNestedValue(obj, key) {
    return key.split('.').reduce((current, prop) => current && current[prop], obj);
  }

  // 设置嵌套值
  setNestedValue(obj, key, value) {
    const keys = key.split('.');
    const lastKey = keys.pop();
    const target = keys.reduce((current, prop) => {
      if (!current[prop] || typeof current[prop] !== 'object') {
        current[prop] = {};
      }
      return current[prop];
    }, obj);
    target[lastKey] = value;
  }

  // 应用配置
  applyConfig() {
    this.applyProxyConfig();
    electronLog.info('配置已应用');
  }

  // 应用代理配置
  applyProxyConfig() {
    const proxyConfig = this.config.proxy;
    
    if (proxyConfig.enabled && proxyConfig.host && proxyConfig.port) {
      let proxyUrl = '';
      
      if (proxyConfig.username && proxyConfig.password) {
        proxyUrl = `${proxyConfig.type}://${proxyConfig.username}:${proxyConfig.password}@${proxyConfig.host}:${proxyConfig.port}`;
      } else {
        proxyUrl = `${proxyConfig.type}://${proxyConfig.host}:${proxyConfig.port}`;
      }

      const proxyRules = {
        proxyRules: proxyUrl,
        proxyBypassRules: proxyConfig.bypassList || 'localhost,127.0.0.1,*.local'
      };

      // 为所有会话设置代理
      const { session } = require('electron');
      session.defaultSession.setProxy(proxyRules)
        .then(() => {
          electronLog.info('代理配置已应用:', proxyUrl);
        })
        .catch(error => {
          electronLog.error('应用代理配置失败:', error);
        });
    } else {
      // 清除代理设置
      const { session } = require('electron');
      session.defaultSession.setProxy({})
        .then(() => {
          electronLog.info('代理配置已清除');
        })
        .catch(error => {
          electronLog.error('清除代理配置失败:', error);
        });
    }
  }

  // 配置界面已集成到主窗口，无需独立窗口

  // 设置IPC处理程序
  setupIpcHandlers() {
    // 获取配置
    ipcMain.handle('get-config', (_, key) => {
      return this.getConfig(key);
    });

    // 设置配置
    ipcMain.handle('set-config', (_, key, value) => {
      this.setConfig(key, value);
      return true;
    });

    // 批量更新配置
    ipcMain.handle('update-config', (_, newConfig) => {
      this.updateConfig(newConfig);
      return true;
    });

    // 重置配置
    ipcMain.handle('reset-config', () => {
      this.config = this.getDefaultConfig();
      this.saveConfig();
      this.applyConfig();
      return true;
    });

    // 测试代理连接
    ipcMain.handle('test-proxy', async (_, proxyConfig) => {
      return await this.testProxyConnection(proxyConfig);
    });

    // 配置界面已集成到主窗口，移除独立窗口相关处理
  }

  // 测试代理连接
  async testProxyConnection(proxyConfig) {
    try {
      const { net } = require('electron');
      
      // 验证输入参数
      if (!proxyConfig.host || !proxyConfig.port) {
        return { success: false, error: '请填写完整的代理服务器地址和端口' };
      }

      // 构建代理URL
      let proxyUrl = '';
      if (proxyConfig.username && proxyConfig.password) {
        proxyUrl = `${proxyConfig.type}://${encodeURIComponent(proxyConfig.username)}:${encodeURIComponent(proxyConfig.password)}@${proxyConfig.host}:${proxyConfig.port}`;
      } else {
        proxyUrl = `${proxyConfig.type}://${proxyConfig.host}:${proxyConfig.port}`;
      }

      electronLog.info('测试代理连接:', proxyUrl.replace(/\/\/.*:.*@/, '//***:***@'));

      // 创建临时会话来测试代理
      const { session } = require('electron');
      const testSession = session.fromPartition(`test-proxy-${Date.now()}`);
      
      try {
        await testSession.setProxy({
          proxyRules: proxyUrl,
          proxyBypassRules: ''
        });
      } catch (proxyError) {
        electronLog.error('设置代理失败:', proxyError);
        return { success: false, error: '设置代理失败: ' + proxyError.message };
      }

      // 根据代理类型选择合适的测试URL
      let testUrls = [];
      
      if (proxyConfig.type === 'http') {
        // HTTP代理通常支持HTTPS CONNECT方法
        testUrls = [
          'http://httpbin.org/ip',
          'https://httpbin.org/ip',
          'http://www.baidu.com'
        ];
      } else if (proxyConfig.type === 'https') {
        // HTTPS代理
        testUrls = [
          'https://httpbin.org/ip',
          'https://www.google.com'
        ];
      } else if (proxyConfig.type === 'socks4' || proxyConfig.type === 'socks5') {
        // SOCKS代理支持所有协议
        testUrls = [
          'http://httpbin.org/ip',
          'https://httpbin.org/ip',
          'http://www.google.com',
          'https://www.baidu.com'
        ];
      } else {
        // 默认测试
        testUrls = [
          'http://httpbin.org/ip',
          'https://httpbin.org/ip'
        ];
      }

      for (const testUrl of testUrls) {
        try {
          const result = await this.testSingleUrl(testUrl, testSession);
          if (result.success) {
            return { success: true, message: `代理连接成功 (测试URL: ${testUrl})` };
          }
          electronLog.info(`测试URL ${testUrl} 失败:`, result.error);
        } catch (error) {
          electronLog.info(`测试URL ${testUrl} 异常:`, error.message);
          continue;
        }
      }

      return { success: false, error: '所有测试URL都无法连接，请检查代理设置和网络连接' };
    } catch (error) {
      electronLog.error('代理测试异常:', error);
      return { success: false, error: '测试异常: ' + error.message };
    }
  }

  // 测试单个URL
  async testSingleUrl(url, session) {
    const { net } = require('electron');
    
    return new Promise((resolve) => {
      const timeout = setTimeout(() => {
        resolve({ success: false, error: '连接超时 (15秒)' });
      }, 15000);

      try {
        const request = net.request({
          method: 'GET',
          url: url,
          session: session
        });

        request.on('response', (response) => {
          clearTimeout(timeout);
          
          let data = '';
          response.on('data', (chunk) => {
            data += chunk;
          });

          response.on('end', () => {
            if (response.statusCode >= 200 && response.statusCode < 400) {
              resolve({ success: true, message: `连接成功 (${response.statusCode})` });
            } else {
              resolve({ success: false, error: `HTTP ${response.statusCode}` });
            }
          });
        });

        request.on('error', (error) => {
          clearTimeout(timeout);
          let errorMsg = error.message;
          
          // 提供更友好的错误信息
          if (errorMsg.includes('ECONNREFUSED')) {
            errorMsg = '连接被拒绝，请检查代理服务器是否正在运行';
          } else if (errorMsg.includes('ENOTFOUND')) {
            errorMsg = '无法解析代理服务器地址';
          } else if (errorMsg.includes('ETIMEDOUT')) {
            errorMsg = '连接超时，请检查代理服务器地址和端口';
          } else if (errorMsg.includes('ECONNRESET')) {
            errorMsg = '连接被重置，可能是代理服务器配置问题';
          } else if (errorMsg.includes('invalid method') || errorMsg.includes('failed to read http request')) {
            errorMsg = '协议不匹配，建议尝试SOCKS5代理类型';
          } else if (errorMsg.includes('EPROTO') || errorMsg.includes('wrong version number')) {
            errorMsg = 'TLS协议错误，可能是代理类型选择错误';
          }
          
          resolve({ success: false, error: errorMsg });
        });

        request.end();
      } catch (error) {
        clearTimeout(timeout);
        resolve({ success: false, error: '请求创建失败: ' + error.message });
      }
    });
  }

  // 初始化时应用配置
  initialize() {
    this.applyConfig();
    electronLog.info('配置管理器已初始化');
  }
}

module.exports = ConfigManager;