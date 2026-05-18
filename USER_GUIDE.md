# Application Usage Guide

Follow these steps to experience the complete agentic commerce flow — from adding a payment card to checking out via an AI shopping agent.

> 📹 **Video Walkthrough:** Watch the demo video for a complete visual guide. Timestamps are noted for each step below.

<video src="https://github.com/kathirvelarun/agentic-commerce/raw/main/Demo-Agentic-Commerce.mp4" controls title="Agentic Commerce Demo">
</video>

---

## Step 1: Add a Payment Card

*📹 Demo: 0:00 – 0:20*

1. Open the application at **http://localhost:3000**
2. Navigate to the **Cards** screen via the top navigation bar
3. Click **"Add Card"**
4. Enter your card details:
   - Card Number (e.g., `4242 4242 4242 4242` for testing)
   - Expiry Date
   - CVV
   - Cardholder Name
5. Click **"Add Card"** to save
6. Your card will appear in the cards list and is now ready to use at checkout

---

## Step 2: Browse Products in the Shop

*📹 Demo: 0:20 – 0:45*

1. Navigate to the **Shop** screen (home page `/`)
2. Browse the product catalog — **16 products** across 7 categories:
   - Electronics, Footwear, Clothing, Kitchen, Home, Sports, Books
3. Use the **search bar** to filter products by keyword
4. Use the **category filter** to narrow results
5. Click **"Add to Cart"** on any product card to add it to your cart
6. The cart sidebar will open showing your selected items

---

## Step 3: Shop with the AI Agent

*📹 Demo: 0:45 – 1:30*

1. Navigate to the **AI Agent** screen via the top navigation bar
2. Start a conversation — for example:
   - *"Show me wireless headphones under $300"*
   - *"I need a gift for someone who likes fitness"*
   - *"What laptops do you have?"*
3. The AI agent will:
   - Understand your intent and ask clarifying questions
   - Search the product catalog using the `search_products` tool
   - Return personalized recommendations with product details and prices
4. Click **"Add to Cart"** on any recommended product card in the chat
5. Continue the conversation — the agent remembers context across messages:
   - *"Add the Sony headphones to my cart"*
   - *"What's in my cart right now?"*
   - *"Remove the last item"*

---

## Step 4: Complete Checkout

*📹 Demo: 1:30 – 2:00*

1. When ready, tell the agent **"checkout"** or navigate to the **Cart** sidebar and click **"Proceed to Checkout"**
2. On the Checkout screen, select your payment card from the list
3. Review the order summary (items, quantities, total)
4. Click **"Place Order"**
5. The agent can also handle checkout conversationally:
   - *"Checkout with my Visa card ending in 4242"*
   - The agent calls `checkout` → processes payment → returns your order ID

---

## Step 5: View Order Confirmation & History

*📹 Demo: 2:00 – end*

1. After checkout, you are taken to the **Order Confirmation** screen
2. Note your **Order ID** and order details
3. Navigate to the **Orders** screen to view your full order history
4. You can also ask the AI agent:
   - *"Show me my recent orders"*
   - The agent calls `get_orders` and returns your order history inline in chat

---

## Example Agent Conversations

| Goal | Example Prompt |
|------|----------------|
| Find products | *"Show me wireless headphones under $300"* |
| Get recommendations | *"What's the best laptop you have?"* |
| Add to cart | *"Add the Sony WH-1000XM5 to my cart"* |
| View cart | *"What's in my cart?"* |
| Update quantity | *"Change the headphones quantity to 2"* |
| Remove item | *"Remove the yoga mat from my cart"* |
| Checkout | *"Checkout with my Visa card ending in 4242"* |
| Order history | *"Show me my recent orders"* |

---

## Quick Start Reference

| Service | URL | Purpose |
|---------|-----|---------|
| React Frontend | http://localhost:3000 | Main UI |
| Agent Backend | http://localhost:8000 | AI chat + card management |
| Merchant Backend | http://localhost:8001 | Products, cart, orders |
| Merchant MCP Server | http://localhost:8002/sse | MCP tool server (SSE) |

See [README.md](README.md) for full setup instructions.