import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Lock, User, AlertCircle, Eye, EyeOff } from 'lucide-react'
import { useStore } from '../store'

const VALID_USERNAME = 'demo-user'
const VALID_PASSWORD = 'admin'

export default function LoginPage() {
  const { login } = useStore()
  const navigate   = useNavigate()

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPwd, setShowPwd]   = useState(false)
  const [error, setError]       = useState('')
  const [loading, setLoading]   = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (!username.trim() || !password.trim()) {
      setError('Please enter your username and password.')
      return
    }

    setLoading(true)
    // Simulate a brief network delay for realism
    await new Promise(r => setTimeout(r, 600))

    if (username.trim() === VALID_USERNAME && password === VALID_PASSWORD) {
      login(username.trim())
      navigate('/', { replace: true })
    } else {
      setError('Invalid username or password. Please try again.')
    }
    setLoading(false)
  }

  return (
    <div className="min-h-screen bg-surface-light flex flex-col items-center justify-center p-4">

      {/* Card */}
      <div className="w-full max-w-sm bg-white rounded-xl border border-surface-border shadow-amex-md overflow-hidden">

        {/* Blue header strip */}
        <div className="bg-amex-navy px-8 py-6 flex flex-col items-center gap-3">
          <div className="bg-amex-blue rounded-lg px-3 py-2">
            <span className="text-white font-black text-xl tracking-tight leading-none">AMEX</span>
          </div>
          <div className="text-center">
            <p className="text-white font-bold text-base">Commerce Portal</p>
            <p className="text-white/60 text-xs mt-0.5">Member Sign In</p>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="px-8 py-7 flex flex-col gap-5">

          {/* Error banner */}
          {error && (
            <div className="flex items-start gap-2 bg-red-50 border border-red-200 rounded-lg px-3 py-2.5">
              <AlertCircle className="w-4 h-4 text-red-500 shrink-0 mt-0.5" />
              <p className="text-xs text-red-700 font-medium">{error}</p>
            </div>
          )}

          {/* Username */}
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-semibold text-ink-secondary uppercase tracking-wide">
              Username
            </label>
            <div className="relative">
              <User className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-muted" />
              <input
                type="text"
                value={username}
                onChange={e => { setUsername(e.target.value); setError('') }}
                placeholder="demo-user"
                autoComplete="username"
                className="w-full pl-9 pr-3 py-2.5 border border-surface-border rounded-lg text-sm text-ink-primary placeholder-ink-muted focus:outline-none focus:border-amex-blue focus:ring-2 focus:ring-amex-light transition-colors"
              />
            </div>
          </div>

          {/* Password */}
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-semibold text-ink-secondary uppercase tracking-wide">
              Password
            </label>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-muted" />
              <input
                type={showPwd ? 'text' : 'password'}
                value={password}
                onChange={e => { setPassword(e.target.value); setError('') }}
                placeholder="••••••"
                autoComplete="current-password"
                className="w-full pl-9 pr-9 py-2.5 border border-surface-border rounded-lg text-sm text-ink-primary placeholder-ink-muted focus:outline-none focus:border-amex-blue focus:ring-2 focus:ring-amex-light transition-colors"
              />
              <button
                type="button"
                onClick={() => setShowPwd(v => !v)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-ink-muted hover:text-ink-primary transition-colors"
              >
                {showPwd ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={loading}
            className="w-full flex items-center justify-center gap-2 bg-amex-blue hover:bg-amex-dark disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold text-sm py-2.5 rounded-lg transition-colors"
          >
            {loading
              ? <span className="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin" />
              : <Lock className="w-4 h-4" />}
            {loading ? 'Signing in…' : 'Sign In'}
          </button>

          {/* Hint */}
          <p className="text-center text-[11px] text-ink-muted">
            Demo credentials: <span className="font-mono font-semibold text-ink-secondary">demo-user</span> / <span className="font-mono font-semibold text-ink-secondary">admin</span>
          </p>
        </form>
      </div>

      <p className="text-[11px] text-ink-muted mt-6">
        © 2026 AMEX Commerce · All rights reserved
      </p>
    </div>
  )
}