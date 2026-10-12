'use client'

import { useState } from 'react'

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

  return (
    <div className="app-item" onClick={onOpen}>
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
