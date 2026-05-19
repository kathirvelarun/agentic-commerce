import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Shield, Fingerprint, Laptop, Smartphone, Tablet,
  Plus, Trash2, CheckCircle, AlertTriangle, Lock,
  ChevronLeft, Globe, RefreshCw, ExternalLink, KeyRound,
  Clock, Calendar, Loader2, ShieldCheck, ShieldAlert, X
} from 'lucide-react'
import { useStore } from '../store'

// ── Helpers ───────────────────────────────────────────────────────────────────
const STORAGE_KEY = 'amex_sandbox_passkeys'

function loadPasskeys() {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]') } catch { return [] }
}
function savePasskeys(list) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
}

function deviceIcon(type) {
  if (type === 'mobile')  return Smartphone
  if (type === 'tablet')  return Tablet
  return Laptop
}

function relativeTime(iso) {
  const diff = Date.now() - new Date(iso).getTime()
  const min = Math.floor(diff / 60000)
  if (min < 1)   return 'Just now'
  if (min < 60)  return `${min}m ago`
  const hr = Math.floor(min / 60)
  if (hr < 24)   return `${hr}h ago`
  const d = Math.floor(hr / 24)
  if (d < 30)    return `${d}d ago`
  return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

function fmtDate(iso) {
  return new Date(iso).toLocaleDateString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit'
  })
}

// Seed demo passkeys if none exist
function seedIfEmpty(userId) {
  const existing = loadPasskeys().filter(p => p.userId === userId)
  if (existing.length > 0) return
  const demos = [
    {
      id: 'pk-demo-1',
      userId,
      name: 'MacBook Pro — Touch ID',
      deviceType: 'desktop',
      os: 'macOS 14.4',
      browser: 'Safari 17',
      createdAt: new Date(Date.now() - 7 * 86400000).toISOString(),
      lastUsedAt: new Date(Date.now() - 2 * 3600000).toISOString(),
      status: 'active',
      isDemo: true,
    },
  ]
  savePasskeys([...loadPasskeys(), ...demos])
}

// ── Fake browser chrome ───────────────────────────────────────────────────────
function FakeBrowserBar() {
  return (
    <div className="bg-[#3c3c3c] rounded-t-xl px-4 py-3 flex items-center gap-3 select-none">
      {/* Traffic lights */}
      <div className="flex gap-2 shrink-0">
        <div className="w-3 h-3 rounded-full bg-[#ff5f56]" />
        <div className="w-3 h-3 rounded-full bg-[#ffbd2e]" />
        <div className="w-3 h-3 rounded-full bg-[#27c93f]" />
      </div>
      {/* Address bar */}
      <div className="flex-1 bg-[#5a5a5a] rounded-md px-3 py-1.5 flex items-center gap-2 text-xs">
        <Lock className="w-3 h-3 text-[#4ade80] shrink-0" />
        <span className="text-[#e0e0e0] font-mono tracking-tight truncate">
          sandbox.auth.amex.com/passkey
        </span>
        <Globe className="w-3 h-3 text-[#9ca3af] ml-auto shrink-0" />
      </div>
      {/* Refresh */}
      <RefreshCw className="w-3.5 h-3.5 text-[#9ca3af] shrink-0" />
    </div>
  )
}

