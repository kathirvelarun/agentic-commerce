import { create } from 'zustand'
import { persist } from 'zustand/middleware'

const DEMO_USER_ID = 'user-demo-001'

export const useStore = create(
  persist(
    (set, get) => ({
      // Auth
      isLoggedIn: false,
      currentUser: null,
      login: (username) => set({ isLoggedIn: true, currentUser: username }),
      logout: () => set({ isLoggedIn: false, currentUser: null, sessionId: null, cart: null }),

      // User
      userId: DEMO_USER_ID,

      // Cart
      cart: null,
      cartOpen: false,
      setCart: (cart) => set({ cart }),
      setCartOpen: (open) => set({ cartOpen: open }),
      getCartCount: () => {
        const cart = get().cart
        if (!cart) return 0
        return cart.items?.reduce((sum, item) => sum + item.quantity, 0) || 0
      },

      // Chat session
      sessionId: null,
      setSessionId: (id) => set({ sessionId: id }),

      // Orders
      lastOrder: null,
      setLastOrder: (order) => set({ lastOrder: order }),

      // Cards — always stored as an array regardless of what the API returns
      cards: [],
      setCards: (cards) => set({ cards: Array.isArray(cards) ? cards : [] }),

      // UI
      notifications: [],
      addNotification: (msg, type = 'success') => {
        const id = Date.now()
        set(s => ({ notifications: [...s.notifications, { id, msg, type }] }))
        setTimeout(() => set(s => ({
          notifications: s.notifications.filter(n => n.id !== id)
        })), 3000)
      },
    }),
    {
      name: 'nexcart-store',
      partialize: (state) => ({
        userId: state.userId,
        sessionId: state.sessionId,
        isLoggedIn: state.isLoggedIn,
        currentUser: state.currentUser,
      }),
    }
  )
)
