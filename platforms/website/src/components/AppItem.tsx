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
  onRemove: () => void
}

export default function AppItem({ app, onOpen, onRemove }: AppItemProps) {
  const [imageError, setImageError] = useState(false)

  const handleImageError = () => {
    setImageError(true)
  }

  const handleClick = () => {
    onOpen()
  }

  const handleRemove = (e: React.MouseEvent) => {
    e.stopPropagation()
    if (confirm(`确定要删除应用 "${app.name}" 吗？`)) {
      onRemove()
    }
  }

  return (
    <div className="app-item" onClick={handleClick}>
      {/* 删除按钮 */}
      <button
        className="delete-button"
        onClick={handleRemove}
        style={{
          position: 'absolute',
          top: '5px',
          right: '5px',
          width: '20px',
          height: '20px',
          borderRadius: '50%',
          backgroundColor: '#dc3545',
          color: 'white',
          border: 'none',
          cursor: 'pointer',
          display: 'none',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '12px',
          lineHeight: 1,
          zIndex: 10,
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.display = 'flex'
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.display = 'none'
        }}
      >
        ×
      </button>

      {/* 应用图标 */}
      <div className="app-icon">
        {app.icon && !imageError ? (
          <img
            src={app.icon}
            alt={app.name}
            onError={handleImageError}
          />
        ) : (
          <span>{app.name.charAt(0).toUpperCase()}</span>
        )}
      </div>

      {/* 应用名称 */}
      <div className="app-name">{app.name}</div>

      <style jsx>{`
        .app-item:hover .delete-button {
          display: flex !important;
        }
        
        .delete-button:hover {
          background-color: #c82333 !important;
        }
      `}</style>
    </div>
  )
}
