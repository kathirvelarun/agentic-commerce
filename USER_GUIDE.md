# Application Usage Guide

Follow these steps to experience the complete agentic commerce flow.

> 📹 **Video Walkthrough:** Watch the Demo Video for a complete visual guide. Timestamps are noted for each step below.

[Demo Video](https://github.com/user-attachments/assets/312d94a7-cc69-4e94-86f2-ea3408c2a773)

---

## Step 1: Sign In

*📹 Demo: 0:00 – 0:12*

1. Open the application at **http://localhost:3000**
2. You will be redirected to the **Commerce Portal** login screen
3. Enter the demo credentials:
   - **Username:** `demo-user`
   - **Password:** `admin`
4. Click **"Sign In"**

---

## Step 2: Browse the Shop

*📹 Demo: 0:12 – 0:33*

1. After login you land on the **Shop** screen
2. Browse **16 products** across 7 categories: Electronics, Footwear, Clothing, Kitchen, Home, Sports, Books
3. Use the **search bar** in the hero banner to filter by keyword
4. Click a **category chip** to narrow results
5. Click **"Add to Cart"** on any product card to add it to your cart

---

## Step 3: Add a Payment Card

*📹 Demo: 0:33 – 1:05*

1. Navigate to the **Cards** screen via the top navigation bar
2. Click **"Add Card"**
3. Enter your card details in the modal:
   - **Card Number** — the brand (Visa / Mastercard / Amex) is detected automatically as you type
   - **Cardholder Name**
   - **Month** (2 digits) and **Year** (4 digits)
   - **CVV**
4. Click **"Save Card"**
5. Your card appears with **PENDING** status — it becomes **ACTIVE** once a passkey is set up

---

## Step 4: Set Up a Passkey

*📹 Demo: 1:05 – 1:32*

1. On the **Cards** screen, find your newly added card showing **PENDING** status
2. Click **"Set Up Passkey"**
3. In the **Create Passkey** modal, click **"Create Passkey"**
4. Follow your device prompt to register using **Face ID, fingerprint, or PIN**
5. Your card status updates to **ACTIVE** — it is now ready for passkey-authenticated checkout
6. You can manage your passkeys at **sandbox.auth.amex.com/passkey**

---

## Step 5: Shop with the AI Agent

*📹 Demo: 1:32 – 1:55*

1. Navigate to the **AI Agent** screen via the top navigation bar
2. Start a conversation using the input bar or click a **suggestion chip**, for example:
   - *"Show me the best headphones under $300"*
   - *"I need a gift for a fitness enthusiast"*
   - *"Find me a laptop for programming"*
3. The agent searches the product catalog using MCP tools and returns **inline product cards** directly in the chat
4. The active MCP tools are shown as badges in the agent header: `search_products`, `add_to_cart`, `get_cart`, `checkout`, `get_orders`

---

## Step 6: Checkout with Passkey

*📹 Demo: 1:55 – 2:25*

1. On an inline product card in the chat, click **"Add to Cart"**
2. Adjust quantity using the **+/−** controls if needed
3. Your saved card is pre-selected in the dropdown
4. Click **"Verify Identity & Continue"**
5. Authenticate using your device **passkey** (Face ID, fingerprint, or PIN)
6. An **Order Confirmed** card appears inline in the chat with your order ID and total

---

## Step 7: View Order History

*📹 Demo: 2:25 – 2:38*

1. Navigate to the **Orders** screen via the top navigation bar
2. Your confirmed order appears with:
   - Order ID, date, and **CONFIRMED** status badge
   - Items purchased and quantities
   - Payment card used (e.g., *Paid with Amex •••• 2108*)
3. You can also ask the AI agent: *"Show me my recent orders"*

---

## Step 8: Observe the Agent Flow in Flow Logs

*📹 Demo: 2:38 – end*

Navigate to **Flow Logs** via the top navigation bar to see every AI Agent request traced end-to-end across **4 steps**:

| Step | Label |
|------|-------|
| 1 | UI → Agent Backend (Request Received) |
| 2 | Agent Backend → LLM (OpenAI GPT-4o-mini) |
| 3 | LLM → MCP (Tool Dispatch) |
| 4 | MCP → Merchant API (Tool Execution Result) |

Each step shows status (DONE / ERROR / RUNNING), detail text, and duration in ms. A live **Flow Logs panel** is also available at the bottom of the AI Agent page — expand it to watch steps arrive in real time while the agent is thinking.

---

## Example Agent Prompts

| Goal | Example Prompt |
|------|----------------|
| Search products | *"Show me wireless headphones under $300"* |
| Browse by category | *"What kitchen appliances do you have?"* |
| Get recommendations | *"I need a gift for a fitness enthusiast"* |
| Add to cart | *"Add the Sony WH-1000XM5 to my cart"* |
| View cart | *"What's in my cart?"* |
| Update quantity | *"Change the headphones quantity to 2"* |
| Remove item | *"Remove the yoga mat from my cart"* |
| Checkout | *"Checkout with my saved card"* |
| Order history | *"Show me my recent orders"* |

---

## Quick Start Reference

| Service | URL | Purpose |
|---------|-----|---------|
| React Frontend | http://localhost:3000 | Main UI |
| Agent Backend | http://localhost:8000 | AI chat, card management, flow logs |
| Merchant Backend | http://localhost:8001 | Products, cart, orders |
| Merchant MCP Server | http://localhost:8002/sse | MCP tool server (SSE) |

See [README.md](README.md) for full setup instructions.
