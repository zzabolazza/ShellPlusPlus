'use client'

import { useEffect, useState } from 'react'
import { highlightFromIcon } from '@/lib/logoColor'

interface App {
  name: string
  url: string
  icon?: string
}

interface AppItemProps {
  app: App
  onOpen: () => void
}

export default function AppItem({ app, onOpen }: AppItemProps) {
  const [imageError, setImageError] = useState(false)
  const [highlight, setHighlight] = useState<string | null>(null)

  useEffect(() => {
    if (!app.icon) return
    let cancelled = false
    highlightFromIcon(app.icon)
      .then((color) => {
        if (!cancelled) setHighlight(color)
      })
      .catch(() => {
        if (!cancelled) setHighlight(null)
      })
    return () => {
      cancelled = true
    }
  }, [app.icon])

  return (
    <div
      className="app-item"
      style={highlight ? { ['--app-highlight' as string]: highlight } : undefined}
      onClick={onOpen}
    >
      <div className="app-icon">
        {app.icon && !imageError ? (
          <img
            src={app.icon}
            alt={app.name}
            onError={() => setImageError(true)}
          />
        ) : (
          <span>{app.name.charAt(0).toUpperCase()}</span>
        )}
      </div>
      <div className="app-name">{app.name}</div>
    </div>
  )
}
