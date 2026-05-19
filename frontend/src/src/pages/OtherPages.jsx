import React, { useEffect, useState, useCallback } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import {
  CheckCircle, XCircle, Package, ShoppingBag,
  CreditCard, Plus, Trash2, Star, X, Lock, AlertCircle,
  Activity, RefreshCw, Loader, Fingerprint, MessageSquare, Mail,
  ShieldCheck, KeyRound, ChevronRight, ExternalLink
} from 'lucide-react'
import { orderService, cardService, logService } from '../services/api'
import { useStore } from '../store'
import { Button, Price, Badge, Spinner, Modal, Input } from '../components/ui'
import { fmtDate, fmtTime, fmtDateTime } from '../utils/date'

// ─── Order Confirmation ────────────────────────────────────────────────────────
export function OrderConfirmationPage() {
  const navigate = useNavigate()
  const { lastOrder } = useStore()

  if (!lastOrder) return (
    <div className="min-h-screen bg-surface-light flex items-center justify-center">
      <Button variant="primary" onClick={() => navigate('/')}>Go Shopping</Button>
    </div>
  )

  return (
    <div className="min-h-screen bg-surface-light flex items-center justify-center p-4">
      <div className="bg-white rounded-lg border border-surface-border shadow-amex-md w-full max-w-md p-8 text-center">
        <div className="w-20 h-20 rounded-full bg-green-50 border-2 border-green-200 flex items-center justify-center mx-auto mb-5">
          <CheckCircle className="w-10 h-10 text-green-600" />
        </div>
        <h1 className="font-bold text-2xl text-ink-primary mb-1">Order Confirmed!</h1>
        <p className="text-ink-secondary text-sm mb-6">Your order has been placed successfully</p>

        <div className="text-left space-y-3 mb-6 bg-surface-light rounded-amex p-4">
          <div className="flex justify-between text-sm">
            <span className="text-ink-secondary">Order ID</span>
            <span className="font-mono text-xs text-ink-primary">{lastOrder.id?.slice(0, 8)}...</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-ink-secondary">Status</span>
            <Badge color="green">{lastOrder.status}</Badge>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-ink-secondary">Payment</span>
            <span className="text-ink-primary font-medium">{lastOrder.cardBrand} •••• {lastOrder.cardLast4}</span>
          </div>
          <div className="flex justify-between items-center pt-2 border-t border-surface-border">
            <span className="font-semibold text-ink-primary">Total</span>
            <Price amount={lastOrder.total} size="md" />
          </div>
        </div>

        <div className="flex gap-2">
          <Button variant="secondary" className="flex-1" onClick={() => navigate('/orders')}>
            <Package className="w-4 h-4" /> Orders
          </Button>
          <Button variant="primary" className="flex-1" onClick={() => navigate('/')}>
            <ShoppingBag className="w-4 h-4" /> Shop More
          </Button>
        </div>
      </div>
    </div>
  )
}

