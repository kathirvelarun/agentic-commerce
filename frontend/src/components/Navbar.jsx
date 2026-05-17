import React, { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { ShoppingCart, Bot, Package, CreditCard, Menu, X, Activity, User, LogOut } from 'lucide-react'
import { useStore } from '../store'

export default function Navbar() {
  const { cartOpen, setCartOpen, getCartCount, currentUser, logout } = useStore()
  const location = useLocation()
  const navigate  = useNavigate()
  const [mobileOpen, setMobileOpen] = useState(false)
  const count = getCartCount()

  const links = [
    { to: '/',       label: 'Shop',      icon: Package    },
    { to: '/agent',  label: 'AI Agent',  icon: Bot        },
    { to: '/orders', label: 'Orders',    icon: Package    },
    { to: '/cards',  label: 'Cards',     icon: CreditCard },
    { to: '/logs',   label: 'Flow Logs', icon: Activity   },
  ]

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <>
      {/* Top utility bar */}
      <div className="bg-amex-navy text-white text-xs py-1.5 hidden md:block">
        <div className="max-w-7xl mx-auto px-6 flex justify-end gap-6 items-center">
          <span className="text-white/70">
            Welcome, <span className="text-white font-semibold">{currentUser}</span>
          </span>
          <button
            onClick={handleLogout}
            className="flex items-center gap-1 hover:text-white text-white/70 transition-colors"
          >
            <LogOut className="w-3 h-3" /> Sign Out
          </button>
        </div>
      </div>

      {/* Main navbar */}
      <header className="bg-white border-b border-surface-border sticky top-0 z-40 shadow-amex-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <div className="flex items-center h-16 gap-8">

            {/* Logo */}
            <Link to="/" className="flex items-center gap-3 shrink-0">
              <div className="bg-amex-blue rounded px-2 py-1.5 flex items-center gap-1.5">
                <span className="text-white font-black text-sm tracking-tight leading-none">AMEX</span>
              </div>
              <span className="font-bold text-ink-primary text-base hidden sm:block tracking-tight">
                Commerce
              </span>
            </Link>

            <div className="h-6 w-px bg-surface-border hidden md:block" />

            {/* Desktop nav links */}
            <nav className="hidden md:flex items-center gap-1 flex-1">
              {links.map(({ to, label, icon: Icon }) => {
                const active = location.pathname === to
                return (
                  <Link
                    key={to}
                    to={to}
                    className={`flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-amex transition-all ${
                      active
                        ? 'text-amex-blue bg-amex-light'
                        : 'text-ink-secondary hover:text-amex-blue hover:bg-surface-light'
                    }`}
                  >
                    <Icon className="w-4 h-4" />
                    {label}
                    {label === 'AI Agent' && (
                      <span className="amex-badge bg-amex-blue text-white text-[9px] px-1.5 py-0.5 ml-0.5">AI</span>
                    )}
                  </Link>
                )
              })}
            </nav>

            {/* Right side actions */}
            <div className="flex items-center gap-2 ml-auto">
              {/* Cart */}
              <button
                onClick={() => setCartOpen(!cartOpen)}
                className="relative flex items-center gap-2 px-3 py-2 rounded-amex text-ink-secondary hover:text-amex-blue hover:bg-surface-light transition-all text-sm font-medium"
              >
                <ShoppingCart className="w-5 h-5" />
                <span className="hidden sm:block">Cart</span>
                {count > 0 && (
                  <span className="absolute -top-1 -right-1 w-5 h-5 bg-amex-blue rounded-full text-[10px] text-white flex items-center justify-center font-bold">
                    {count > 9 ? '9+' : count}
                  </span>
                )}
              </button>

              {/* Logged-in user chip */}
              <div className="hidden md:flex items-center gap-2 bg-amex-light border border-amex-blue/20 rounded-amex px-3 py-1.5">
                <div className="w-5 h-5 rounded-full bg-amex-blue flex items-center justify-center">
                  <User className="w-3 h-3 text-white" />
                </div>
                <span className="text-xs font-semibold text-amex-blue">{currentUser}</span>
              </div>

              {/* Mobile menu toggle */}
              <button
                className="md:hidden p-2 rounded-amex text-ink-secondary hover:bg-surface-light transition-colors"
                onClick={() => setMobileOpen(!mobileOpen)}
              >
                {mobileOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
              </button>
            </div>
          </div>

          {/* Mobile nav */}
          {mobileOpen && (
            <div className="md:hidden border-t border-surface-muted py-3 space-y-1">
              {links.map(({ to, label, icon: Icon }) => (
                <Link
                  key={to}
                  to={to}
                  onClick={() => setMobileOpen(false)}
                  className={`flex items-center gap-2 px-3 py-2.5 rounded-amex text-sm font-medium transition-all ${
                    location.pathname === to
                      ? 'text-amex-blue bg-amex-light'
                      : 'text-ink-secondary hover:text-amex-blue'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {label}
                </Link>
              ))}
              <button
                onClick={() => { setMobileOpen(false); handleLogout() }}
                className="flex items-center gap-2 px-3 py-2.5 rounded-amex text-sm font-medium text-red-500 hover:bg-red-50 w-full transition-all"
              >
                <LogOut className="w-4 h-4" /> Sign Out
              </button>
            </div>
          )}
        </div>
      </header>
    </>
  )
}