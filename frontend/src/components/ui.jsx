import React from 'react'
import { Loader2, X, Check, AlertCircle, Info, Star } from 'lucide-react'

// ─── Button ──────────────────────────────────────────────────────────────────
export function Button({ children, variant = 'primary', size = 'md', loading, disabled, className = '', ...props }) {
  const base = 'inline-flex items-center justify-center gap-2 font-semibold transition-all duration-150 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed select-none'
  const variants = {
    primary:   'bg-amex-blue hover:bg-amex-dark text-white rounded-amex',
    secondary: 'bg-surface-light hover:bg-surface-section text-ink-primary border border-surface-border rounded-amex',
    ghost:     'bg-transparent hover:bg-amex-light text-amex-blue rounded-amex',
    danger:    'bg-red-50 hover:bg-red-100 text-red-700 border border-red-200 rounded-amex',
    outline:   'bg-transparent border-2 border-amex-blue text-amex-blue hover:bg-amex-light rounded-amex',
    navy:      'bg-amex-navy hover:bg-amex-dark text-white rounded-amex',
  }
  const sizes = { sm: 'px-3 py-1.5 text-xs', md: 'px-5 py-2.5 text-sm', lg: 'px-6 py-3 text-base', xl: 'px-8 py-4 text-lg', icon: 'p-2' }
  return (
    <button className={`${base} ${variants[variant]} ${sizes[size]} ${className}`} disabled={disabled || loading} {...props}>
      {loading && <Loader2 className="w-4 h-4 animate-spin" />}
      {children}
    </button>
  )
}

// ─── Badge ────────────────────────────────────────────────────────────────────
export function Badge({ children, color = 'blue' }) {
  const colors = {
    blue:   'bg-amex-light text-amex-blue',
    navy:   'bg-amex-navy text-white',
    green:  'bg-emerald-50 text-emerald-700',
    amber:  'bg-amber-50 text-amber-700',
    red:    'bg-red-50 text-red-700',
    gray:   'bg-surface-light text-ink-secondary',
  }
  return <span className={`amex-badge ${colors[color] || colors.gray}`}>{children}</span>
}

// ─── Card ─────────────────────────────────────────────────────────────────────
export function Card({ children, className = '', hover = false, onClick }) {
  return (
    <div onClick={onClick} className={`amex-card ${hover ? 'cursor-pointer' : ''} ${className}`}>
      {children}
    </div>
  )
}

// ─── Star Rating ──────────────────────────────────────────────────────────────
export function StarRating({ rating, max = 5 }) {
  return (
    <div className="flex items-center gap-0.5">
      {[...Array(max)].map((_, i) => (
        <Star key={i} className={`w-3 h-3 ${i < Math.floor(rating) ? 'star-filled fill-current' : 'star-empty'}`} />
      ))}
      <span className="ml-1 text-xs text-ink-muted">{rating?.toFixed(1)}</span>
    </div>
  )
}

// ─── Price ────────────────────────────────────────────────────────────────────
export function Price({ amount, size = 'md' }) {
  const sizes = { sm: 'text-sm', md: 'text-lg', lg: 'text-2xl', xl: 'text-3xl' }
  return (
    <span className={`font-bold text-amex-blue ${sizes[size]}`}>
      ${Number(amount).toFixed(2)}
    </span>
  )
}

// ─── Input ────────────────────────────────────────────────────────────────────
export function Input({ label, error, className = '', ...props }) {
  return (
    <div className="flex flex-col gap-1">
      {label && <label className="text-xs font-semibold text-ink-secondary uppercase tracking-wide">{label}</label>}
      <input
        className={`w-full bg-white border border-surface-border rounded-amex px-3 py-2.5 text-sm text-ink-primary placeholder-ink-muted focus:outline-none focus:border-amex-blue focus:ring-2 focus:ring-amex-light transition-colors ${className}`}
        {...props}
      />
      {error && <span className="text-xs text-red-600">{error}</span>}
    </div>
  )
}

// ─── Spinner ─────────────────────────────────────────────────────────────────
export function Spinner({ size = 'md' }) {
  const sizes = { sm: 'w-4 h-4', md: 'w-6 h-6', lg: 'w-8 h-8' }
  return <Loader2 className={`${sizes[size]} animate-spin text-amex-blue`} />
}

