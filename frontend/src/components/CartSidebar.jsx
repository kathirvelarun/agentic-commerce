import React from 'react'
import { X, ShoppingCart, Trash2, Minus, Plus, ArrowRight } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useStore } from '../store'
import { cartService } from '../services/api'
import { Button, Price } from './ui'

export default function CartSidebar() {
  const { cart, cartOpen, setCartOpen, userId, setCart, addNotification } = useStore()
  const navigate = useNavigate()
  const [updating, setUpdating] = React.useState(null)

  const handleQtyChange = async (itemId, newQty) => {
    setUpdating(itemId)
    try {
      const res = await cartService.updateItem(userId, itemId, newQty)
      setCart(res.data)
    } catch { addNotification('Failed to update cart', 'error') }
    finally { setUpdating(null) }
  }

  const handleRemove = async (itemId) => {
    setUpdating(itemId)
    try {
      const res = await cartService.removeItem(userId, itemId)
      setCart(res.data)
      addNotification('Item removed from cart')
    } catch { addNotification('Failed to remove item', 'error') }
    finally { setUpdating(null) }
  }

  if (!cartOpen) return null

  return (
    <>
      <div className="fixed inset-0 z-40 bg-black/20" onClick={() => setCartOpen(false)} />
      <div className="fixed right-0 top-0 bottom-0 z-50 w-full max-w-sm bg-white shadow-amex-lg flex flex-col border-l border-surface-border">

        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-surface-muted">
          <div className="flex items-center gap-2">
            <ShoppingCart className="w-5 h-5 text-amex-blue" />
            <span className="font-semibold text-ink-primary">Your Cart</span>
            {cart?.items?.length > 0 && (
              <span className="amex-badge bg-amex-light text-amex-blue">{cart.items.length}</span>
            )}
          </div>
          <button onClick={() => setCartOpen(false)} className="p-1.5 rounded hover:bg-surface-light text-ink-muted transition-colors">
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Items */}
        <div className="flex-1 overflow-y-auto">
          {!cart || cart.items?.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full text-center p-8">
              <div className="w-16 h-16 rounded-full bg-surface-light flex items-center justify-center mb-4">
                <ShoppingCart className="w-8 h-8 text-ink-muted" />
              </div>
              <p className="font-semibold text-ink-primary mb-1">Your cart is empty</p>
              <p className="text-sm text-ink-secondary mb-4">Add products to get started</p>
              <Button variant="outline" size="sm" onClick={() => setCartOpen(false)}>Browse Products</Button>
            </div>
          ) : (
            <div className="divide-y divide-surface-muted">
              {cart.items.map(item => (
                <div key={item.id} className={`flex gap-3 p-4 transition-opacity ${updating === item.id ? 'opacity-40' : ''}`}>
                  <div className="w-16 h-16 rounded border border-surface-border overflow-hidden shrink-0 bg-surface-light">
                    <img src={item.imageUrl} alt={item.productName} className="w-full h-full object-cover"
                      onError={e => { e.target.src = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=100' }} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-ink-primary leading-snug mb-1 line-clamp-2">{item.productName}</p>
                    <Price amount={item.price} size="sm" />
                    <div className="flex items-center justify-between mt-2">
                      <div className="flex items-center border border-surface-border rounded-amex overflow-hidden">
                        <button onClick={() => handleQtyChange(item.id, item.quantity - 1)}
                          disabled={item.quantity <= 1 || updating === item.id}
                          className="px-2 py-1 text-ink-secondary hover:bg-surface-light disabled:opacity-40 transition-colors">
                          <Minus className="w-3 h-3" />
                        </button>
                        <span className="px-3 text-sm font-semibold text-ink-primary border-x border-surface-border">{item.quantity}</span>
                        <button onClick={() => handleQtyChange(item.id, item.quantity + 1)}
                          disabled={updating === item.id}
                          className="px-2 py-1 text-ink-secondary hover:bg-surface-light transition-colors">
                          <Plus className="w-3 h-3" />
                        </button>
                      </div>
                      <div className="flex items-center gap-3">
                        <span className="text-sm font-semibold text-ink-primary">${(item.price * item.quantity).toFixed(2)}</span>
                        <button onClick={() => handleRemove(item.id)} className="text-ink-muted hover:text-red-600 transition-colors">
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        {cart?.items?.length > 0 && (
          <div className="border-t border-surface-border p-5 bg-surface-light space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-sm text-ink-secondary font-medium">Total</span>
              <Price amount={cart.total} size="lg" />
            </div>
            <Button variant="primary" size="lg" className="w-full" onClick={() => { setCartOpen(false); navigate('/checkout') }}>
              Proceed to Checkout <ArrowRight className="w-4 h-4" />
            </Button>
            <p className="text-xs text-center text-ink-muted">Secure checkout · 256-bit encryption</p>
          </div>
        )}
      </div>
    </>
  )
}
