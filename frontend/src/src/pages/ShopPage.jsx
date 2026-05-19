import React, { useEffect, useState } from 'react'
import { Search, ShoppingCart, Sparkles } from 'lucide-react'
import { productService, cartService } from '../services/api'
import { useStore } from '../store'
import ProductCard from '../components/ProductCard'
import { Button, Modal, StarRating, Price, Badge, Skeleton, SectionHeader } from '../components/ui'

const CATEGORIES = ['All', 'Electronics', 'Footwear', 'Clothing', 'Kitchen', 'Home', 'Sports', 'Books']

export default function ShopPage() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('All')
  const [selected, setSelected] = useState(null)
  const [adding, setAdding] = useState(false)
  const { userId, setCart, addNotification } = useStore()

  const fetch = async (q, cat) => {
    setLoading(true)
    try {
      const res = await productService.getAll(q || undefined, cat === 'All' ? undefined : cat)
      setProducts(res.data.products || [])
    } catch { addNotification('Failed to load products', 'error') }
    finally { setLoading(false) }
  }

  useEffect(() => { fetch(search, category) }, [category])

  const handleSearch = e => { e.preventDefault(); fetch(search, category) }

  const handleAddToCart = async (product) => {
    setAdding(true)
    try {
      const res = await cartService.addItem(userId, product.id, 1)
      setCart(res.data)
      addNotification(`${product.name} added to cart`, 'success')
      setSelected(null)
    } catch { addNotification('Failed to add to cart', 'error') }
    finally { setAdding(false) }
  }

  return (
    <div className="min-h-screen bg-surface-light">

      {/* Hero banner — Amex style */}
      <div className="bg-amex-navy text-white">
        <div className="max-w-7xl mx-auto px-6 py-10">
          <div className="flex items-center gap-2 mb-3">
            <Sparkles className="w-4 h-4 text-amex-gold" />
            <span className="text-xs font-semibold uppercase tracking-widest text-amex-gold">AI-Powered Shopping</span>
          </div>
          <h1 className="text-3xl md:text-4xl font-bold text-white mb-2">
            Discover Premium Products
          </h1>
          <p className="text-white/70 text-base max-w-lg">
            Curated selection with intelligent recommendations, exclusive benefits, and seamless checkout.
          </p>

          {/* Search bar inside hero */}
          <form onSubmit={handleSearch} className="mt-6 flex gap-2 max-w-xl">
            <div className="relative flex-1">
              <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-muted" />
              <input
                value={search}
                onChange={e => setSearch(e.target.value)}
                placeholder="Search products, brands..."
                className="w-full bg-white rounded-amex pl-10 pr-4 py-3 text-sm text-ink-primary placeholder-ink-muted border-0 outline-none focus:ring-2 focus:ring-amex-blue"
              />
            </div>
            <Button type="submit" variant="primary" size="md">Search</Button>
          </form>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
        {/* Category filters */}
        <div className="flex gap-2 overflow-x-auto pb-1 mb-6">
          {CATEGORIES.map(cat => (
            <button
              key={cat}
              onClick={() => setCategory(cat)}
              className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all border ${
                category === cat
                  ? 'bg-amex-blue text-white border-amex-blue shadow-amex-sm'
                  : 'bg-white text-ink-secondary border-surface-border hover:border-amex-blue hover:text-amex-blue'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Results count */}
        {!loading && (
          <p className="text-sm text-ink-muted mb-4">
            Showing <span className="font-semibold text-ink-primary">{products.length}</span> products
            {category !== 'All' && <> in <span className="text-amex-blue font-medium">{category}</span></>}
          </p>
        )}

        {/* Grid */}
        {loading ? (
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {[...Array(8)].map((_, i) => (
              <div key={i} className="bg-white rounded-lg border border-surface-border overflow-hidden">
                <Skeleton className="h-44 w-full rounded-none" />
                <div className="p-4 space-y-2">
                  <Skeleton className="h-3 w-16" />
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-3 w-3/4" />
                  <div className="flex justify-between items-center pt-2">
                    <Skeleton className="h-6 w-20" />
                    <Skeleton className="h-8 w-16 rounded-amex" />
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : products.length === 0 ? (
          <div className="text-center py-20 bg-white rounded-lg border border-surface-border">
            <Search className="w-12 h-12 text-ink-muted mx-auto mb-3" />
            <p className="font-semibold text-ink-primary text-lg mb-1">No products found</p>
            <p className="text-ink-secondary text-sm">Try a different search or category</p>
          </div>
        ) : (
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {products.map(product => (
              <ProductCard key={product.id} product={product} onView={() => setSelected(product)} />
            ))}
          </div>
        )}
      </div>

      {/* Product detail modal */}
      <Modal
        open={!!selected}
        onClose={() => setSelected(null)}
        title="Product Details"
        footer={
          <>
            <Button variant="secondary" onClick={() => setSelected(null)}>Cancel</Button>
            <Button variant="primary" loading={adding} onClick={() => handleAddToCart(selected)}>
              <ShoppingCart className="w-4 h-4" /> Add to Cart
            </Button>
          </>
        }
      >
        {selected && (
          <div className="space-y-4">
            <div className="h-52 rounded-amex overflow-hidden bg-surface-light">
              <img src={selected.imageUrl} alt={selected.name} className="w-full h-full object-cover"
                onError={e => { e.target.src = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400' }} />
            </div>
            <div>
              {selected.brand && <p className="text-xs font-semibold text-amex-blue uppercase tracking-wider mb-1">{selected.brand}</p>}
              <h3 className="font-bold text-xl text-ink-primary">{selected.name}</h3>
              <div className="flex items-center gap-3 mt-2">
                <Price amount={selected.price} size="lg" />
                <StarRating rating={selected.rating} />
                <Badge color="gray">{selected.stock} in stock</Badge>
              </div>
            </div>
            <p className="text-sm text-ink-secondary leading-relaxed">{selected.description}</p>
            <div className="flex gap-2 flex-wrap">
              <Badge color="blue">{selected.category}</Badge>
              {selected.tags?.split(',').slice(0, 3).map(t => (
                <Badge key={t} color="gray">{t.trim()}</Badge>
              ))}
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}