// ─── Orders Page ───────────────────────────────────────────────────────────────
export function OrdersPage() {
  const { userId } = useStore()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    orderService.getOrders(userId)
      .then(res => setOrders(res.data))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [userId])

  const statusColor = s => ({
    CONFIRMED: 'green', SHIPPED: 'blue',
    DELIVERED: 'green', PENDING: 'amber', CANCELLED: 'red'
  }[s] || 'gray')

  return (
    <div className="min-h-screen bg-surface-light">
      <div className="bg-white border-b border-surface-border">
        <div className="max-w-4xl mx-auto px-4 py-6">
          <h1 className="font-bold text-2xl text-ink-primary">Order History</h1>
          <p className="text-sm text-ink-secondary mt-1">Track and manage your purchases</p>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 py-8">
        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="lg" /></div>
        ) : orders.length === 0 ? (
          <div className="bg-white rounded-lg border border-surface-border text-center py-16">
            <Package className="w-16 h-16 text-ink-muted mx-auto mb-4" />
            <h2 className="font-semibold text-lg text-ink-primary mb-1">No orders yet</h2>
            <p className="text-ink-secondary text-sm mb-4">Your order history will appear here</p>
            <Button variant="primary" onClick={() => navigate('/')}>Start Shopping</Button>
          </div>
        ) : (
          <div className="space-y-3">
            {orders.map(order => (
              <div key={order.id} className="bg-white rounded-lg border border-surface-border overflow-hidden">
                <div className="flex items-center justify-between px-5 py-4 border-b border-surface-muted">
                  <div>
                    <p className="text-xs text-ink-muted font-mono">#{order.id?.slice(0, 8)}</p>
                    <p className="text-sm text-ink-secondary">
                      {fmtDate(order.createdAt)}
                    </p>
                  </div>
                  <div className="flex items-center gap-3">
                    <Badge color={statusColor(order.status)}>{order.status}</Badge>
                    <Price amount={order.total} size="sm" />
                  </div>
                </div>
                <div className="px-5 py-3 flex flex-wrap gap-2">
                  {order.items?.slice(0, 4).map((item, i) => (
                    <div key={i} className="flex items-center gap-1.5 bg-surface-light rounded-full px-3 py-1.5 text-xs text-ink-secondary">
                      <span className="font-medium text-ink-primary">{item.productName}</span>
                      <span>×{item.quantity}</span>
                    </div>
                  ))}
                  {(order.items?.length || 0) > 4 && (
                    <span className="text-xs text-ink-muted self-center">
                      +{order.items.length - 4} more
                    </span>
                  )}
                </div>
                {order.cardLast4 && (
                  <div className="px-5 pb-3 flex items-center gap-1.5 text-xs text-ink-muted">
                    <CreditCard className="w-3.5 h-3.5" />
                    Paid with {order.cardBrand} •••• {order.cardLast4}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

// ─── Card Notification Banner ──────────────────────────────────────────────────
function CardNotificationBanner({ notification, onDismiss }) {
  useEffect(() => {
    if (!notification) return
    const t = setTimeout(onDismiss, 5000)
    return () => clearTimeout(t)
  }, [notification, onDismiss])

  if (!notification) return null

  const isSuccess = notification.type === 'success'

  return (
    <div className={`rounded-lg border p-4 mb-6 flex items-start gap-3 relative overflow-hidden
      ${isSuccess ? 'bg-emerald-50 border-emerald-200' : 'bg-red-50 border-red-200'}`}
      style={{ animation: 'fadeSlideUp 0.35s ease-out' }}
    >
      {/* Icon */}
      <div className={`shrink-0 w-9 h-9 rounded-full flex items-center justify-center
        ${isSuccess ? 'bg-emerald-100' : 'bg-red-100'}`}>
        {isSuccess
          ? <CheckCircle className="w-5 h-5 text-emerald-600" />
          : <XCircle className="w-5 h-5 text-red-600" />}
      </div>

      {/* Text */}
      <div className="flex-1 min-w-0 pt-0.5">
        <p className={`font-semibold text-sm ${isSuccess ? 'text-emerald-800' : 'text-red-800'}`}>
          {isSuccess ? 'Card Added Successfully' : 'Failed to Add Card'}
        </p>
        <p className={`text-xs mt-0.5 leading-relaxed ${isSuccess ? 'text-emerald-700' : 'text-red-700'}`}>
          {notification.message}
        </p>
      </div>

      {/* Dismiss */}
      <button
        onClick={onDismiss}
        className={`shrink-0 p-1 rounded transition-colors mt-0.5
          ${isSuccess ? 'text-emerald-500 hover:bg-emerald-100' : 'text-red-500 hover:bg-red-100'}`}
      >
        <X className="w-4 h-4" />
      </button>

      {/* Progress bar — shrinks to zero over 5s */}
      <div
        className={`absolute bottom-0 left-0 h-0.5 ${isSuccess ? 'bg-emerald-400' : 'bg-red-400'}`}
        style={{ animation: 'shrinkBar 5s linear forwards' }}
      />
    </div>
  )
}

// ─── Card Number formatter ─────────────────────────────────────────────────────
// Formats raw digits into "XXXX XXXX XXXX XXXX" groups as the user types
function formatCardNumber(raw) {
  const digits = raw.replace(/\D/g, '').slice(0, 16)
  return digits.replace(/(.{4})/g, '$1 ').trim()
}

function detectBrand(num) {
  const n = num.replace(/\s/g, '')
  if (n.startsWith('4')) return 'Visa'
  if (n.startsWith('5')) return 'Mastercard'
  if (n.startsWith('3')) return 'Amex'
  return 'Card'
}

// ─── Passkey Setup Modal ───────────────────────────────────────────────────────
// Steps: otp-method → otp-enter → passkey → success
const DEMO_OTP = '456789'

function PasskeySetupModal({ card, userId, open, onClose, onActivated }) {
  const [step, setStep]             = useState('otp-method')
  const [otpMethod, setOtpMethod]   = useState(null)
  const [otp, setOtp]               = useState('')
  const [otpError, setOtpError]     = useState('')
  const [creating, setCreating]     = useState(false)
  const [passkeyError, setPasskeyError] = useState('')

  const reset = () => {
    setStep('otp-method')
    setOtpMethod(null)
    setOtp('')
    setOtpError('')
    setCreating(false)
    setPasskeyError('')
  }

  const handleClose = () => { reset(); onClose() }

  const handleVerifyOtp = () => {
    if (otp.trim() !== DEMO_OTP) {
      setOtpError('Invalid OTP. Please try again.')
      return
    }
    setOtpError('')
    setStep('passkey')
  }

  const activateCard = async () => {
    await cardService.activate(userId, card.id)
    onActivated(card.id)
    setStep('success')
  }

  const handleCreatePasskey = async () => {
    setCreating(true)
    setPasskeyError('')
    try {
      const challenge = new Uint8Array(32)
      crypto.getRandomValues(challenge)

      await navigator.credentials.create({
        publicKey: {
          challenge,
          rp: { name: 'AMEX Commerce', id: window.location.hostname },
          user: {
            id: new TextEncoder().encode(userId + ':' + card.id),
            name: userId,
            displayName: card.cardholderName
          },
          pubKeyCredParams: [
            { alg: -7,   type: 'public-key' },
            { alg: -257, type: 'public-key' }
          ],
          // No authenticatorAttachment — let browser pick any available authenticator
          authenticatorSelection: {
            userVerification: 'preferred',
            residentKey: 'preferred'
          },
          timeout: 60000
        }
      })

      await activateCard()
    } catch (err) {
      const msg = err.name === 'NotAllowedError'
        ? 'The browser prompt was dismissed or no authenticator is available.'
        : err.name === 'NotSupportedError'
          ? 'Passkeys are not supported on this device/browser.'
          : err.message || 'Passkey creation failed.'
      setPasskeyError(msg)
    } finally {
      setCreating(false)
    }
  }

  const titles = {
    'otp-method': 'Verify Your Identity',
    'otp-enter':  'Enter Verification Code',
    'passkey':    'Create Passkey',
    'success':    'Passkey Created!'
  }

  return (
    <Modal open={open} onClose={handleClose} title={titles[step]}
      footer={step === 'success'
        ? <Button variant="primary" onClick={handleClose}><CheckCircle className="w-4 h-4" /> Done</Button>
        : null
      }
    >
      {/* ── Step 1: Choose OTP method ── */}
      {step === 'otp-method' && (
        <div className="space-y-4">
          <p className="text-sm text-ink-secondary">
            To set up a passkey for your <strong>{card?.brand} •••• {card?.last4}</strong> card,
            first verify your identity with a one-time code.
          </p>
          <div className="space-y-2">
            {[
              { id: 'sms', icon: MessageSquare, label: 'SMS', desc: 'Send code to your registered mobile number' },
              { id: 'email', icon: Mail, label: 'Email', desc: 'Send code to your registered email address' }
            ].map(m => (
              <button key={m.id} onClick={() => setOtpMethod(m.id)}
                className={`w-full flex items-center gap-3 p-3.5 rounded-lg border text-left transition-all ${
                  otpMethod === m.id
                    ? 'border-amex-blue bg-amex-light'
                    : 'border-surface-border hover:border-amex-blue/40'
                }`}
              >
                <div className={`w-9 h-9 rounded-full flex items-center justify-center shrink-0 ${
                  otpMethod === m.id ? 'bg-amex-blue' : 'bg-surface-light'
                }`}>
                  <m.icon className={`w-4 h-4 ${otpMethod === m.id ? 'text-white' : 'text-ink-secondary'}`} />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-semibold text-ink-primary">{m.label}</p>
                  <p className="text-xs text-ink-secondary">{m.desc}</p>
                </div>
                {otpMethod === m.id && <ChevronRight className="w-4 h-4 text-amex-blue shrink-0" />}
              </button>
            ))}
          </div>
          <Button variant="primary" className="w-full" disabled={!otpMethod}
            onClick={() => setStep('otp-enter')}>
            Continue <ChevronRight className="w-4 h-4" />
          </Button>
        </div>
      )}

      {/* ── Step 2: Enter OTP ── */}
      {step === 'otp-enter' && (
        <div className="space-y-4">
          <div className="flex items-center gap-2 bg-amex-light border border-amex-blue/20 rounded-lg px-3 py-2.5">
            {otpMethod === 'sms'
              ? <MessageSquare className="w-4 h-4 text-amex-blue shrink-0" />
              : <Mail className="w-4 h-4 text-amex-blue shrink-0" />}
            <p className="text-xs text-amex-blue font-medium">
              A 6-digit code has been sent via {otpMethod === 'sms' ? 'SMS' : 'email'}.
            </p>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-xs font-semibold text-ink-secondary uppercase tracking-wide">
              Verification Code
            </label>
            <input
              type="text"
              inputMode="numeric"
              maxLength={6}
              placeholder="000000"
              value={otp}
              onChange={e => { setOtp(e.target.value.replace(/\D/g, '').slice(0, 6)); setOtpError('') }}
              className="w-full text-center text-2xl font-mono tracking-[0.5em] border border-surface-border rounded-lg px-3 py-3 focus:outline-none focus:border-amex-blue focus:ring-2 focus:ring-amex-light"
              autoFocus
            />
            {otpError && (
              <p className="text-xs text-red-600 flex items-center gap-1 mt-1">
                <AlertCircle className="w-3.5 h-3.5" /> {otpError}
              </p>
            )}
          </div>

          <div className="flex gap-2">
            <Button variant="secondary" className="flex-1" onClick={() => setStep('otp-method')}>
              Back
            </Button>
            <Button variant="primary" className="flex-1" disabled={otp.length < 6}
              onClick={handleVerifyOtp}>
              <ShieldCheck className="w-4 h-4" /> Verify
            </Button>
          </div>
        </div>
      )}

      {/* ── Step 3: Create Passkey ── */}
      {step === 'passkey' && (
        <div className="space-y-4">
          <div className="flex flex-col items-center py-4 gap-3">
            <div className="w-16 h-16 rounded-full bg-amex-light border-2 border-amex-blue/30 flex items-center justify-center">
              <Fingerprint className="w-8 h-8 text-amex-blue" />
            </div>
            <div className="text-center">
              <p className="font-semibold text-ink-primary">Use your device to create a passkey</p>
              <p className="text-xs text-ink-secondary mt-1">
                Your device will prompt you to use Face ID, fingerprint, or PIN to create a passkey
                for your <strong>{card?.brand} •••• {card?.last4}</strong> card.
              </p>
            </div>
          </div>

          {passkeyError && (
            <div className="space-y-2">
              <div className="flex items-start gap-2 bg-red-50 border border-red-200 rounded-lg px-3 py-2.5">
                <AlertCircle className="w-4 h-4 text-red-500 shrink-0 mt-0.5" />
                <p className="text-xs text-red-700">{passkeyError}</p>
              </div>
              {/* Sandbox fallback — activates the card without real WebAuthn */}
              <button
                onClick={activateCard}
                className="w-full text-xs font-semibold px-4 py-2 rounded-lg border border-amber-200 text-amber-700 bg-amber-50 hover:bg-amber-100 transition-colors flex items-center justify-center gap-1.5"
              >
                <span className="text-[10px] font-bold uppercase tracking-wider bg-amber-200 text-amber-800 px-1 rounded">Sandbox</span>
                Continue in Demo Mode (skip device auth)
              </button>
            </div>
          )}

          <Button variant="primary" className="w-full" loading={creating}
            onClick={handleCreatePasskey}>
            <KeyRound className="w-4 h-4" /> Create Passkey
          </Button>
        </div>
      )}

      {/* ── Step 4: Success ── */}
      {step === 'success' && (
        <div className="flex flex-col items-center py-6 gap-4 text-center">
          <div className="w-16 h-16 rounded-full bg-green-50 border-2 border-green-200 flex items-center justify-center">
            <CheckCircle className="w-8 h-8 text-green-600" />
          </div>
          <div>
            <p className="font-semibold text-ink-primary text-lg">Passkey Created!</p>
            <p className="text-sm text-ink-secondary mt-1">
              Your <strong>{card?.brand} •••• {card?.last4}</strong> card is now{' '}
              <span className="text-green-600 font-semibold">ACTIVE</span> and secured with a passkey.
            </p>
          </div>
          <div className="flex items-center gap-2 bg-green-50 border border-green-200 rounded-lg px-4 py-2.5 w-full">
            <ShieldCheck className="w-4 h-4 text-green-600 shrink-0" />
            <p className="text-xs text-green-700">
              You can now use this card for checkout with passkey authentication.
            </p>
          </div>
        </div>
      )}
    </Modal>
  )
}

// ─── Cards Page ────────────────────────────────────────────────────────────────
export function CardsPage() {
  const { userId, cards, setCards, addNotification } = useStore()
  const [loading, setLoading] = useState(true)
  const [addingCard, setAddingCard] = useState(false)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')
  const [banner, setBanner] = useState(null)
  const [passkeyCard, setPasskeyCard] = useState(null)

  // Form state — cardNumber is stored formatted ("1234 5678 9012 3456")
  const [form, setForm] = useState({
    cardNumber: '', expiryMonth: '', expiryYear: '', cvv: '', cardholderName: ''
  })

  const handleDismissBanner = useCallback(() => setBanner(null), [])

  useEffect(() => {
    cardService.getCards(userId)
      .then(res => setCards(Array.isArray(res.data) ? res.data : []))
      .catch(() => setCards([]))
      .finally(() => setLoading(false))
  }, [userId])

  // Auto-format card number as user types
  const handleCardNumberChange = (e) => {
    const formatted = formatCardNumber(e.target.value)
    setForm(prev => ({ ...prev, cardNumber: formatted }))
  }

  const validateForm = () => {
    const clean = form.cardNumber.replace(/\s/g, '')
    if (clean.length < 13) return 'Please enter a valid card number (13–16 digits).'
    if (!form.cardholderName.trim()) return 'Cardholder name is required.'
    if (!form.expiryMonth || form.expiryMonth.length !== 2) return 'Enter a 2-digit expiry month (e.g. 09).'
    if (!form.expiryYear || form.expiryYear.length !== 4) return 'Enter a 4-digit expiry year (e.g. 2027).'
    if (!form.cvv || form.cvv.length < 3) return 'Enter a valid CVV (3–4 digits).'
    return ''
  }

  const resetForm = () => {
    setForm({ cardNumber: '', expiryMonth: '', expiryYear: '', cvv: '', cardholderName: '' })
    setFormError('')
  }

  const handleAdd = async () => {
    // Client-side validation — never hits the API if invalid
    const validationError = validateForm()
    if (validationError) {
      setFormError(validationError)
      return
    }
    setFormError('')
    setSaving(true)

    const clean = form.cardNumber.replace(/\s/g, '')
    const last4 = clean.slice(-4)
    const brand = detectBrand(form.cardNumber)

    try {
      const payload = {
        cardNumber: clean,
        expiryMonth: form.expiryMonth,
        expiryYear: form.expiryYear,
        cvv: form.cvv,
        cardholderName: form.cardholderName.trim()
      }

      // Step 1: save the card
      await cardService.addCard(userId, payload)

      // Step 2: re-fetch full card list from backend — gets server-assigned
      // IDs, isDefault flag and exact field values without any page refresh
      try {
        const refreshed = await cardService.getCards(userId)
        setCards(Array.isArray(refreshed.data) ? refreshed.data : [])
      } catch {
        // If refresh fails, list still shows previous state — not critical
      }

      // Step 3: close modal, reset form, show success banner + toast
      setAddingCard(false)
      setForm({ cardNumber: '', expiryMonth: '', expiryYear: '', cvv: '', cardholderName: '' })
      setFormError('')
      setBanner({
        type: 'success',
        message: `${brand} card ending in ${last4} has been saved to your account.`
      })
      addNotification(`${brand} \u2022\u2022\u2022\u2022 ${last4} added successfully`, 'success')

    } catch (apiError) {
      // Keep modal open on error so user can fix and retry
      const msg =
        apiError?.response?.data?.message ||
        apiError?.response?.data?.error ||
        apiError?.message ||
        'Unable to save card. Please check your details and try again.'

      setFormError(msg)
      setBanner({ type: 'error', message: msg })
      addNotification('Failed to add card: ' + msg, 'error')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id, brand, last4) => {
    try {
      await cardService.deleteCard(userId, id)
      setCards(prev => prev.filter(c => c.id !== id))

            // Step 2: re-fetch full card list from backend — gets server-assigned
      // IDs, isDefault flag and exact field values without any page refresh
      try {
        const refreshed = await cardService.getCards(userId)
        setCards(Array.isArray(refreshed.data) ? refreshed.data : [])
      } catch {
        // If refresh fails, list still shows previous state — not critical
      }
      
      addNotification(`${brand} •••• ${last4} removed`, 'info')
    } catch {
      addNotification('Failed to remove card. Please try again.', 'error')
    }
  }

  const handleDefault = async (id, brand, last4) => {
    try {
      await cardService.setDefault(userId, id)
      setCards(prev => prev.map(c => ({ ...c, isDefault: c.id === id })))
      addNotification(`${brand} •••• ${last4} set as default`, 'success')
    } catch {
      addNotification('Failed to update default card.', 'error')
    }
  }

  const brandIcon = b => ({ Visa: '💳', Mastercard: '🔴', Amex: '🔵' }[b] || '💳')

  // Detect brand live from what the user is typing
  const liveBrand = form.cardNumber ? detectBrand(form.cardNumber) : null

  return (
    <div className="min-h-screen bg-surface-light">
      {/* Page header */}
      <div className="bg-white border-b border-surface-border">
        <div className="max-w-2xl mx-auto px-4 py-6 flex items-center justify-between">
          <div>
            <h1 className="font-bold text-2xl text-ink-primary">Payment Cards</h1>
            <p className="text-sm text-ink-secondary mt-1">Manage your saved payment methods</p>
          </div>
          <Button variant="primary" onClick={() => { setAddingCard(true); resetForm() }}>
            <Plus className="w-4 h-4" /> Add Card
          </Button>
        </div>
      </div>

      <div className="max-w-2xl mx-auto px-4 py-8">

        {/* Passkey management link */}
        <div className="flex items-center gap-3 bg-amex-light border border-amex-blue/20 rounded-lg px-4 py-3 mb-5">
          <Fingerprint className="w-4 h-4 text-amex-blue shrink-0" />
          <div className="flex-1 min-w-0">
            <p className="text-xs font-semibold text-amex-blue">Manage your passkeys</p>
            <Link
              to="/passkeys"
              className="text-xs text-amex-blue/70 hover:text-amex-blue hover:underline font-mono truncate block"
            >
              sandbox.auth.amex.com/passkey
            </Link>
          </div>
          <Link
            to="/passkeys"
            className="shrink-0 flex items-center gap-1 text-xs font-semibold text-amex-blue hover:text-amex-dark transition-colors"
          >
            Open <ExternalLink className="w-3 h-3" />
          </Link>
        </div>

        {/* Inline notification banner — appears here after modal closes */}
        <CardNotificationBanner
          notification={banner}
          onDismiss={handleDismissBanner}
        />

        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="lg" /></div>
        ) : cards.length === 0 ? (
          <div className="bg-white rounded-lg border border-surface-border text-center py-16">
            <CreditCard className="w-16 h-16 text-ink-muted mx-auto mb-4" />
            <h2 className="font-semibold text-lg text-ink-primary mb-1">No cards saved</h2>
            <p className="text-ink-secondary text-sm mb-4">Add a payment card to checkout faster</p>
            <Button variant="primary" onClick={() => setAddingCard(true)}>Add Card</Button>
          </div>
        ) : (
          <div className="space-y-3">
            {(Array.isArray(cards) ? cards : []).map(card => (
              <div
                key={card.id}
                className={`bg-white rounded-lg border px-5 py-4 flex items-center gap-4 transition-shadow hover:shadow-amex-sm ${
                  card.status === 'ACTIVE'
                    ? 'border-green-200 bg-green-50/30'
                    : 'border-surface-border'
                }`}
              >
                <span className="text-3xl">{brandIcon(card.brand)}</span>
                <div className="flex-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    <p className="font-semibold text-ink-primary">{card.brand} •••• {card.last4}</p>
                    {card.isDefault && <Badge color="blue">Default</Badge>}
                    {card.status === 'ACTIVE'
                      ? <Badge color="green"><ShieldCheck className="w-3 h-3 inline mr-0.5" />ACTIVE</Badge>
                      : <Badge color="amber">PENDING</Badge>
                    }
                  </div>
                  <p className="text-sm text-ink-secondary">
                    {card.cardholderName} · Expires {card.expiryMonth}/{card.expiryYear}
                  </p>
                </div>
                <div className="flex gap-2 flex-wrap justify-end">
                  {card.status !== 'ACTIVE' && (
                    <Button variant="ghost" size="sm"
                      onClick={() => setPasskeyCard(card)}>
                      <Fingerprint className="w-3.5 h-3.5" /> Set Up Passkey
                    </Button>
                  )}
                  {!card.isDefault && (
                    <Button variant="ghost" size="sm"
                      onClick={() => handleDefault(card.id, card.brand, card.last4)}>
                      <Star className="w-3.5 h-3.5" /> Set Default
                    </Button>
                  )}
                  <Button variant="danger" size="sm"
                    onClick={() => handleDelete(card.id, card.brand, card.last4)}>
                    <Trash2 className="w-3.5 h-3.5" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Passkey Setup Modal */}
      {passkeyCard && (
        <PasskeySetupModal
          card={passkeyCard}
          userId={userId}
          open={!!passkeyCard}
          onClose={() => setPasskeyCard(null)}
          onActivated={async (cardId) => {
            setCards(prev => prev.map(c => c.id === cardId ? { ...c, status: 'ACTIVE' } : c))
            addNotification(`Card activated with passkey`, 'success')
            try {
              const refreshed = await cardService.getCards(userId)
              setCards(Array.isArray(refreshed.data) ? refreshed.data : [])
            } catch {}
          }}
        />
      )}

      {/* Add Card Modal */}
      <Modal
        open={addingCard}
        onClose={() => { setAddingCard(false); resetForm() }}
        title="Add Payment Card"
        footer={
          <>
            <Button variant="secondary" onClick={() => { setAddingCard(false); resetForm() }}>
              Cancel
            </Button>
            <Button variant="primary" loading={saving} onClick={handleAdd}>
              <CreditCard className="w-4 h-4" /> Save Card
            </Button>
          </>
        }
      >
        <div className="space-y-4">

          {/* Security notice */}
          <div className="flex items-center gap-2 bg-amex-light border border-amex-blue/20 rounded-amex px-3 py-2">
            <Lock className="w-3.5 h-3.5 text-amex-blue shrink-0" />
            <p className="text-xs text-amex-blue font-medium">
              Your card details are encrypted and stored securely.
            </p>
          </div>

          {/* Inline form error */}
          {formError && (
            <div className="flex items-start gap-2 bg-red-50 border border-red-200 rounded-amex px-3 py-2.5"
              style={{ animation: 'fadeSlideUp 0.25s ease-out' }}>
              <AlertCircle className="w-4 h-4 text-red-500 shrink-0 mt-0.5" />
              <p className="text-xs text-red-700 font-medium">{formError}</p>
            </div>
          )}

          {/* Card number — with live brand detection + auto-format */}
          <div className="flex flex-col gap-1">
            <label className="text-xs font-semibold text-ink-secondary uppercase tracking-wide">
              Card Number
            </label>
            <div className="relative">
              <input
                type="text"
                inputMode="numeric"
                placeholder="1234 5678 9012 3456"
                value={form.cardNumber}
                onChange={handleCardNumberChange}
                maxLength={19}
                className="w-full bg-white border border-surface-border rounded-amex px-3 py-2.5 pr-16 text-sm text-ink-primary placeholder-ink-muted focus:outline-none focus:border-amex-blue focus:ring-2 focus:ring-amex-light transition-colors font-mono tracking-widest"
              />
              {liveBrand && (
                <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs font-bold text-amex-blue bg-amex-light px-2 py-0.5 rounded">
                  {liveBrand}
                </span>
              )}
            </div>
          </div>

          <Input
            label="Cardholder Name"
            placeholder="John Doe"
            value={form.cardholderName}
            onChange={e => setForm(prev => ({ ...prev, cardholderName: e.target.value }))}
          />

          <div className="grid grid-cols-3 gap-3">
            <Input
              label="Month"
              placeholder="09"
              value={form.expiryMonth}
              onChange={e => setForm(prev => ({ ...prev, expiryMonth: e.target.value.replace(/\D/g, '').slice(0, 2) }))}
              maxLength={2}
              inputMode="numeric"
            />
            <Input
              label="Year"
              placeholder="2027"
              value={form.expiryYear}
              onChange={e => setForm(prev => ({ ...prev, expiryYear: e.target.value.replace(/\D/g, '').slice(0, 4) }))}
              maxLength={4}
              inputMode="numeric"
            />
            <Input
              label="CVV"
              placeholder="•••"
              value={form.cvv}
              onChange={e => setForm(prev => ({ ...prev, cvv: e.target.value.replace(/\D/g, '').slice(0, 4) }))}
              maxLength={4}
              type="password"
              inputMode="numeric"
            />
          </div>
        </div>
      </Modal>
    </div>
  )
}

// ─── Flow Logs Page ────────────────────────────────────────────────────────────
const STEP_COLORS = {
  1: 'bg-blue-100 text-blue-700 border-blue-200',
  2: 'bg-purple-100 text-purple-700 border-purple-200',
  3: 'bg-amber-100 text-amber-700 border-amber-200',
  4: 'bg-green-100 text-green-700 border-green-200',
}
const STEP_BAR = { 1: 'bg-blue-400', 2: 'bg-purple-400', 3: 'bg-amber-400', 4: 'bg-green-500' }

function LogStepRow({ log }) {
  return (
    <div className="flex items-start gap-2 py-2 border-b border-surface-muted last:border-0">
      <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded border shrink-0 ${STEP_COLORS[log.stepNumber] || 'bg-gray-100 text-gray-600 border-gray-200'}`}>
        STEP {log.stepNumber}
      </span>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-1.5">
          {log.status === 'COMPLETED'
            ? <CheckCircle className="w-3.5 h-3.5 text-green-500 shrink-0" />
            : log.status === 'ERROR'
              ? <XCircle className="w-3.5 h-3.5 text-red-500 shrink-0" />
              : <Loader className="w-3.5 h-3.5 text-blue-400 animate-spin shrink-0" />}
          <span className="text-xs font-semibold text-ink-primary">{log.stepLabel}</span>
          {log.durationMs != null && log.durationMs > 0 && (
            <span className="text-[10px] text-ink-muted ml-auto shrink-0">{log.durationMs}ms</span>
          )}
        </div>
        {log.detail && (
          <p className="text-[11px] text-ink-secondary mt-0.5 leading-relaxed break-words">{log.detail}</p>
        )}
        <p className="text-[10px] text-ink-muted mt-0.5">
          {fmtTime(log.createdAt)}
        </p>
      </div>
    </div>
  )
}

function LogRequestBlock({ requestId, logs, onDelete }) {
  const hasError = logs.some(l => l.status === 'ERROR')
  const allDone = logs.every(l => l.status !== 'IN_PROGRESS')
  const firstLog = logs[0]
  const [deleting, setDeleting] = useState(false)

  const handleDelete = async () => {
    if (!firstLog?.sessionId) return
    setDeleting(true)
    try {
      await logService.deleteBySession(firstLog.sessionId)
      onDelete(firstLog.sessionId)
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="bg-white rounded-lg border border-surface-border overflow-hidden">
      <div className="h-1.5 flex gap-px">
        {[1, 2, 3, 4].map(n => {
          const step = logs.find(l => l.stepNumber === n)
          return (
            <div key={n} className={`flex-1 transition-all ${
              step ? (step.status === 'ERROR' ? 'bg-red-400' : STEP_BAR[n]) : 'bg-surface-muted'
            }`} />
          )
        })}
      </div>
      <div className="px-5 py-3 border-b border-surface-muted flex items-center gap-3">
        <Activity className="w-4 h-4 text-amex-blue shrink-0" />
        <div className="flex-1 min-w-0">
          <p className="text-xs font-mono text-ink-muted truncate">req: {requestId}</p>
          {firstLog && (
            <p className="text-[10px] text-ink-muted">
              {fmtDateTime(firstLog.createdAt)}
              {firstLog.sessionId && <> · session: {firstLog.sessionId.slice(0, 8)}…</>}
            </p>
          )}
        </div>
        <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${
          hasError ? 'bg-red-50 text-red-600 border-red-200'
            : allDone ? 'bg-green-50 text-green-600 border-green-200'
              : 'bg-blue-50 text-blue-600 border-blue-200'
        }`}>
          {hasError ? 'ERROR' : allDone ? 'DONE' : 'RUNNING'}
        </span>
        <button
          onClick={handleDelete}
          disabled={deleting}
          className="p-1.5 rounded text-ink-muted hover:text-red-500 hover:bg-red-50 transition-colors disabled:opacity-40"
          title="Delete session logs"
        >
          {deleting
            ? <Loader className="w-3.5 h-3.5 animate-spin" />
            : <Trash2 className="w-3.5 h-3.5" />}
        </button>
      </div>
      <div className="px-5 py-1">
        {logs.map(l => <LogStepRow key={l.id} log={l} />)}
      </div>
    </div>
  )
}

export function FlowLogsPage() {
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [clearingAll, setClearingAll] = useState(false)

  const fetchLogs = async (showSpinner = false) => {
    if (showSpinner) setRefreshing(true)
    try {
      const res = await logService.getAll()
      setLogs(res.data || [])
    } catch {
      // ignore
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }

  useEffect(() => { fetchLogs() }, [])

  const handleDelete = (sessionId) => {
    setLogs(prev => prev.filter(l => l.sessionId !== sessionId))
  }

  const handleClearAll = async () => {
    setClearingAll(true)
    try {
      await logService.deleteAll()
      setLogs([])
    } finally {
      setClearingAll(false)
    }
  }

  const groups = logs.reduce((acc, l) => {
    if (!acc[l.requestId]) acc[l.requestId] = []
    acc[l.requestId].push(l)
    return acc
  }, {})
  const requestIds = Object.keys(groups)

  return (
    <div className="min-h-screen bg-surface-light">
      <div className="bg-white border-b border-surface-border">
        <div className="max-w-4xl mx-auto px-4 py-6 flex items-center justify-between">
          <div>
            <h1 className="font-bold text-2xl text-ink-primary">Flow Logs</h1>
            <p className="text-sm text-ink-secondary mt-1">
              Agent request flow · Chat → LLM → MCP → Merchant API
            </p>
          </div>
          <div className="flex items-center gap-2">
            <Button variant="secondary" onClick={() => fetchLogs(true)} disabled={refreshing}>
              <RefreshCw className={`w-4 h-4 ${refreshing ? 'animate-spin' : ''}`} />
              Refresh
            </Button>
            {logs.length > 0 && (
              <Button variant="danger" onClick={handleClearAll} disabled={clearingAll}>
                {clearingAll
                  ? <Loader className="w-4 h-4 animate-spin" />
                  : <Trash2 className="w-4 h-4" />}
                Clear All
              </Button>
            )}
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 py-8">
        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="lg" /></div>
        ) : requestIds.length === 0 ? (
          <div className="bg-white rounded-lg border border-surface-border text-center py-16">
            <Activity className="w-16 h-16 text-ink-muted mx-auto mb-4" />
            <h2 className="font-semibold text-lg text-ink-primary mb-1">No logs yet</h2>
            <p className="text-ink-secondary text-sm">Send a message to the AI Agent to see flow logs here.</p>
          </div>
        ) : (
          <div className="space-y-3">
            {requestIds.map(rid => (
              <LogRequestBlock key={rid} requestId={rid} logs={groups[rid]} onDelete={handleDelete} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
