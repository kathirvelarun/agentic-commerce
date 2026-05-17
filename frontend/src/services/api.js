import axios from 'axios'

// Merchant Backend (port 8001)
const merchantApi = axios.create({
  baseURL: '/api/merchant',
  headers: { 'Content-Type': 'application/json' }
})

// Agent Backend (port 8000)
const agentApi = axios.create({
  baseURL: '/api/agent',
  headers: { 'Content-Type': 'application/json' }
})

// === PRODUCTS ===
export const productService = {
  getAll: (query, category) => {
    const params = {}
    if (query) params.query = query
    if (category) params.category = category
    return merchantApi.get('/products', { params })
  },
  getById: (id) => merchantApi.get(`/products/${id}`),
  getCategories: () => merchantApi.get('/products/categories'),
}

// === CART ===
export const cartService = {
  getCart: (userId) => merchantApi.get(`/users/${userId}/cart`),
  addItem: (userId, productId, quantity) =>
    merchantApi.post(`/users/${userId}/cart/items`, { productId, quantity }),
  updateItem: (userId, itemId, quantity) =>
    merchantApi.put(`/users/${userId}/cart/items/${itemId}`, { quantity }),
  removeItem: (userId, itemId) =>
    merchantApi.delete(`/users/${userId}/cart/items/${itemId}`),
  clearCart: (userId) => merchantApi.delete(`/users/${userId}/cart`),
}

// === ORDERS ===
export const orderService = {
  checkout: (userId, cartId, cardLast4, cardBrand) =>
    merchantApi.post(`/users/${userId}/cart/${cartId}/checkout`, {
      cardLast4, cardBrand, shippingAddress: '123 Main St, City, State 12345'
    }),
  getOrders: (userId) => merchantApi.get(`/users/${userId}/orders`),
  getOrder: (orderId) => merchantApi.get(`/orders/${orderId}`),
}

// === AGENT CHAT ===
export const agentService = {
  chat: (sessionId, message, userId) =>
    agentApi.post('/chat', { sessionId, message, userId }),
  getHistory: (sessionId) => agentApi.get(`/chat/${sessionId}/history`),
  clearSession: (sessionId) => agentApi.delete(`/chat/${sessionId}`),
}

// === FLOW LOGS ===
export const logService = {
  getBySession: (sessionId) => agentApi.get(`/logs/${sessionId}`),
  getAll: () => agentApi.get('/logs'),
  deleteBySession: (sessionId) => agentApi.delete(`/logs/${sessionId}`),
  deleteAll: () => agentApi.delete('/logs'),
}

// === CARDS ===
export const cardService = {
  getCards: (userId) => agentApi.get(`/users/${userId}/cards`),
  addCard: (userId, cardData) => agentApi.post(`/users/${userId}/cards`, cardData),
  deleteCard: (userId, cardId) => agentApi.delete(`/users/${userId}/cards/${cardId}`),
  setDefault: (userId, cardId) => agentApi.put(`/users/${userId}/cards/${cardId}/default`),
}
