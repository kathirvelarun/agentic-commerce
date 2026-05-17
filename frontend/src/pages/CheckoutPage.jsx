import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { CreditCard, Plus, Check, Lock, ArrowLeft, ShoppingBag } from 'lucide-react'
import { cardService, orderService } from '../services/api'
import { useStore } from '../store'
import { Button, Card, Price, Badge, Modal, Input } from '../components/ui'

export default function CheckoutPage() {
  const navigate = useNavigate()
  const { userId, cart, setCart, cards, setCards, setLastOrder, addNotification } = useStore()
  const [selectedCard, setSelectedCard] = useState(null)
  const [addingCard, setAddingCard] = useState(false)
  const [processing, setProcessing] = useState(false)
  const [form, setForm] = useState({ cardNumber: '', expiryMonth: '', expiryYear: '', cvv: '', cardholderName: '' })
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    cardService.getCards(userId).then(res => {
      setCards(res.data)
      const def = res.data.find(c => c.isDefault)
      if (def) setSelectedCard(def)
    }).catch(() => {})
  }, [])

  const handleAddCard = async () => {
    setSaving(true)
    try {
      const res = await cardService.addCard(userId, form)
      setCards(prev => [...prev, res.data])
      setSelectedCard(res.data)
      addNotification('Card added successfully', 'success')
      setAddingCard(false)
      setForm({ cardNumber: '', expiryMonth: '', expiryYear: '', cvv: '', cardholderName: '' })
    } catch { addNotification('Failed to add card', 'error') }
    finally { setSaving(false) }
  }

  const handleCheckout = async () => {
    if (!selectedCard) return addNotification('Please select a payment card', 'error')
    if (!cart?.id || !cart?.items?.length) return addNotification('Your cart is empty', 'error')
    setProcessing(true)
    try {
      const res = await orderService.checkout(userId, cart.id, selectedCard.last4, selectedCard.brand)
      setLastOrder(res.data)
      setCart(null)
      navigate('/order-confirmation')
    } catch { addNotification('Checkout failed. Please try again.', 'error') }
    finally { setProcessing(false) }
  }

  const brandIcon = brand => ({ Visa: '💳', Mastercard: '🔴', Amex: '🔵' }[brand] || '💳')

  if (!cart || cart.items?.length === 0) return (
    <div className="min-h-screen bg-surface-light flex items-center justify-center">
      <div className="text-center">
        <ShoppingBag className="w-16 h-16 text-ink-muted mx-auto mb-4" />
        <h2 className="font-bold text-xl text-ink-primary mb-2">Your cart is empty</h2>
        <Button variant="primary" onClick={() => navigate('/')}>Start Shopping</Button>
      </div>
    </div>
  )

  return (
    <div className="min-h-screen bg-surface-light">
      {/* Header */}
      <div className="bg-white border-b border-surface-border">
        <div className="max-w-5xl mx-auto px-4 py-4">
          <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-amex-blue text-sm font-medium hover:underline mb-1">
            <ArrowLeft className="w-4 h-4" /> Back
          </button>
          <h1 className="font-bold text-2xl text-ink-primary">Secure Checkout</h1>
        </div>
      </div>

      <div className="max-w-5xl mx-auto px-4 py-8 grid lg:grid-cols-2 gap-6">

        {/* Left — Payment */}
        <div className="space-y-4">
          <div className="bg-white rounded-lg border border-surface-border overflow-hidden">
            <div className="flex items-center justify-between px-5 py-4 border-b border-surface-muted">
              <div className="flex items-center gap-2">
                <CreditCard className="w-5 h-5 text-amex-blue" />
                <span className="font-semibold text-ink-primary">Payment Method</span>
              </div>
              <Button variant="outline" size="sm" onClick={() => setAddingCard(true)}>
                <Plus className="w-3.5 h-3.5" /> Add Card
              </Button>
            </div>

            <div className="p-4">
              {cards.length === 0 ? (
                <div className="text-center py-8">
                  <CreditCard className="w-10 h-10 text-ink-muted mx-auto mb-2" />
                  <p className="text-sm text-ink-secondary mb-3">No payment cards saved</p>
                  <Button variant="outline" size="sm" onClick={() => setAddingCard(true)}>Add Your First Card</Button>
                </div>
              ) : (
                <div className="space-y-2">
                  {cards.map(card => (
                    <button key={card.id} onClick={() => setSelectedCard(card)}
                      className={`w-full flex items-center gap-3 p-3 rounded-amex border-2 transition-all text-left ${
                        selectedCard?.id === card.id
                          ? 'border-amex-blue bg-amex-light'
                          : 'border-surface-border hover:border-surface-border bg-white hover:bg-surface-light'
                      }`}>
                      <span className="text-2xl">{brandIcon(card.brand)}</span>
                      <div className="flex-1">
                        <p className="text-sm font-semibold text-ink-primary">{card.brand} •••• {card.last4}</p>
                        <p className="text-xs text-ink-secondary">{card.cardholderName} · {card.expiryMonth}/{card.expiryYear}</p>
                      </div>
                      {selectedCard?.id === card.id && <Check className="w-4 h-4 text-amex-blue" />}
                      {card.isDefault && <Badge color="green">Default</Badge>}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div className="flex items-center gap-2 px-1 text-xs text-ink-muted">
            <Lock className="w-3.5 h-3.5 text-green-600" />
            Your payment information is protected by 256-bit SSL encryption
          </div>
        </div>

        {/* Right — Order summary */}
        <div>
          <div className="bg-white rounded-lg border border-surface-border overflow-hidden sticky top-24">
            <div className="px-5 py-4 border-b border-surface-muted bg-surface-light">
              <h2 className="font-semibold text-ink-primary">Order Summary</h2>
            </div>
            <div className="divide-y divide-surface-muted max-h-64 overflow-y-auto">
              {cart.items?.map(item => (
                <div key={item.id} className="flex items-center gap-3 px-5 py-3">
                  <div className="w-12 h-12 rounded border border-surface-border overflow-hidden shrink-0 bg-surface-light">
                    <img src={item.imageUrl} alt={item.productName} className="w-full h-full object-cover"
                      onError={e => { e.target.src = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=100' }} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-ink-primary font-medium truncate">{item.productName}</p>
                    <p className="text-xs text-ink-secondary">Qty: {item.quantity}</p>
                  </div>
                  <span className="text-sm font-semibold text-ink-primary">${(item.price * item.quantity).toFixed(2)}</span>
                </div>
              ))}
            </div>

            <div className="px-5 py-4 space-y-2 border-t border-surface-border bg-surface-light">
              <div className="flex justify-between text-sm text-ink-secondary">
                <span>Subtotal</span>
                <span className="text-ink-primary font-medium">${Number(cart.total).toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-sm text-ink-secondary">
                <span>Shipping</span>
                <span className="text-green-600 font-medium">Free</span>
              </div>
              <div className="flex justify-between text-base font-bold text-ink-primary pt-2 border-t border-surface-border">
                <span>Total</span>
                <Price amount={cart.total} size="lg" />
              </div>

              <Button variant="primary" size="lg" className="w-full mt-2" loading={processing}
                disabled={!selectedCard} onClick={handleCheckout}>
                <Lock className="w-4 h-4" />
                {processing ? 'Processing...' : `Pay $${Number(cart.total).toFixed(2)}`}
              </Button>
              {!selectedCard && <p className="text-xs text-amber-600 text-center">Please select a payment method</p>}
            </div>
          </div>
        </div>
      </div>

      {/* Add card modal */}
      <Modal open={addingCard} onClose={() => setAddingCard(false)} title="Add Payment Card"
        footer={
          <>
            <Button variant="secondary" onClick={() => setAddingCard(false)}>Cancel</Button>
            <Button variant="primary" loading={saving} onClick={handleAddCard}>Save Card</Button>
          </>
        }>
        <div className="space-y-4">
          <Input label="Card Number" placeholder="4242 4242 4242 4242"
            value={form.cardNumber} onChange={e => setForm({ ...form, cardNumber: e.target.value })} maxLength={19} />
          <Input label="Cardholder Name" placeholder="John Doe"
            value={form.cardholderName} onChange={e => setForm({ ...form, cardholderName: e.target.value })} />
          <div className="grid grid-cols-3 gap-3">
            <Input label="Month" placeholder="MM" value={form.expiryMonth}
              onChange={e => setForm({ ...form, expiryMonth: e.target.value })} maxLength={2} />
            <Input label="Year" placeholder="YYYY" value={form.expiryYear}
              onChange={e => setForm({ ...form, expiryYear: e.target.value })} maxLength={4} />
            <Input label="CVV" placeholder="•••" value={form.cvv}
              onChange={e => setForm({ ...form, cvv: e.target.value })} maxLength={4} type="password" />
          </div>
        </div>
      </Modal>
    </div>
  )
}
