// 应用管理模块
const fs = require('fs');
const path = require('path');
const { app } = require('electron');

class AppManager {
  constructor() {
    this.apps = [];
    this.config = {}; // 完整的配置对象
    // 配置文件路径：在用户数据目录下
    this.configPath = path.join(app.getPath('userData'), 'config.json');
    this.loadApps();
  }

  // 加载应用列表 - 首先尝试从配置文件读取，否则使用默认列表
  loadApps() {
    try {
      // 尝试从配置文件读取
      if (fs.existsSync(this.configPath)) {
        const configData = fs.readFileSync(this.configPath, 'utf8');
        this.config = JSON.parse(configData);
        
        // 从配置对象中提取应用列表
        if (this.config.apps && Array.isArray(this.config.apps)) {
          this.apps = this.config.apps;
          console.log('从配置文件加载应用列表:', this.configPath);
          return;
        }
      }
    } catch (error) {
      console.error('读取配置文件失败:', error);
    }

    // 如果配置文件不存在或读取失败，使用默认的应用列表
    console.log('使用默认应用列表并保存到配置文件');
    const defaultApps = [
      { name: "chatgpt", url: "https://chatgpt.com/" },
      { name: "perplexity", url: "https://www.perplexity.ai/" },
      { name: "claude", url: "https://claude.ai" },
      { name: "grok", url: "https://grok.com" },
      { name: "yuewen", url: "https://yuewen.cn" },
      { name: "kimi", url: "https://kimi.moonshot.cn/" },
      { name: "tiangong", url: "https://www.tiangong.cn/" },
      { name: "doubao", url: "https://www.doubao.com/chat/" },
      { name: "mistral", url: "https://chat.mistral.ai/chat" },
      { name: "yuanbao", url: "https://yuanbao.tencent.com" },
      { name: "tongyi", url: "https://tongyi.aliyun.com" },
      { name: "gemini", url: "https://gemini.google.com" },
      { name: "duckai", url: "https://duckduckgo.com/?q=DuckDuckGo+AI+Chat&ia=chat&duckai=1" },
      { name: "chatglm", url: "https://chatglm.cn" },
      { name: "hailuoai", url: "https://hailuoai.com/" },
      { name: "meta", url: "https://www.meta.ai/" },
      { name: "poe", url: "https://www.poe.com/" },
      { name: "deepseek", url: "https://chat.deepseek.com/" },
      { name: "yiyan", url: "https://yiyan.baidu.com/" },
      { name: "copilot", url: "https://copilot.microsoft.com/" },
      { name: "monica", url: "https://monica.im/" },
      { name: "github-copilot", url: "https://github.com/copilot" },
      { name: "z", url: "https://z.ai" },
      { name: "wps", url: "https://copilot.wps.cn/" },
      { name: "huggingface", url: "https://huggingface.co/chat/" },
      { name: "openrouter", url: "https://openrouter.ai/" },
      { name: "youware", url: "https://www.youware.com/" },
    ];

    // 初始化配置对象和应用列表
    this.apps = defaultApps;
    this.config = {
      apps: defaultApps,
      // 这里可以添加其他配置项
      version: "1.0.0"
    };

    // 保存默认配置到文件
    this.saveApps();
  }

  // 保存配置到配置文件
  saveApps() {
    try {
      // 确保用户数据目录存在
      const userDataDir = app.getPath('userData');
      if (!fs.existsSync(userDataDir)) {
        fs.mkdirSync(userDataDir, { recursive: true });
      }

      // 更新配置对象中的应用列表
      this.config.apps = this.apps;

      // 保存完整的配置对象到文件
      fs.writeFileSync(this.configPath, JSON.stringify(this.config, null, 2), 'utf8');
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