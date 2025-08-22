'use client'

import { useState, useEffect } from 'react'
import AppItem from '@/components/AppItem'
import AddAppItem from '@/components/AddAppItem'

interface App {
  name: string
  url: string
  icon?: string
}

// 默认应用列表 - 参考 appManager.js 中的 AI 应用
const defaultApps: App[] = [
  { name: "ChatGPT", url: "https://chatgpt.com/", icon: "https://favicon.im/chatgpt.com" },
  { name: "Perplexity", url: "https://www.perplexity.ai/", icon: "https://favicon.im/www.perplexity.ai" },
  { name: "Claude", url: "https://claude.ai", icon: "https://favicon.im/claude.ai" },
  { name: "Grok", url: "https://grok.com", icon: "https://favicon.im/grok.com" },
  { name: "跃问", url: "https://yuewen.cn", icon: "https://favicon.im/yuewen.cn" },
  { name: "Kimi", url: "https://kimi.moonshot.cn/", icon: "https://favicon.im/kimi.moonshot.cn" },
  { name: "天工", url: "https://www.tiangong.cn/", icon: "https://favicon.im/www.tiangong.cn" },
  { name: "豆包", url: "https://www.doubao.com/chat/", icon: "https://favicon.im/www.doubao.com" },
  { name: "Mistral", url: "https://chat.mistral.ai/chat", icon: "https://favicon.im/chat.mistral.ai" },
  { name: "元宝", url: "https://yuanbao.tencent.com", icon: "https://favicon.im/yuanbao.tencent.com" },
  { name: "通义千问", url: "https://tongyi.aliyun.com", icon: "https://favicon.im/tongyi.aliyun.com" },
  { name: "Gemini", url: "https://gemini.google.com", icon: "https://favicon.im/gemini.google.com" },
  { name: "DuckAI", url: "https://duckduckgo.com/?q=DuckDuckGo+AI+Chat&ia=chat&duckai=1", icon: "https://favicon.im/duckduckgo.com" },
  { name: "ChatGLM", url: "https://chatglm.cn", icon: "https://favicon.im/chatglm.cn" },
  { name: "海螺AI", url: "https://hailuoai.com/", icon: "https://favicon.im/hailuoai.com" },
  { name: "Meta AI", url: "https://www.meta.ai/", icon: "https://favicon.im/www.meta.ai" },
  { name: "Poe", url: "https://www.poe.com/", icon: "https://favicon.im/www.poe.com" },
  { name: "Deepseek", url: "https://chat.deepseek.com/", icon: "https://favicon.im/chat.deepseek.com" },
  { name: "文心一言", url: "https://yiyan.baidu.com/", icon: "https://favicon.im/yiyan.baidu.com" },
  { name: "Microsoft Copilot", url: "https://copilot.microsoft.com/", icon: "https://favicon.im/copilot.microsoft.com" },
  { name: "Monica", url: "https://monica.im/", icon: "https://favicon.im/monica.im" },
  { name: "GitHub Copilot", url: "https://github.com/copilot", icon: "https://favicon.im/github.com" },
  { name: "Z.ai", url: "https://z.ai", icon: "https://favicon.im/z.ai" },
  { name: "WPS AI", url: "https://copilot.wps.cn/", icon: "https://favicon.im/copilot.wps.cn" },
  { name: "Hugging Face", url: "https://huggingface.co/chat/", icon: "https://favicon.im/huggingface.co" },
  { name: "OpenRouter", url: "https://openrouter.ai/", icon: "https://favicon.im/openrouter.ai" },
  { name: "YouWare", url: "https://www.youware.com/", icon: "https://favicon.im/www.youware.com" },
  { name: "Replit", url: "https://replit.com/", icon: "https://favicon.im/replit.com" },
  { name: "You", url: "https://you.com/?chatMode=default", icon: "https://favicon.im/you.com" },
  { name: "纳米搜索", url: "https://www.n.cn/", icon: "https://favicon.im/www.n.cn" },
  { name: "讯飞星火", url: "https://xinghuo.xfyun.cn/", icon: "https://favicon.im/xinghuo.xfyun.cn" },
  { name: "秘塔AI", url: "https://metaso.cn/", icon: "https://favicon.im/metaso.cn" }
]

export default function HomePage() {
  const [apps, setApps] = useState<App[]>([])
  const [showAddForm, setShowAddForm] = useState(false)

  useEffect(() => {
    // 从 localStorage 加载应用列表，如果没有则使用默认列表
    const savedApps = localStorage.getItem('shellplusplus-apps')
    if (savedApps) {
      try {
        setApps(JSON.parse(savedApps))
      } catch (error) {
        console.error('Failed to parse saved apps:', error)
        setApps(defaultApps)
      }
    } else {
      setApps(defaultApps)
    }
  }, [])

  const saveApps = (newApps: App[]) => {
    setApps(newApps)
    localStorage.setItem('shellplusplus-apps', JSON.stringify(newApps))
  }

  const addApp = (app: App) => {
    const newApps = [...apps, app]
    saveApps(newApps)
    setShowAddForm(false)
  }

  const removeApp = (appName: string) => {
    const newApps = apps.filter(app => app.name !== appName)
    saveApps(newApps)
  }

  const openApp = (app: App) => {
    window.open(app.url, '_blank')
  }

  return (
    <div className="content">
      <div className="app-grid">
        {apps.map((app) => (
          <AppItem
            key={app.name}
            app={app}
            onOpen={() => openApp(app)}
            onRemove={() => removeApp(app.name)}
          />
        ))}
        
        <AddAppItem
          onAdd={addApp}
          showForm={showAddForm}
          onToggleForm={() => setShowAddForm(!showAddForm)}
        />
      </div>
    </div>
  )
}
