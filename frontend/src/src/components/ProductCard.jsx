import React from 'react'
import { ShoppingCart, Star } from 'lucide-react'
import { cartService } from '../services/api'
import { useStore } from '../store'
import { Badge, Price, StarRating, Button } from './ui'

const CATEGORY_COLORS = {
  Electronics: 'blue', Clothing: 'blue', Kitchen: 'amber',
  Sports: 'green', Books: 'navy', Footwear: 'gray', Home: 'gray'
}

export default function ProductCard({ product, onView }) {
  const { userId, setCart, addNotification } = useStore()
  const [adding, setAdding] = React.useState(false)

  const handleAdd = async (e) => {
    e.stopPropagation()
    setAdding(true)
    try {
      const res = await cartService.addItem(userId, product.id, 1)
      setCart(res.data)
      addNotification(`${product.name} added to cart`, 'success')
    } catch { addNotification('Failed to add to cart', 'error') }
    finally { setAdding(false) }
  }

  return (
    <div
      onClick={onView}
      className="bg-white border border-surface-border rounded-lg overflow-hidden cursor-pointer group transition-all duration-200 hover:border-amex-blue hover:shadow-amex-md flex flex-col"
    >
      {/* Image */}
      <div className="relative h-44 overflow-hidden bg-surface-light product-img-wrap">
        <img
          src={product.imageUrl}
          alt={product.name}
          className="w-full h-full object-cover"
          onError={e => { e.target.src = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400' }}
        />
        <div className="absolute top-2.5 left-2.5">
          <Badge color={CATEGORY_COLORS[product.category] || 'gray'}>{product.category}</Badge>
        </div>
        {product.stock <= 5 && product.stock > 0 && (
          <div className="absolute top-2.5 right-2.5">
            <Badge color="amber">Low Stock</Badge>
          </div>
        )}
        {product.stock === 0 && (
          <div className="absolute top-2.5 right-2.5">
            <Badge color="red">Out of Stock</Badge>
          </div>
        )}
      </div>

      {/* Body */}
      <div className="p-4 flex flex-col flex-1 gap-2">
        {product.brand && (
          <p className="text-xs font-semibold text-amex-blue uppercase tracking-wider">{product.brand}</p>
        )}
        <h3 className="text-sm font-semibold text-ink-primary leading-snug line-clamp-2 flex-1">
          {product.name}
        </h3>
        <p className="text-xs text-ink-secondary line-clamp-2">{product.description}</p>

        <div className="flex items-center justify-between pt-1">
          <StarRating rating={product.rating} />
          <span className="text-xs text-ink-muted">{product.stock} in stock</span>
        </div>

        {/* Price + CTA */}
        <div className="flex items-center justify-between pt-2 border-t border-surface-muted mt-1">
          <Price amount={product.price} size="md" />
          <Button
            variant="primary"
            size="sm"
            loading={adding}
            onClick={handleAdd}
            disabled={product.stock === 0}
          >
            <ShoppingCart className="w-3.5 h-3.5" />
            Add
          </Button>
        </div>
      </div>
    </div>
  )
}