// ─── Notifications ────────────────────────────────────────────────────────────
export function Notifications({ notifications }) {
  const icons = { success: Check, error: X, warning: AlertCircle, info: Info }
  const styles = {
    success: { wrap: 'border-emerald-200 bg-white shadow-lg', icon: 'bg-emerald-100 text-emerald-600', text: 'text-emerald-800', sub: 'text-emerald-600', bar: 'bg-emerald-400' },
    error:   { wrap: 'border-red-200 bg-white shadow-lg',     icon: 'bg-red-100 text-red-600',         text: 'text-red-800',   sub: 'text-red-500',   bar: 'bg-red-400' },
    warning: { wrap: 'border-amber-200 bg-white shadow-lg',   icon: 'bg-amber-100 text-amber-600',     text: 'text-amber-800', sub: 'text-amber-600', bar: 'bg-amber-400' },
    info:    { wrap: 'border-blue-200 bg-white shadow-lg',    icon: 'bg-amex-light text-amex-blue',    text: 'text-amex-blue', sub: 'text-amex-blue', bar: 'bg-amex-blue' },
  }
  return (
    <div className="fixed bottom-6 right-6 z-50 flex flex-col gap-2 max-w-xs w-full">
      {notifications.map(({ id, msg, type }) => {
        const Icon = icons[type] || Check
        const s = styles[type] || styles.info
        return (
          <div key={id}
            className={`relative flex items-start gap-3 px-4 py-3 rounded-lg border text-sm font-medium animate-in overflow-hidden ${s.wrap}`}
          >
            <div className={`w-7 h-7 rounded-full flex items-center justify-center shrink-0 ${s.icon}`}>
              <Icon className="w-4 h-4" />
            </div>
            <div className="flex-1 min-w-0 pt-0.5">
              <p className={`font-semibold text-xs uppercase tracking-wide mb-0.5 ${s.sub}`}>
                {type === 'success' ? 'Success' : type === 'error' ? 'Error' : type === 'warning' ? 'Warning' : 'Notice'}
              </p>
              <p className={`text-sm leading-snug ${s.text}`}>{msg}</p>
            </div>
            {/* Progress bar */}
            <div className={`absolute bottom-0 left-0 h-0.5 ${s.bar}`}
              style={{ animation: 'shrinkBar 3s linear forwards' }} />
          </div>
        )
      })}
    </div>
  )
}

// ─── Modal ────────────────────────────────────────────────────────────────────
export function Modal({ open, onClose, title, children, footer }) {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/30 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-white rounded-lg shadow-amex-lg w-full max-w-lg animate-in border border-surface-border">
        <div className="flex items-center justify-between px-6 py-4 border-b border-surface-muted">
          <h2 className="font-semibold text-ink-primary text-base">{title}</h2>
          <button onClick={onClose} className="p-1 rounded hover:bg-surface-light text-ink-muted transition-colors">
            <X className="w-4 h-4" />
          </button>
        </div>
        <div className="px-6 py-5">{children}</div>
        {footer && <div className="px-6 py-4 border-t border-surface-muted bg-surface-light flex justify-end gap-2 rounded-b-lg">{footer}</div>}
      </div>
    </div>
  )
}

// ─── Skeleton ─────────────────────────────────────────────────────────────────
export function Skeleton({ className = '' }) {
  return (
    <div className={`bg-surface-section rounded animate-pulse ${className}`} />
  )
}

// ─── Section Header ───────────────────────────────────────────────────────────
export function SectionHeader({ title, subtitle, action }) {
  return (
    <div className="flex items-end justify-between mb-6">
      <div>
        <h1 className="text-2xl font-bold text-ink-primary">{title}</h1>
        {subtitle && <p className="text-sm text-ink-secondary mt-1">{subtitle}</p>}
      </div>
      {action}
    </div>
  )
}

// ─── Amex Logo ───────────────────────────────────────────────────────────────
export function AmexLogo({ size = 'md' }) {
  const sizes = { sm: 'w-7 h-7 text-xs', md: 'w-9 h-9 text-sm', lg: 'w-12 h-12 text-base' }
  return (
    <div className={`amex-logo-box ${sizes[size]} font-black`}>
      <span>A</span>
    </div>
  )
}
