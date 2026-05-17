import React, { useState, useRef, useEffect } from 'react'
import {
  Bot, Send, User, Zap, Trash2, ShoppingCart, Star, Package,
  Plus, Minus, CreditCard, CheckCircle
} from 'lucide-react'
import { agentService, cartService, cardService, orderService } from '../services/api'
import { useStore } from '../store'
import { Button } from '../components/ui'
import { fmtTime } from '../utils/date'

const SUGGESTIONS = [
  "Show me the best headphones under $300",
  "I need a gift for a fitness enthusiast",
  "What's in my cart?",
  "Find me a laptop for programming",
  "Show me kitchen appliances",
  "Checkout with my saved card",
]

const MCP_TOOLS = ['search_products', 'add_to_cart', 'get_cart', 'checkout', 'get_orders']

// ── Inline Product Card ───────────────────────────────────────────────────────
// phase: idle → adding → ready → checkout → confirmed
function ChatProductCard({ product, onOrderConfirmed }) {
  const { userId, cards: storeCards, setCart, addNotification } = useStore()
  const [phase, setPhase]               = useState('idle')
  const [qty, setQty]                   = useState(1)
  const [cartId, setCartId]             = useState(null)
  const [itemId, setItemId]             = useState(null)
  const [availableCards, setAvailableCards] = useState([])
  const [selectedCard, setSelectedCard] = useState(null)
  const [order, setOrder]               = useState(null)

  const handleAdd = async () => {
    setPhase('adding')
    try {
      const res  = await cartService.addItem(userId, product.id, 1)
      const cart = res.data
      setCart(cart)
      setCartId(cart.id)
      const item = cart.items?.find(i => i.productId === product.id)
      setItemId(item?.id || null)

      // resolve payment cards — store first, then API
      let cards = Array.isArray(storeCards) && storeCards.length > 0 ? storeCards : []
      if (cards.length === 0) {
        try { cards = (await cardService.getCards(userId)).data || [] } catch {}
      }
      setAvailableCards(cards)
      setSelectedCard(cards.find(c => c.isDefault) || cards[0] || null)
      setPhase('ready')
    } catch {
      addNotification('Failed to add to cart', 'error')
      setPhase('idle')
    }
  }

  const handleQty = async (delta) => {
    const newQty = Math.max(1, qty + delta)
    setQty(newQty)
    if (itemId) {
      try {
        const res = await cartService.updateItem(userId, itemId, newQty)
        setCart(res.data)
      } catch {}
    }
  }

  const handleCheckout = async () => {
    if (!selectedCard) {
      addNotification('Add a payment card in the Cards page first', 'error')
      return
    }
    setPhase('checkout')
    try {
      const res   = await orderService.checkout(userId, cartId, selectedCard.last4, selectedCard.brand)
      const orderData = res.data
      setOrder(orderData)
      setCart(null)
      setPhase('confirmed')
      onOrderConfirmed(orderData, product, qty)
    } catch {
      addNotification('Checkout failed. Please try again.', 'error')
      setPhase('ready')
    }
  }

  // ── Confirmed state — compact success card ───────────────────────────────
  if (phase === 'confirmed' && order) {
    return (
      <div className="bg-green-50 border border-green-200 rounded-lg p-3 flex flex-col gap-2">
        <div className="flex items-center gap-1.5">
          <CheckCircle className="w-4 h-4 text-green-600 shrink-0" />
          <span className="text-xs font-bold text-green-700">Order Confirmed!</span>
          <span className="text-[10px] font-mono text-green-500 ml-auto">#{order.id?.slice(0, 8)}</span>
        </div>
        <p className="text-xs text-green-800 font-medium line-clamp-1">{product.name} × {qty}</p>
        <div className="flex items-center justify-between pt-1.5 border-t border-green-200">
          <span className="text-[10px] text-green-600 flex items-center gap-1">
            <CreditCard className="w-3 h-3" />
            {order.cardBrand} •••• {order.cardLast4}
          </span>
          <span className="text-xs font-bold text-green-700">${Number(order.total).toFixed(2)}</span>
        </div>
      </div>
    )
  }

  return (
    <div className={`bg-white border rounded-lg overflow-hidden shadow-amex-sm transition-all duration-200 flex flex-col ${
      phase === 'ready' || phase === 'checkout'
        ? 'border-amex-blue shadow-amex-md'
        : 'border-surface-border hover:border-amex-blue hover:shadow-amex-md'
    }`}>
      {/* Image */}
      <div className="relative h-32 bg-surface-light overflow-hidden shrink-0">
        <img
          src={product.imageUrl}
          alt={product.name}
          className="w-full h-full object-cover transition-transform duration-300 hover:scale-105"
          onError={e => { e.target.src = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=300' }}
        />
        <div className="absolute top-2 left-2">
          <span className="text-[9px] font-bold uppercase tracking-wider bg-amex-light text-amex-blue px-2 py-0.5 rounded-full">
            {product.category}
          </span>
        </div>
        {product.stock <= 5 && (
          <div className="absolute top-2 right-2">
            <span className="text-[9px] font-bold uppercase tracking-wider bg-amber-50 text-amber-700 px-2 py-0.5 rounded-full border border-amber-200">
              Low Stock
            </span>
          </div>
        )}
      </div>

      {/* Body */}
      <div className="p-3 flex flex-col gap-1.5 flex-1">
        {product.brand && (
          <p className="text-[9px] font-bold text-amex-blue uppercase tracking-widest">{product.brand}</p>
        )}
        <h4 className="text-xs font-semibold text-ink-primary leading-snug line-clamp-2 flex-1">
          {product.name}
        </h4>

        {/* Stars */}
        <div className="flex items-center gap-1">
          {[...Array(5)].map((_, i) => (
            <Star key={i} className={`w-2.5 h-2.5 ${i < Math.floor(product.rating) ? 'text-amber-400 fill-current' : 'text-surface-border'}`} />
          ))}
          <span className="text-[10px] text-ink-muted ml-0.5">{product.rating?.toFixed(1)}</span>
          <span className="text-[10px] text-ink-muted ml-auto">{product.stock} left</span>
        </div>

        {/* Price + Add/Qty row */}
        <div className="flex items-center justify-between pt-1.5 border-t border-surface-muted mt-auto">
          <span className="text-sm font-bold text-amex-blue">${Number(product.price).toFixed(2)}</span>

          {phase === 'idle' && (
            <button
              onClick={handleAdd}
              disabled={product.stock === 0}
              className="flex items-center gap-1 text-[11px] font-semibold px-2.5 py-1.5 rounded bg-amex-blue hover:bg-amex-dark text-white disabled:opacity-50 disabled:cursor-not-allowed transition-all"
            >
              <ShoppingCart className="w-3 h-3" /> Add to Cart
            </button>
          )}

          {phase === 'adding' && (
            <span className="flex items-center gap-1 text-[11px] text-ink-muted px-1">
              <span className="w-3 h-3 border-2 border-amex-blue/30 border-t-amex-blue rounded-full animate-spin" />
              Adding…
            </span>
          )}

          {(phase === 'ready' || phase === 'checkout') && (
            <div className="flex items-center gap-1 bg-surface-light border border-surface-border rounded px-1">
              <button onClick={() => handleQty(-1)} disabled={qty <= 1}
                className="w-5 h-5 flex items-center justify-center text-ink-secondary hover:text-amex-blue disabled:opacity-30 transition-colors">
                <Minus className="w-3 h-3" />
              </button>
              <span className="text-xs font-bold text-ink-primary w-4 text-center">{qty}</span>
              <button onClick={() => handleQty(1)}
                className="w-5 h-5 flex items-center justify-center text-ink-secondary hover:text-amex-blue transition-colors">
                <Plus className="w-3 h-3" />
              </button>
            </div>
          )}
        </div>

        {/* ── Checkout section (ready / checkout phases) ── */}
        {(phase === 'ready' || phase === 'checkout') && (
          <div className="flex flex-col gap-2 pt-2 border-t border-amex-blue/20">
            {/* Card selector */}
            {availableCards.length > 0 ? (
              <select
                value={selectedCard?.id || ''}
                onChange={e => setSelectedCard(availableCards.find(c => c.id === e.target.value))}
                disabled={phase === 'checkout'}
                className="w-full text-[11px] border border-surface-border rounded px-2 py-1.5 bg-white text-ink-primary focus:outline-none focus:border-amex-blue disabled:opacity-60"
              >
                {availableCards.map(c => (
                  <option key={c.id} value={c.id}>
                    {c.brand} •••• {c.last4}{c.isDefault ? ' (Default)' : ''}
                  </option>
                ))}
              </select>
            ) : (
              <p className="text-[10px] text-red-500 flex items-center gap-1">
                <CreditCard className="w-3 h-3" /> No card saved — add one in Cards tab
              </p>
            )}

            {/* Subtotal */}
            <div className="flex items-center justify-between text-[11px]">
              <span className="text-ink-muted">Subtotal ({qty} item{qty > 1 ? 's' : ''})</span>
              <span className="font-bold text-ink-primary">${(Number(product.price) * qty).toFixed(2)}</span>
            </div>

            {/* Proceed to Checkout */}
            <button
              onClick={handleCheckout}
              disabled={phase === 'checkout' || !selectedCard}
              className="w-full flex items-center justify-center gap-1.5 text-xs font-bold py-2 rounded bg-amex-blue hover:bg-amex-dark text-white disabled:opacity-60 disabled:cursor-not-allowed transition-all"
            >
              {phase === 'checkout' ? (
                <><span className="w-3 h-3 border-2 border-white/40 border-t-white rounded-full animate-spin" /> Processing…</>
              ) : (
                <><CreditCard className="w-3.5 h-3.5" /> Proceed to Checkout</>
              )}
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

// ── Product Grid ──────────────────────────────────────────────────────────────
function ChatProductGrid({ products, onOrderConfirmed }) {
  return (
    <div className="mt-3 w-full">
      <div className="flex items-center gap-1.5 mb-2">
        <Package className="w-3.5 h-3.5 text-amex-blue" />
        <span className="text-xs font-semibold text-amex-blue uppercase tracking-wide">
          {products.length} Product{products.length !== 1 ? 's' : ''} Found
        </span>
      </div>
      <div className="grid grid-cols-2 gap-2">
        {products.map(p => (
          <ChatProductCard key={p.id} product={p} onOrderConfirmed={onOrderConfirmed} />
        ))}
      </div>
    </div>
  )
}

// ── Order Confirmation message injected into chat ─────────────────────────────
function OrderConfirmationBubble({ order, product, qty }) {
  return (
    <div className="bg-green-50 border border-green-200 rounded-lg p-4 mt-2">
      <div className="flex items-center gap-2 mb-3">
        <div className="w-8 h-8 rounded-full bg-green-100 flex items-center justify-center">
          <CheckCircle className="w-4 h-4 text-green-600" />
        </div>
        <div>
          <p className="text-sm font-bold text-green-700">Order Confirmed!</p>
          <p className="text-[10px] font-mono text-green-500">#{order.id?.slice(0, 8)}</p>
        </div>
        <span className="ml-auto text-[10px] font-bold px-2 py-0.5 bg-green-100 text-green-700 rounded-full border border-green-300">
          {order.status}
        </span>
      </div>
      <div className="space-y-1.5 text-xs">
        <div className="flex justify-between">
          <span className="text-green-600 truncate pr-2">{product?.name || 'Product'} × {qty}</span>
          <span className="font-semibold text-green-800 shrink-0">${(Number(product?.price) * qty).toFixed(2)}</span>
        </div>
        <div className="flex items-center justify-between border-t border-green-200 pt-1.5">
          <span className="flex items-center gap-1 text-green-600 text-[11px]">
            <CreditCard className="w-3 h-3" />
            {order.cardBrand} •••• {order.cardLast4}
          </span>
          <span className="font-bold text-green-800">Total: ${Number(order.total).toFixed(2)}</span>
        </div>
      </div>
      <p className="text-[10px] text-green-600 mt-2">
        Your order has been placed. Track it in the Orders page.
      </p>
    </div>
  )
}

// ── Single chat message ───────────────────────────────────────────────────────
function Message({ msg, onOrderConfirmed }) {
  const isUser = msg.role === 'user'
  return (
    <div className={`flex gap-3 animate-in ${isUser ? 'flex-row-reverse' : 'flex-row'}`}>
      <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 shadow-amex-sm ${
        isUser ? 'bg-amex-blue' : 'bg-amex-navy'
      }`}>
        {isUser ? <User className="w-4 h-4 text-white" /> : <Bot className="w-4 h-4 text-white" />}
      </div>

      <div className={`${msg.products?.length || msg.orderConfirmation ? 'w-full max-w-[92%]' : 'max-w-[78%]'}`}>
        <div className={`px-4 py-3 ${isUser ? 'bubble-user' : 'bubble-ai'}`}>
          <p className="text-sm leading-relaxed whitespace-pre-wrap">{msg.content}</p>

          {/* Inline product grid */}
          {!isUser && msg.products?.length > 0 && (
            <ChatProductGrid products={msg.products} onOrderConfirmed={onOrderConfirmed} />
          )}

          {/* Inline order confirmation */}
          {!isUser && msg.orderConfirmation && (
            <OrderConfirmationBubble
              order={msg.orderConfirmation}
              product={msg.orderProduct}
              qty={msg.orderQty}
            />
          )}

          <p className={`text-xs mt-2 ${isUser ? 'text-white/50' : 'text-ink-muted'}`}>
            {fmtTime(msg.timestamp)}
          </p>
        </div>
      </div>
    </div>
  )
}

// ── Typing indicator ──────────────────────────────────────────────────────────
function TypingIndicator() {
  return (
    <div className="flex gap-3">
      <div className="w-8 h-8 rounded-full bg-amex-navy flex items-center justify-center shrink-0">
        <Bot className="w-4 h-4 text-white" />
      </div>
      <div className="bubble-ai px-4 py-3 flex items-center gap-1.5">
        {[0, 1, 2].map(i => (
          <div key={i} className="w-2 h-2 bg-amex-blue rounded-full animate-bounce"
            style={{ animationDelay: `${i * 0.15}s` }} />
        ))}
      </div>
    </div>
  )
}

// ── Main Agent Page ───────────────────────────────────────────────────────────
export default function AgentPage() {
  const { sessionId, setSessionId, userId, addNotification } = useStore()
  const [messages, setMessages] = useState([{
    role: 'assistant',
    content: "Hello! I'm your AMEX Commerce AI Shopping Assistant.\n\nI can help you:\n• 🔍 Search and discover products\n• 🛒 Add to cart and adjust quantity\n• 💳 Checkout directly from here\n• 📦 Track your orders\n\nHow can I assist you today?",
    products: [],
    timestamp: new Date().toISOString()
  }])
  const [input, setInput]   = useState('')
  const [loading, setLoading] = useState(false)
  const bottomRef = useRef(null)
  const inputRef  = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  // Injected when a product card completes checkout
  const handleOrderConfirmed = (order, product, qty) => {
    setMessages(prev => [...prev, {
      role: 'assistant',
      content: `Your order for ${product.name} has been placed successfully! Here's your confirmation:`,
      products: [],
      timestamp: new Date().toISOString(),
      orderConfirmation: order,
      orderProduct: product,
      orderQty: qty
    }])
  }

  const send = async (text) => {
    const msg = text || input.trim()
    if (!msg || loading) return

    setMessages(prev => [...prev, {
      role: 'user', content: msg, products: [], timestamp: new Date().toISOString()
    }])
    setInput('')
    setLoading(true)

    try {
      const res  = await agentService.chat(sessionId, msg, userId)
      const data = res.data
      if (!sessionId && data.sessionId) setSessionId(data.sessionId)

      let finalMessage  = data.message || ''
      let finalProducts = data.products || []

      if (finalProducts.length === 0 && finalMessage.trim().startsWith('{')) {
        try {
          const stripped = finalMessage
            .replace(/^```(?:json)?\s*/i, '').replace(/```\s*$/, '').trim()
          const parsed = JSON.parse(stripped)
          if (parsed.message) finalMessage = parsed.message
          if (Array.isArray(parsed.products)) finalProducts = parsed.products
        } catch {}
      }

      setMessages(prev => [...prev, {
        role: 'assistant',
        content: finalMessage,
        products: finalProducts,
        timestamp: data.timestamp || new Date().toISOString()
      }])
    } catch {
      setMessages(prev => [...prev, {
        role: 'assistant',
        content: '⚠️ Unable to connect. Please check that all services are running.',
        products: [],
        timestamp: new Date().toISOString()
      }])
      addNotification('Agent connection failed', 'error')
    } finally {
      setLoading(false)
      inputRef.current?.focus()
    }
  }

  const handleClear = () => {
    if (sessionId) agentService.clearSession(sessionId).catch(() => {})
    setSessionId(null)
    setMessages([{
      role: 'assistant',
      content: 'Chat cleared. How can I help you today?',
      products: [],
      timestamp: new Date().toISOString()
    }])
  }

  return (
    <div className="min-h-screen bg-surface-light flex flex-col">

      {/* Header */}
      <div className="bg-white border-b border-surface-border shrink-0">
        <div className="max-w-4xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-amex-navy flex items-center justify-center shadow-amex-sm">
              <Bot className="w-5 h-5 text-white" />
            </div>
            <div>
              <h1 className="font-bold text-ink-primary text-base">AI Shopping Assistant</h1>
              <div className="flex items-center gap-1.5 mt-0.5">
                <div className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
                <span className="text-xs text-ink-secondary">Spring AI + MCP · GPT-4o-mini</span>
              </div>
            </div>
          </div>
          <Button variant="secondary" size="sm" onClick={handleClear}>
            <Trash2 className="w-3.5 h-3.5" /> Clear
          </Button>
        </div>

        {/* MCP tools strip */}
        <div className="max-w-4xl mx-auto px-4 pb-3 flex gap-2 overflow-x-auto">
          {MCP_TOOLS.map(tool => (
            <span key={tool} className="flex items-center gap-1 text-xs text-amex-blue bg-amex-light border border-amex-blue/20 rounded-full px-2.5 py-1 whitespace-nowrap font-medium shrink-0">
              <Zap className="w-3 h-3" />{tool}
            </span>
          ))}
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto">
        <div className="max-w-4xl mx-auto px-4 py-6 space-y-5">
          {messages.map((msg, i) => (
            <Message key={i} msg={msg} onOrderConfirmed={handleOrderConfirmed} />
          ))}
          {loading && <TypingIndicator />}
          <div ref={bottomRef} />
        </div>
      </div>

      {/* Suggestions */}
      {messages.length <= 2 && (
        <div className="max-w-4xl mx-auto w-full px-4 pb-2">
          <p className="text-xs text-ink-muted mb-2 font-medium">Try asking:</p>
          <div className="flex flex-wrap gap-2">
            {SUGGESTIONS.map(s => (
              <button key={s} onClick={() => send(s)}
                className="text-xs px-3 py-1.5 bg-white border border-surface-border rounded-full text-ink-secondary hover:border-amex-blue hover:text-amex-blue transition-all">
                {s}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Input bar */}
      <div className="shrink-0 bg-white border-t border-surface-border">
        <div className="max-w-4xl mx-auto px-4 py-3">
          <div className="flex gap-2 bg-surface-light border border-surface-border rounded-lg p-1.5">
            <input
              ref={inputRef}
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && !e.shiftKey && send()}
              placeholder="Ask me to find products, manage cart, or checkout..."
              className="flex-1 bg-transparent text-sm text-ink-primary placeholder-ink-muted outline-none px-2 py-1"
              disabled={loading}
            />
            <button
              onClick={() => send()}
              disabled={!input.trim() || loading}
              className="bg-amex-blue hover:bg-amex-dark disabled:opacity-40 disabled:cursor-not-allowed text-white rounded px-4 py-2 flex items-center gap-1.5 text-sm font-semibold transition-colors shrink-0"
            >
              {loading
                ? <span className="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin" />
                : <Send className="w-4 h-4" />}
              Send
            </button>
          </div>
          <p className="text-center text-[10px] text-ink-muted mt-1.5">
            AI responses may vary · Powered by OpenAI GPT-4o-mini via Spring AI MCP
          </p>
        </div>
      </div>
    </div>
  )
}