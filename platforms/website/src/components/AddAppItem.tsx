'use client'

import { useState } from 'react'

interface App {
  name: string
  url: string
  icon?: string
}

interface AddAppItemProps {
  onAdd: (app: App) => void
  showForm: boolean
  onToggleForm: () => void
}

export default function AddAppItem({ onAdd, showForm, onToggleForm }: AddAppItemProps) {
  const [formData, setFormData] = useState({ name: '', url: '' })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!formData.name.trim() || !formData.url.trim()) {
      alert('请填写完整的应用信息')
      return
    }

    // 验证URL格式
    try {
      new URL(formData.url)
    } catch (error) {
      alert('请输入有效的网址')
      return
    }

    // 提取域名作为图标
    const domain = new URL(formData.url).hostname
    const icon = `https://favicon.im/${domain}`

    const newApp: App = {
      name: formData.name.trim(),
      url: formData.url.trim(),
      icon
    }

    onAdd(newApp)
    setFormData({ name: '', url: '' })
  }

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  if (showForm) {
    return (
      <div className="add-app-form">
        <div className="form-content">
          <h2 className="form-title">添加新应用</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="appName">应用名称:</label>
              <input
                type="text"
                id="appName"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                required
                placeholder="例如: ChatGPT"
                autoFocus
              />
            </div>
            <div className="form-group">
              <label htmlFor="appUrl">应用网址:</label>
              <input
                type="url"
                id="appUrl"
                name="url"
                value={formData.url}
                onChange={handleInputChange}
                required
                placeholder="例如: https://chatgpt.com/"
              />
            </div>
            <div className="form-buttons">
              <button
                type="button"
                className="form-button secondary"
                onClick={onToggleForm}
              >
                取消
              </button>
              <button type="submit" className="form-button primary">
                添加
              </button>
            </div>
          </form>
        </div>

        <style jsx>{`
          .add-app-form {
            display: flex;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            z-index: 1000;
            align-items: center;
            justify-content: center;
          }

          .form-content {
            background-color: white;
            padding: 30px;
            border-radius: 10px;
            width: 90%;
            max-width: 400px;
            box-shadow: 0 4px 20px rgba(214, 213, 213, 0.15);
          }

          .form-title {
            font-size: 20px;
            margin-bottom: 20px;
            color: #333;
            text-align: center;
          }

          .form-group {
            margin-bottom: 15px;
          }

          .form-group label {
            display: block;
            margin-bottom: 5px;
            color: #555;
            font-weight: 500;
          }

          .form-group input {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 14px;
            box-sizing: border-box;
          }

          .form-group input:focus {
            outline: none;
            border-color: #007bff;
            box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
          }

          .form-buttons {
            display: flex;
            gap: 10px;
            justify-content: flex-end;
            margin-top: 20px;
          }

          .form-button {
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            transition: background-color 0.2s;
          }

          .form-button.primary {
            background-color: #007bff;
            color: white;
          }

          .form-button.primary:hover {
            background-color: #0056b3;
          }

          .form-button.secondary {
            background-color: #6c757d;
            color: white;
          }

          .form-button.secondary:hover {
            background-color: #545b62;
          }
        `}</style>
      </div>
    )
  }

  return (
    <div className="add-app-item" onClick={onToggleForm}>
      <div className="add-app-icon">+</div>
    </div>
  )
}
