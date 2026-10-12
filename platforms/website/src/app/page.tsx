'use client'

import { useEffect, useRef, useState, type FormEvent } from 'react'
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
  const [sourceOpen, setSourceOpen] = useState(false)
  const sourceAnchorRef = useRef<HTMLDivElement>(null)

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

  useEffect(() => {
    if (!sourceOpen) return

    const closeOnOutside = (event: MouseEvent) => {
      if (!sourceAnchorRef.current?.contains(event.target as Node)) {
        setSourceOpen(false)
      }
    }
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setSourceOpen(false)
    }

    document.addEventListener('mousedown', closeOnOutside)
    document.addEventListener('keydown', closeOnEscape)
    return () => {
      document.removeEventListener('mousedown', closeOnOutside)
      document.removeEventListener('keydown', closeOnEscape)
    }
  }, [sourceOpen])

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
      </div>
      <div className="source-anchor" ref={sourceAnchorRef}>
        <button
          type="button"
          className="source-toggle"
          aria-label="设置"
          aria-expanded={sourceOpen}
          onClick={() => setSourceOpen((open) => !open)}
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M19.14 12.94c.04-.31.06-.63.06-.94s-.02-.63-.06-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.49.49 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94L14.4 2.81a.48.48 0 0 0-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96a.49.49 0 0 0-.59.22L2.74 8.87a.49.49 0 0 0 .12.61l2.03 1.58c-.04.31-.06.63-.06.94s.02.63.06.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32a.49.49 0 0 0-.12-.61l-2.01-1.58zM12 15.6A3.6 3.6 0 1 1 12 8.4a3.6 3.6 0 0 1 0 7.2z" />
          </svg>
        </button>
        {sourceOpen ? (
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
        ) : null}
      </div>
    </div>
  )
}
