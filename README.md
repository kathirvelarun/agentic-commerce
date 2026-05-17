# 🛍️ NexCart — Agentic AI Commerce Platform

> End-to-end agentic commerce flow with AI-driven product discovery and checkout, built with **Java 21**, **Spring Boot 3.3**, **Spring AI**, **OpenAI GPT-4o-mini**, and **React**.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        React Frontend (Port 3000)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │  Shop Page   │  │  AI Agent    │  │  Checkout    │              │
│  │  (Catalog)   │  │  Chat UI     │  │  + Orders    │              │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘              │
└─────────┼────────────────┼────────────────  ┼───────────────────────┘
          │                │                  │
          ▼                ▼                  ▼
┌─────────────────┐   ┌────────────────────────┐
│ Merchant Backend│   │    Agent Backend        │
│   (Port 8001)   │   │     (Port 8000)         │
│                 │   │                         │
│ • Products API  │   │ • Chat API              │
│ • Cart API      │   │ • Card Management       │
│ • Orders API    │   │ • Spring AI ChatClient  │
│ • H2 Database   │◄──│ • MCP Client            │
└─────────────────┘   └────────────┬────────────┘
                                   │ SSE (MCP Protocol)
                                   ▼
                      ┌────────────────────────┐
                      │  Merchant MCP Server   │
                      │     (Port 8002)        │
                      │                        │
                      │ @Tool search_products  │
                      │ @Tool add_to_cart      │
                      │ @Tool checkout         │
                      │ @Tool get_orders       │
                      │ ... 9 tools total      │
                      └────────────────────────┘
```

## 🚀 Modules

### 1. 🤖 Agent Backend (Port 8000)
AI Shopping Agent powered by Spring AI and GPT-4o-mini.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/chat` | POST | Send message to AI agent |
| `/api/v1/chat/{sessionId}/history` | GET | Get conversation history |
| `/api/v1/chat/{sessionId}` | DELETE | Clear chat session |
| `/api/v1/users/{userId}/cards` | GET | List payment cards |
| `/api/v1/users/{userId}/cards` | POST | Add payment card |
| `/api/v1/users/{userId}/cards/{cardId}` | DELETE | Remove card |
| `/api/v1/users/{userId}/cards/{cardId}/default` | PUT | Set default card |
| `/api/v1/health` | GET | Health check |

**Chat Request:**
```json
{
  "sessionId": "session-123",
  "message": "Show me the best headphones under $300",
  "userId": "user-001"
}
```

**Chat Response:**
```json
{
  "sessionId": "session-123",
  "message": "I found some great headphones...",
  "role": "assistant",
  "timestamp": "2024-01-01T12:00:00",
  "intent": "SEARCH"
}
```

---

### 2. 🏪 Merchant Backend (Port 8001)
REST API for Product Catalog, Shopping Cart, and Order Management.

#### Product Catalog
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/products` | GET | Search/list products (`?query=...&category=...`) |
| `/api/v1/products/{id}` | GET | Get product by ID |
| `/api/v1/products/categories` | GET | Get all categories |
| `/api/v1/products` | POST | Create product |

#### Shopping Cart
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/users/{userId}/cart` | GET | Get or create cart |
| `/api/v1/users/{userId}/cart/items` | POST | Add item to cart |
| `/api/v1/users/{userId}/cart/items/{itemId}` | PUT | Update item quantity |
| `/api/v1/users/{userId}/cart/items/{itemId}` | DELETE | Remove item |
| `/api/v1/users/{userId}/cart` | DELETE | Clear cart |

#### Orders / Checkout
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/users/{userId}/cart/{cartId}/checkout` | POST | Process checkout |
| `/api/v1/users/{userId}/orders` | GET | Get order history |
| `/api/v1/orders/{orderId}` | GET | Get order by ID |

---

### 3. 🔌 Merchant MCP Server (Port 8002)
Spring AI MCP Server exposing 9 tools via Server-Sent Events.

| MCP Tool | Description |
|----------|-------------|
| `search_products` | Search product catalog with query + category filters |
| `get_product_details` | Get full product details by ID |
| `get_categories` | List all product categories |
| `get_cart` | Get user's current cart |
| `add_to_cart` | Add product to cart |
| `update_cart_item` | Update item quantity (0 = remove) |
| `remove_from_cart` | Remove specific item |
| `checkout` | Process payment and create order |
| `get_orders` | Get user order history |

**MCP Connection:** `http://localhost:8002/sse`

---

### 4. ⚛️ React Frontend (Port 3000)
Modern dark-themed e-commerce UI.

| Page | Route | Features |
|------|-------|----------|
| Shop | `/` | Product catalog, search, filters, cart |
| AI Agent | `/agent` | Conversational shopping with AI |
| Checkout | `/checkout` | Card selection, order summary |
| Confirmation | `/order-confirmation` | Order success |
| Orders | `/orders` | Order history |
| Cards | `/cards` | Payment card management |

---

## ⚡ Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- Node.js 20+
- OpenAI API Key

### 1. Start Merchant Backend
```bash
cd merchant-backend
mvn spring-boot:run
# Starts on http://localhost:8001
# Auto-seeds 16 products across 7 categories
```

