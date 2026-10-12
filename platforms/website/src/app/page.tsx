'use client'

import { useEffect, useState, type FormEvent } from 'react'
import AppItem from '@/components/AppItem'

interface App {
  name: string
  url: string
  icon?: string
  packageName?: string
}

const DEFAULT_SOURCE_URL = 'https://raw.githubusercontent.com/zzabolazza/ShellPlusPlus/main/apps.json'
const APPS_CACHE_KEY = 'shellplusplus-apps'
const SOURCE_CACHE_KEY = 'shellplusplus-app-source'

const bundledApps: App[] = [
  { name: 'ChatGPT', url: 'https://chatgpt.com/', icon: 'https://favicon.im/chatgpt.com' },
  { name: 'Perplexity', url: 'https://www.perplexity.ai/', icon: 'https://favicon.im/www.perplexity.ai' }
]

function iconFor(url: string) {
  try {
    return `https://favicon.im/${new URL(url).hostname}`
  } catch {
    return undefined
  }
}

function normalizeApps(list: unknown): App[] {
  if (!Array.isArray(list)) return []
  return list.flatMap((item) => {
    if (!item || typeof item !== 'object') return []
    const record = item as { name?: string; url?: string; packageName?: string }
    if (!record.name || !record.url) return []
    return [{
      name: record.name,
      url: record.url,
      packageName: record.packageName,
      icon: iconFor(record.url)
    }]
  })
}

function readCachedApps(): App[] | null {
  const savedApps = localStorage.getItem(APPS_CACHE_KEY)
  if (!savedApps) return null
  try {
    const apps = normalizeApps(JSON.parse(savedApps))
    return apps.length > 0 ? apps : null
  } catch {
    return null
  }
}

async function fetchApps(sourceUrl: string): Promise<App[]> {
  const separator = sourceUrl.includes('?') ? '&' : '?'
  const response = await fetch(`${sourceUrl}${separator}t=${Date.now()}`, {
    cache: 'no-store'
  })
  if (!response.ok) {
    throw new Error(`应用列表请求失败: ${response.status}`)
  }
  return normalizeApps(await response.json())
}

export default function HomePage() {
  const [apps, setApps] = useState<App[]>(bundledApps)
  const [sourceText, setSourceText] = useState(DEFAULT_SOURCE_URL)
  const [status, setStatus] = useState('')

  useEffect(() => {
    const savedSource = localStorage.getItem(SOURCE_CACHE_KEY)?.trim() || DEFAULT_SOURCE_URL
    setSourceText(savedSource)
    const cached = readCachedApps()
    if (cached) setApps(cached)

    let cancelled = false
    fetchApps(savedSource)
      .then((remoteApps) => {
        if (cancelled || remoteApps.length === 0) return
        setApps(remoteApps)
        setStatus('')
        localStorage.setItem(APPS_CACHE_KEY, JSON.stringify(remoteApps))
      })
      .catch((error) => {
        console.error('同步应用列表失败:', error)
        if (!cancelled) setStatus('同步失败，继续使用上次的列表')
      })

    return () => {
      cancelled = true
    }
  }, [])

  const saveSource = async (event: FormEvent) => {
    event.preventDefault()
    const nextSource = sourceText.trim()
    if (!nextSource) return
    localStorage.setItem(SOURCE_CACHE_KEY, nextSource)
    setStatus('')
    try {
      const remoteApps = await fetchApps(nextSource)
      if (remoteApps.length === 0) {
        setStatus('源地址没有返回应用')
        return
      }
      setApps(remoteApps)
      localStorage.setItem(APPS_CACHE_KEY, JSON.stringify(remoteApps))
    } catch (error) {
      console.error('同步应用列表失败:', error)
      setStatus('同步失败，继续使用上次的列表')
    }
  }

  return (
    <div className="content">
      <div className="home">
        <div className="app-grid">
          {apps.map((app) => (
            <AppItem
              key={app.packageName || app.name}
              app={app}
              onOpen={() => window.open(app.url, '_blank')}
            />
          ))}
        </div>
        <form className="source-form" onSubmit={saveSource}>
          <label htmlFor="appListSource">应用列表源</label>
          <input
            id="appListSource"
            type="url"
            value={sourceText}
            onChange={(event) => setSourceText(event.target.value)}
            placeholder={DEFAULT_SOURCE_URL}
          />
          <button type="submit">保存</button>
          {status ? <span className="source-current">{status}</span> : null}
        </form>
      </div>
    </div>
  )
}
