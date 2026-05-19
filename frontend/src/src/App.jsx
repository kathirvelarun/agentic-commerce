import React, { useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import CartSidebar from './components/CartSidebar'
import ShopPage from './pages/ShopPage'
import AgentPage from './pages/AgentPage'
import CheckoutPage from './pages/CheckoutPage'
import LoginPage from './pages/LoginPage'
import { OrderConfirmationPage, OrdersPage, CardsPage, FlowLogsPage } from './pages/OtherPages'
import PasskeyManagerPage from './pages/PasskeyManagerPage'
import { Notifications } from './components/ui'
import { useStore } from './store'
import { cartService } from './services/api'

function ProtectedRoute({ children }) {
  const { isLoggedIn } = useStore()
  return isLoggedIn ? children : <Navigate to="/login" replace />
}

export default function App() {
  const { userId, setCart, notifications, isLoggedIn } = useStore()

  useEffect(() => {
    if (isLoggedIn) {
      cartService.getCart(userId).then(res => setCart(res.data)).catch(() => {})
    }
  }, [userId, isLoggedIn])

  return (
    <BrowserRouter>
      <div className="min-h-screen bg-surface-light">
        {isLoggedIn && <Navbar />}
        {isLoggedIn && <CartSidebar />}
        <main>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/"                   element={<ProtectedRoute><ShopPage /></ProtectedRoute>} />
            <Route path="/agent"              element={<ProtectedRoute><AgentPage /></ProtectedRoute>} />
            <Route path="/checkout"           element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>} />
            <Route path="/order-confirmation" element={<ProtectedRoute><OrderConfirmationPage /></ProtectedRoute>} />
            <Route path="/orders"             element={<ProtectedRoute><OrdersPage /></ProtectedRoute>} />
            <Route path="/cards"              element={<ProtectedRoute><CardsPage /></ProtectedRoute>} />
            <Route path="/logs"               element={<ProtectedRoute><FlowLogsPage /></ProtectedRoute>} />
            <Route path="/passkeys"           element={<ProtectedRoute><PasskeyManagerPage /></ProtectedRoute>} />
            <Route path="*"                   element={<Navigate to={isLoggedIn ? '/' : '/login'} replace />} />
          </Routes>
        </main>
        <Notifications notifications={notifications} />
      </div>
    </BrowserRouter>
  )
}