// ── Passkey row ───────────────────────────────────────────────────────────────
function PasskeyRow({ passkey, onRevoke, revoking }) {
  const Icon = deviceIcon(passkey.deviceType)
  const isRecent = Date.now() - new Date(passkey.lastUsedAt).getTime() < 86400000

  return (
    <div className="flex items-start gap-4 py-4 px-5 border-b border-[#e8ecf0] last:border-0 group hover:bg-blue-50/30 transition-colors">
      {/* Device icon */}
      <div className="w-10 h-10 rounded-xl bg-[#f0f4ff] border border-[#dce4f5] flex items-center justify-center shrink-0 mt-0.5">
        <Icon className="w-5 h-5 text-[#006fcf]" />
      </div>

      {/* Info */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 flex-wrap">
          <p className="text-sm font-semibold text-[#1a1a2e]">{passkey.name}</p>
          {passkey.isDemo && (
            <span className="text-[9px] font-bold uppercase tracking-wider px-1.5 py-0.5 rounded bg-amber-100 text-amber-700 border border-amber-200">
              Demo
            </span>
          )}
          <span className={`text-[10px] font-bold uppercase tracking-wider px-1.5 py-0.5 rounded flex items-center gap-0.5 ${
            passkey.status === 'active'
              ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
              : 'bg-red-50 text-red-700 border border-red-200'
          }`}>
            <span className={`w-1.5 h-1.5 rounded-full ${passkey.status === 'active' ? 'bg-emerald-500' : 'bg-red-500'}`} />
            {passkey.status}
          </span>
          {isRecent && (
            <span className="text-[10px] font-bold uppercase tracking-wider px-1.5 py-0.5 rounded bg-blue-50 text-blue-700 border border-blue-200">
              Recent
            </span>
          )}
        </div>

        <div className="flex flex-wrap gap-x-4 gap-y-0.5 mt-1">
          {passkey.os && (
            <p className="text-xs text-[#5a6a7a]">{passkey.os} · {passkey.browser}</p>
          )}
          <p className="text-xs text-[#5a6a7a] flex items-center gap-1">
            <Calendar className="w-3 h-3" /> Added {relativeTime(passkey.createdAt)}
          </p>
          <p className="text-xs text-[#5a6a7a] flex items-center gap-1">
            <Clock className="w-3 h-3" /> Last used {relativeTime(passkey.lastUsedAt)}
          </p>
        </div>
      </div>

      {/* Revoke */}
      <button
        onClick={() => onRevoke(passkey.id)}
        disabled={revoking === passkey.id}
        className="shrink-0 flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-lg border border-red-200 text-red-600 bg-red-50 hover:bg-red-100 disabled:opacity-50 transition-colors mt-0.5"
      >
        {revoking === passkey.id
          ? <Loader2 className="w-3.5 h-3.5 animate-spin" />
          : <Trash2 className="w-3.5 h-3.5" />}
        Revoke
      </button>
    </div>
  )
}

// ── Register new passkey modal ────────────────────────────────────────────────
function buildPasskeyRecord(userId, passkeyName, isDemo = false) {
  const ua = navigator.userAgent
  const isMobile = /iPhone|Android/i.test(ua)
  const isTablet = /iPad/i.test(ua)
  const os = /Mac/i.test(ua) ? 'macOS' : /Win/i.test(ua) ? 'Windows' : /Linux/i.test(ua) ? 'Linux' : 'Unknown OS'
  const browser = /Chrome/i.test(ua) ? 'Chrome' : /Safari/i.test(ua) ? 'Safari' : /Firefox/i.test(ua) ? 'Firefox' : 'Browser'
  return {
    id: 'pk-' + Date.now(),
    userId,
    name: passkeyName.trim(),
    deviceType: isMobile ? 'mobile' : isTablet ? 'tablet' : 'desktop',
    os,
    browser,
    createdAt: new Date().toISOString(),
    lastUsedAt: new Date().toISOString(),
    status: 'active',
    isDemo,
  }
}

function RegisterModal({ open, onClose, onRegistered, userId }) {
  const [name, setName] = useState('')
  const [step, setStep] = useState('form') // form | creating | success | error
  const [error, setError] = useState('')

  const reset = () => { setName(''); setStep('form'); setError('') }
  const handleClose = () => { reset(); onClose() }

  const commitPasskey = (isDemo = false) => {
    const record = buildPasskeyRecord(userId, name, isDemo)
    savePasskeys([...loadPasskeys(), record])
    onRegistered(record)
    setStep('success')
  }

  const handleRegister = async () => {
    if (!name.trim()) { setError('Please enter a name for this passkey.'); return }
    setStep('creating')
    setError('')
    try {
      const challenge = new Uint8Array(32)
      crypto.getRandomValues(challenge)

      await navigator.credentials.create({
        publicKey: {
          challenge,
          rp: { name: 'American Express', id: window.location.hostname },
          user: {
            id: new TextEncoder().encode(userId + ':pk-' + Date.now()),
            name: userId,
            displayName: userId
          },
          pubKeyCredParams: [
            { alg: -7,   type: 'public-key' },
            { alg: -257, type: 'public-key' }
          ],
          // No authenticatorAttachment — let the browser pick any available authenticator
          authenticatorSelection: {
            userVerification: 'preferred',
            residentKey: 'preferred'
          },
          timeout: 60000
        }
      })

      commitPasskey(false)
    } catch (err) {
      const msg = err.name === 'NotAllowedError'
        ? 'The browser prompt was dismissed or no authenticator is available.'
        : err.name === 'NotSupportedError'
          ? 'Passkeys are not supported on this device or browser.'
          : err.message || 'Registration failed.'
      setError(msg)
      setStep('error')
    }
  }

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={handleClose} />
      <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-md border border-[#dce4f5]">

        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#e8ecf0]">
          <div className="flex items-center gap-2">
            <KeyRound className="w-4 h-4 text-[#006fcf]" />
            <h2 className="font-semibold text-[#1a1a2e] text-sm">Register New Passkey</h2>
          </div>
          <button onClick={handleClose} className="p-1 rounded text-[#5a6a7a] hover:bg-[#f0f4ff] transition-colors">
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="px-6 py-5 space-y-4">
          {step === 'form' && (
            <>
              <p className="text-sm text-[#5a6a7a]">
                Give this passkey a name to help you identify it later (e.g. "Work MacBook" or "Personal iPhone").
              </p>
              <div className="flex flex-col gap-1">
                <label className="text-xs font-semibold text-[#5a6a7a] uppercase tracking-wide">Passkey Name</label>
                <input
                  autoFocus
                  type="text"
                  placeholder="e.g. Work MacBook"
                  value={name}
                  onChange={e => { setName(e.target.value); setError('') }}
                  onKeyDown={e => e.key === 'Enter' && handleRegister()}
                  className="w-full border border-[#dce4f5] rounded-lg px-3 py-2.5 text-sm text-[#1a1a2e] focus:outline-none focus:border-[#006fcf] focus:ring-2 focus:ring-[#006fcf]/10"
                />
                {error && <p className="text-xs text-red-600">{error}</p>}
              </div>
              <div className="flex gap-2 pt-1">
                <button onClick={handleClose}
                  className="flex-1 text-sm font-semibold px-4 py-2.5 rounded-lg border border-[#dce4f5] text-[#5a6a7a] hover:bg-[#f0f4ff] transition-colors">
                  Cancel
                </button>
                <button onClick={handleRegister} disabled={!name.trim()}
                  className="flex-1 text-sm font-semibold px-4 py-2.5 rounded-lg bg-[#006fcf] hover:bg-[#004a8f] text-white disabled:opacity-50 transition-colors flex items-center justify-center gap-2">
                  <Fingerprint className="w-4 h-4" /> Continue
                </button>
              </div>
            </>
          )}

          {step === 'creating' && (
            <div className="flex flex-col items-center py-8 gap-4 text-center">
              <div className="w-16 h-16 rounded-full bg-[#f0f4ff] border-2 border-[#006fcf]/20 flex items-center justify-center">
                <Fingerprint className="w-8 h-8 text-[#006fcf] animate-pulse" />
              </div>
              <div>
                <p className="font-semibold text-[#1a1a2e]">Waiting for authentication…</p>
                <p className="text-xs text-[#5a6a7a] mt-1">Follow your device's prompts to create the passkey.</p>
              </div>
            </div>
          )}

          {step === 'success' && (
            <div className="flex flex-col items-center py-6 gap-4 text-center">
              <div className="w-16 h-16 rounded-full bg-emerald-50 border-2 border-emerald-200 flex items-center justify-center">
                <CheckCircle className="w-8 h-8 text-emerald-600" />
              </div>
              <div>
                <p className="font-semibold text-[#1a1a2e] text-lg">Passkey Registered!</p>
                <p className="text-sm text-[#5a6a7a] mt-1">
                  <strong>"{name}"</strong> has been added to your account.
                </p>
              </div>
              <button onClick={handleClose}
                className="w-full text-sm font-semibold px-4 py-2.5 rounded-lg bg-[#006fcf] hover:bg-[#004a8f] text-white transition-colors flex items-center justify-center gap-2">
                <CheckCircle className="w-4 h-4" /> Done
              </button>
            </div>
          )}

          {step === 'error' && (
            <div className="flex flex-col items-center py-4 gap-4 text-center">
              <div className="w-12 h-12 rounded-full bg-red-50 border border-red-200 flex items-center justify-center">
                <AlertTriangle className="w-6 h-6 text-red-500" />
              </div>
              <div>
                <p className="font-semibold text-[#1a1a2e]">Device Authentication Failed</p>
                <p className="text-xs text-red-600 mt-1">{error}</p>
              </div>
              <div className="flex flex-col gap-2 w-full">
                <div className="flex gap-2">
                  <button onClick={handleClose}
                    className="flex-1 text-sm font-semibold px-4 py-2.5 rounded-lg border border-[#dce4f5] text-[#5a6a7a] hover:bg-[#f0f4ff] transition-colors">
                    Cancel
                  </button>
                  <button onClick={() => { setStep('form'); setError('') }}
                    className="flex-1 text-sm font-semibold px-4 py-2.5 rounded-lg bg-[#006fcf] hover:bg-[#004a8f] text-white transition-colors">
                    Try Again
                  </button>
                </div>
                {/* Demo fallback — skips real WebAuthn for sandbox environments */}
                <button onClick={() => commitPasskey(true)}
                  className="w-full text-xs font-semibold px-4 py-2 rounded-lg border border-amber-200 text-amber-700 bg-amber-50 hover:bg-amber-100 transition-colors flex items-center justify-center gap-1.5">
                  <span className="text-[10px] font-bold uppercase tracking-wider bg-amber-200 text-amber-800 px-1 rounded">Sandbox</span>
                  Continue in Demo Mode (skip device auth)
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

// ── Main page ─────────────────────────────────────────────────────────────────
export default function PasskeyManagerPage() {
  const navigate = useNavigate()
  const { userId, currentUser } = useStore()
  const [passkeys, setPasskeys] = useState([])
  const [revoking, setRevoking] = useState(null)
  const [registerOpen, setRegisterOpen] = useState(false)
  const [revokeConfirm, setRevokeConfirm] = useState(null)
  const [toast, setToast] = useState(null)

  useEffect(() => {
    seedIfEmpty(userId)
    setPasskeys(loadPasskeys().filter(p => p.userId === userId))
  }, [userId])

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type })
    setTimeout(() => setToast(null), 3500)
  }

  const handleRevoke = async (id) => {
    setRevokeConfirm(null)
    setRevoking(id)
    await new Promise(r => setTimeout(r, 800))
    const updated = loadPasskeys().filter(p => p.id !== id)
    savePasskeys(updated)
    setPasskeys(updated.filter(p => p.userId === userId))
    setRevoking(null)
    showToast('Passkey revoked successfully.')
  }

  const handleRegistered = (newKey) => {
    setPasskeys(loadPasskeys().filter(p => p.userId === userId))
    showToast(`"${newKey.name}" registered successfully.`)
  }

  const activeCount = passkeys.filter(p => p.status === 'active').length

  return (
    <div className="min-h-screen bg-[#f5f7fa] font-sans">

      {/* Fake browser wrapper */}
      <div className="max-w-4xl mx-auto px-4 pt-6 pb-12">
        <FakeBrowserBar />

        {/* Site chrome */}
        <div className="bg-white border border-[#dce4f5] border-t-0 rounded-b-xl shadow-xl overflow-hidden">

          {/* AMEX auth header */}
          <div className="bg-[#006fcf] px-8 py-5">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-4">
                <div className="bg-white rounded-md px-2.5 py-1.5">
                  <span className="text-[#006fcf] font-black text-sm tracking-tight">AMEX</span>
                </div>
                <div className="h-5 w-px bg-white/30" />
                <div>
                  <p className="text-white font-semibold text-sm">Security Center</p>
                  <p className="text-white/60 text-[11px]">sandbox.auth.amex.com</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="text-right">
                  <p className="text-white/70 text-[11px]">Signed in as</p>
                  <p className="text-white font-semibold text-xs">{currentUser}</p>
                </div>
                <div className="w-8 h-8 rounded-full bg-white/20 border border-white/30 flex items-center justify-center">
                  <span className="text-white font-bold text-sm">{currentUser?.[0]?.toUpperCase()}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Sandbox banner */}
          <div className="bg-amber-50 border-b border-amber-200 px-8 py-2 flex items-center gap-2">
            <AlertTriangle className="w-3.5 h-3.5 text-amber-600 shrink-0" />
            <p className="text-xs text-amber-700 font-medium">
              Sandbox environment — passkeys registered here are for testing only and not linked to real accounts.
            </p>
          </div>

          {/* Page content */}
          <div className="px-8 py-7">

            {/* Breadcrumb + back */}
            <div className="flex items-center gap-2 mb-6">
              <button onClick={() => navigate('/cards')}
                className="flex items-center gap-1 text-xs text-[#006fcf] hover:underline font-medium">
                <ChevronLeft className="w-3.5 h-3.5" /> Back to Cards
              </button>
              <span className="text-[#9ca3af] text-xs">/</span>
              <span className="text-xs text-[#5a6a7a]">Passkey Management</span>
            </div>

            {/* Title row */}
            <div className="flex items-start justify-between mb-6">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <KeyRound className="w-5 h-5 text-[#006fcf]" />
                  <h1 className="text-xl font-bold text-[#1a1a2e]">Your Passkeys</h1>
                </div>
                <p className="text-sm text-[#5a6a7a]">
                  Passkeys let you sign in securely using your device's biometrics — no password needed.
                </p>
              </div>
              <button
                onClick={() => setRegisterOpen(true)}
                className="flex items-center gap-2 text-sm font-semibold px-4 py-2.5 rounded-lg bg-[#006fcf] hover:bg-[#004a8f] text-white transition-colors shrink-0 ml-4"
              >
                <Plus className="w-4 h-4" /> Add Passkey
              </button>
            </div>

            {/* Stats strip */}
            <div className="grid grid-cols-3 gap-4 mb-6">
              {[
                { label: 'Registered',  value: passkeys.length,  icon: KeyRound,     color: 'text-[#006fcf]', bg: 'bg-[#f0f4ff]' },
                { label: 'Active',      value: activeCount,       icon: ShieldCheck,  color: 'text-emerald-600', bg: 'bg-emerald-50' },
                { label: 'Revoked',     value: passkeys.length - activeCount, icon: ShieldAlert, color: 'text-red-500', bg: 'bg-red-50' },
              ].map(({ label, value, icon: Icon, color, bg }) => (
                <div key={label} className="bg-white border border-[#e8ecf0] rounded-xl p-4 flex items-center gap-3">
                  <div className={`w-9 h-9 rounded-xl ${bg} flex items-center justify-center`}>
                    <Icon className={`w-4.5 h-4.5 ${color}`} />
                  </div>
                  <div>
                    <p className="text-xl font-bold text-[#1a1a2e] leading-none">{value}</p>
                    <p className="text-xs text-[#5a6a7a] mt-0.5">{label}</p>
                  </div>
                </div>
              ))}
            </div>

            {/* Passkey list */}
            <div className="bg-white border border-[#e8ecf0] rounded-xl overflow-hidden">

              {/* List header */}
              <div className="px-5 py-3 bg-[#f8fafc] border-b border-[#e8ecf0] flex items-center justify-between">
                <p className="text-xs font-semibold text-[#5a6a7a] uppercase tracking-wider">
                  Registered Devices
                </p>
                <p className="text-xs text-[#9ca3af]">{passkeys.length} passkey{passkeys.length !== 1 ? 's' : ''}</p>
              </div>

              {passkeys.length === 0 ? (
                <div className="flex flex-col items-center py-16 gap-3 text-center px-8">
                  <div className="w-14 h-14 rounded-full bg-[#f0f4ff] flex items-center justify-center">
                    <KeyRound className="w-7 h-7 text-[#006fcf]" />
                  </div>
                  <p className="font-semibold text-[#1a1a2e]">No passkeys registered</p>
                  <p className="text-sm text-[#5a6a7a] max-w-xs">
                    Add a passkey to enable fast, secure sign-in using your device's biometrics.
                  </p>
                  <button
                    onClick={() => setRegisterOpen(true)}
                    className="flex items-center gap-2 text-sm font-semibold px-4 py-2 rounded-lg bg-[#006fcf] hover:bg-[#004a8f] text-white transition-colors mt-2"
                  >
                    <Plus className="w-4 h-4" /> Add Your First Passkey
                  </button>
                </div>
              ) : (
                passkeys.map(pk => (
                  <PasskeyRow
                    key={pk.id}
                    passkey={pk}
                    revoking={revoking}
                    onRevoke={(id) => setRevokeConfirm(id)}
                  />
                ))
              )}
            </div>

            {/* Info box */}
            <div className="mt-5 bg-[#f0f4ff] border border-[#dce4f5] rounded-xl p-4 flex gap-3">
              <Shield className="w-4 h-4 text-[#006fcf] shrink-0 mt-0.5" />
              <div className="text-xs text-[#3a4a6a] leading-relaxed">
                <strong>About passkeys:</strong> Passkeys are stored on your device and never sent to our servers.
                Revoking a passkey removes its authorization from your account — you will be prompted to
                set up a new one next time you sign in from that device.
              </div>
            </div>

          </div>

          {/* Footer */}
          <div className="px-8 py-4 bg-[#f8fafc] border-t border-[#e8ecf0] flex items-center justify-between">
            <p className="text-[11px] text-[#9ca3af]">
              American Express · Security Center · Sandbox v2.1.0
            </p>
            <div className="flex gap-4">
              {['Privacy Policy', 'Terms of Use', 'Help'].map(l => (
                <a key={l} href="#" className="text-[11px] text-[#5a6a7a] hover:text-[#006fcf] flex items-center gap-0.5 transition-colors">
                  {l} <ExternalLink className="w-2.5 h-2.5" />
                </a>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Revoke confirm dialog */}
      {revokeConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={() => setRevokeConfirm(null)} />
          <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-sm border border-[#dce4f5] p-6 text-center">
            <div className="w-12 h-12 rounded-full bg-red-50 border border-red-200 flex items-center justify-center mx-auto mb-3">
              <AlertTriangle className="w-6 h-6 text-red-500" />
            </div>
            <p className="font-semibold text-[#1a1a2e] text-base">Revoke this passkey?</p>
            <p className="text-sm text-[#5a6a7a] mt-1 mb-5">
              This device will no longer be able to sign in using its passkey. This cannot be undone.
            </p>
            <div className="flex gap-2">
              <button onClick={() => setRevokeConfirm(null)}
                className="flex-1 text-sm font-semibold px-4 py-2.5 rounded-lg border border-[#dce4f5] text-[#5a6a7a] hover:bg-[#f0f4ff] transition-colors">
                Cancel
              </button>
              <button onClick={() => handleRevoke(revokeConfirm)}
                className="flex-1 text-sm font-semibold px-4 py-2.5 rounded-lg bg-red-600 hover:bg-red-700 text-white transition-colors">
                Revoke
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Register modal */}
      <RegisterModal
        open={registerOpen}
        onClose={() => setRegisterOpen(false)}
        onRegistered={handleRegistered}
        userId={userId}
      />

      {/* Toast */}
      {toast && (
        <div className={`fixed bottom-6 right-6 z-50 flex items-center gap-2 px-4 py-3 rounded-lg shadow-lg text-sm font-medium border ${
          toast.type === 'success'
            ? 'bg-white border-emerald-200 text-emerald-800'
            : 'bg-white border-red-200 text-red-800'
        }`}>
          {toast.type === 'success'
            ? <CheckCircle className="w-4 h-4 text-emerald-600 shrink-0" />
            : <AlertTriangle className="w-4 h-4 text-red-500 shrink-0" />}
          {toast.msg}
        </div>
      )}
    </div>
  )
}