### 2. Start Merchant MCP Server
```bash
cd merchant-mcp
mvn spring-boot:run
# Starts on http://localhost:8002
# MCP SSE endpoint: http://localhost:8002/sse
```

### 3. Start Agent Backend
```bash
cd agent-backend
export OPENAI_API_KEY=your-api-key-here
export OPENAI_API_KEY=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
mvn spring-boot:run
# Starts on http://localhost:8000
```

### 4. Start Frontend
```bash
cd frontend
npm install
npm run dev
# Starts on http://localhost:3000
```

### Docker Compose (All at once)
```bash
export OPENAI_API_KEY=your-api-key
docker-compose up --build
# Frontend: http://localhost:3000
```

---

## 🤖 AI Agent — How It Works

The AI agent uses **Spring AI** with **MCP (Model Context Protocol)** for tool-based interactions:

1. User sends a message → `POST /api/v1/chat`
2. Agent Backend creates a `ChatClient` with MCP tool callbacks
3. Spring AI sends the message + tool definitions to GPT-4o-mini
4. OpenAI decides which MCP tools to invoke (e.g., `search_products`)
5. Spring AI calls the Merchant MCP Server via SSE
6. MCP Server invokes the tool → calls Merchant Backend REST API
7. Results flow back: MCP → Spring AI → OpenAI → final response
8. Frontend displays the AI's response with product cards

### Example Conversations

**Product Discovery:**
> "Show me wireless headphones under $300"
→ AI calls `search_products(query="wireless headphones", category="Electronics")`
→ Returns matching products with prices and ratings

**Cart Management:**
> "Add the Sony WH-1000XM5 to my cart"
→ AI calls `add_to_cart(userId, productId, 1)`
→ Confirms item added with updated cart total

**Checkout:**
> "Checkout with my Visa card ending in 4242"
→ AI calls `get_cart` → `checkout(userId, cartId, "4242", "Visa")`
→ Returns order confirmation with order ID

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| AI Model | OpenAI GPT-4o-mini |
| AI Framework | Spring AI 1.0.0-M3 |
| MCP Protocol | Spring AI MCP Server/Client |
| Backend | Java 21, Spring Boot 3.3 |
| Database | H2 (in-memory, dev) |
| Frontend | React 18, Vite, Tailwind CSS |
| State | Zustand |
| HTTP Client | Axios |
| Containerization | Docker, Docker Compose |

---

## 📁 Project Structure

```
agentic-commerce/
├── agent-backend/          # Port 8000 — AI Agent + Card Mgmt
│   ├── src/main/java/com/commerce/agent/
│   │   ├── controller/AgentController.java
│   │   ├── service/ShoppingAgentService.java
│   │   ├── model/AgentCard.java
│   │   ├── dto/AgentDtos.java
│   │   └── config/AgentConfig.java
│   └── src/main/resources/application.yml
│
├── merchant-backend/       # Port 8001 — Products, Cart, Orders
│   ├── src/main/java/com/commerce/merchant/
│   │   ├── controller/MerchantController.java
│   │   ├── service/{ProductService, CartOrderService}.java
│   │   ├── model/{Product, Cart, CartItem, Order}.java
│   │   ├── repository/
│   │   └── config/DataSeeder.java   ← Seeds 16 products
│   └── src/main/resources/application.yml
│
├── merchant-mcp/           # Port 8002 — MCP Tool Server
│   ├── src/main/java/com/commerce/mcp/
│   │   ├── tool/MerchantTools.java  ← 9 @Tool methods
│   │   ├── service/MerchantClientService.java
│   │   └── config/McpConfig.java
│   └── src/main/resources/application.yml
│
├── frontend/               # Port 3000 — React UI
│   ├── src/
│   │   ├── App.jsx
│   │   ├── pages/{ShopPage, AgentPage, CheckoutPage, ...}
│   │   ├── components/{Navbar, CartSidebar, ProductCard, ui}
│   │   ├── services/api.js
│   │   └── store/index.js
│   └── vite.config.js
│
└── docker-compose.yml
```

---

## 🔑 Environment Variables

| Variable | Service | Description |
|----------|---------|-------------|
| `OPENAI_API_KEY` | agent-backend | Your OpenAI API key |
| `MERCHANT_BACKEND_URL` | merchant-mcp | Merchant Backend URL (default: http://localhost:8001) |

---

## 📦 Seeded Product Catalog

The Merchant Backend auto-seeds **16 products** across **7 categories**:

| Category | Products |
|----------|---------|
| Electronics | Sony WH-1000XM5, AirPods Pro, Samsung QLED TV, iPad Pro, MacBook Air M3 |
| Footwear | Nike Air Max 270, Adidas Ultraboost 23 |
| Clothing | Levi's 501 Jeans, Patagonia Down Jacket |
| Kitchen | Instant Pot, KitchenAid Stand Mixer |
| Home | Dyson V15 Vacuum |
| Sports | Yoga Mat, Theragun Pro |
| Books | The Psychology of Money, Atomic Habits |
