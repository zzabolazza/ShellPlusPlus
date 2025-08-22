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
      { name: "ChatGPT", url: "https://chatgpt.com/" },
      { name: "Perplexity", url: "https://www.perplexity.ai/" },
      { name: "Claude", url: "https://claude.ai" },
      { name: "Grok", url: "https://grok.com" },
      { name: "跃问", url: "https://yuewen.cn" },
      { name: "Kimi", url: "https://kimi.moonshot.cn/" },
      { name: "天工", url: "https://www.tiangong.cn/" },
      { name: "豆包", url: "https://www.doubao.com/chat/" },
      { name: "Mistral", url: "https://chat.mistral.ai/chat" },
      { name: "元宝", url: "https://yuanbao.tencent.com" },
      { name: "通义千问", url: "https://tongyi.aliyun.com" },
      { name: "Gemini", url: "https://gemini.google.com" },
      { name: "DuckAI", url: "https://duckduckgo.com/?q=DuckDuckGo+AI+Chat&ia=chat&duckai=1" },
      { name: "ChatGLM", url: "https://chatglm.cn" },
      { name: "海螺AI", url: "https://hailuoai.com/" },
      { name: "Meta AI", url: "https://www.meta.ai/" },
      { name: "Poe", url: "https://www.poe.com/" },
      { name: "Deepseek", url: "https://chat.deepseek.com/" },
      { name: "文心一言", url: "https://yiyan.baidu.com/" },
      { name: "Microsoft Copilot", url: "https://copilot.microsoft.com/" },
      { name: "Monica", url: "https://monica.im/" },
      { name: "GitHub Copilot", url: "https://github.com/copilot" },
      { name: "Z.ai", url: "https://z.ai" },
      { name: "WPS AI", url: "https://copilot.wps.cn/" },
      { name: "Hugging Face", url: "https://huggingface.co/chat/" },
      { name: "OpenRouter", url: "https://openrouter.ai/" },
      { name: "YouWare", url: "https://www.youware.com/" },
      { name: "Replit", url: "https://replit.com/" },
      { name: "You", url: "https://you.com/?chatMode=default" },
      { name: "纳米搜索", url: "https://www.n.cn/" },
      { name: "讯飞星火", url: "https://xinghuo.xfyun.cn/" },
      { name: "秘塔AI", url: "https://metaso.cn/" },
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