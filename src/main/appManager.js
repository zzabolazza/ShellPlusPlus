// 应用管理模块
class AppManager {
  constructor() {
    this.apps = [];
    this.loadApps();
  }

  // 加载应用列表 - 使用硬编码的应用列表
  loadApps() {
    // 硬编码的应用列表
    this.apps = [
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
    ];
  }

  // 保存应用列表 - 不再需要保存到文件
  saveApps() {
    // 应用列表已硬编码，不需要保存
    return;
  }

  // 获取应用列表
  getApps() {
    return this.apps;
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