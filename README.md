# VIC Reference Agent — Java / Spring Boot / Spring AI

Java migration of the [Visa Intelligent Commerce Reference Agent](https://github.com/visa/vic-reference-agent).

## Stack

| Service | Port | Technology |
|---|---|---|
| `agent-backend` | 8000 | Spring Boot 3.3 + Spring AI 1.0 + Nimbus JOSE |
| `merchant-backend` | 8001 | Spring Boot 3.3 + Spring Data JPA + H2 |
| `merchant-mcp` | 8002 | Spring Boot 3.3 + Spring AI MCP Server |
| `reference-agent-frontend` | 3000 | React / Vite (unchanged) |
| `reference-merchant-frontend` | 3001 | React / Vite (unchanged) |

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose (for containerised deployment)

## Configuration

Copy the original `.env` to the project root. All environment variable names are identical to the Python version.

```bash
cp /path/to/original/.env .env
```

## Running with Docker Compose

```bash
# Build and start all services
docker compose up --build

# Stop
docker compose down
```

## Running locally (development)

```bash
# Start merchant-backend first (no external deps)
cd merchant-backend
mvn spring-boot:run

# Start merchant-mcp (depends on merchant-backend)
cd merchant-mcp
mvn spring-boot:run

# Start agent-backend (depends on merchant-mcp + VDP credentials in env)
cd agent-backend
export $(cat ../.env | xargs)
mvn spring-boot:run
```

## Building fat JARs

```bash
# Build all modules from parent
mvn package -DskipTests

# Individual module
mvn package -pl agent-backend -am -DskipTests
```

## Project Structure

```
vic-reference-agent-java/
├── pom.xml                        # Parent POM (Spring Boot 3.3 + Spring AI 1.0 BOM)
├── docker-compose.yml
├── agent-backend/                 # Port 8000 — AI agent + VDP client
│   ├── src/main/java/com/visa/vic/agent/
│   │   ├── config/                # AppConfig, AiConfig, JweKeyConfig, CorsConfig
│   │   ├── controller/            # 5 REST controllers + exception handler + VDP logging
│   │   ├── dto/                   # Request/Response records (cards, chat, commerce, passkey)
│   │   ├── entity/                # 6 JPA entities (Card, CardArt, Intent, Mandate, Transaction…)
│   │   ├── repository/            # 7 Spring Data JPA repositories
│   │   ├── service/               # AgentService, ChatService, CardService, PasskeyService, CommerceService
│   │   │   └── vdp/               # VdpClient (HMAC signing, JWE MLE, all VTS/VIC API methods)
│   │   └── util/                  # Constants, Base64UrlUtil
│   └── src/main/resources/application.properties
├── merchant-backend/              # Port 8001 — e-commerce catalog/cart/orders
│   ├── src/main/java/com/visa/vic/merchant/
│   │   ├── config/                # CorsConfig, GlobalExceptionHandler
│   │   ├── controller/            # ProductController, CartController, OrderController
│   │   ├── dto/                   # MerchantDtos (sealed interface with all records)
│   │   ├── entity/                # Product, Cart, CartItem, Order, OrderItem
│   │   ├── repository/            # 5 Spring Data JPA repositories
│   │   └── service/               # ProductService, CartService, OrderService
│   └── src/main/resources/
│       ├── application.properties
│       └── data.sql               # 19 sample products (seed on first boot)
└── merchant-mcp/                  # Port 8002 — Spring AI MCP Server (6 tools)
    ├── src/main/java/com/visa/vic/mcp/
    │   ├── config/McpConfig.java  # RestClient bean
    │   └── tools/MerchantTools.java  # 6 @Tool methods
    └── src/main/resources/application.properties
```

## Key Design Decisions

### Python async → Java virtual threads
All `async def` route handlers map to standard `@RestController` methods.
`spring.threads.virtual.enabled=true` enables Project Loom virtual threads globally,
giving equivalent non-blocking throughput with no async/await syntax.

### LangChain + LangGraph → Spring AI ChatClient
`create_agent()` + `InMemorySaver` → `ChatClient` + `MessageChatMemoryAdvisor`.
The conversation thread ID (`current_thread_id`) is a volatile field on `AgentService`.

### MCP StreamableHTTP client/server
Python `streamablehttp_client` + Node.js `StreamableHTTPServerTransport` →
Spring AI `McpSyncClient` (client) + `spring-ai-starter-mcp-server-webmvc` (server).

### JWE encryption (authlib → Nimbus JOSE)
- MLE (RSA-OAEP-256 + A256GCM): `RSAEncrypter` / `RSADecrypter`
- Field-level (A256GCMKW + A256GCM): `AESEncrypter`
- JWT unverified claims: `SignedJWT.parse().getPayload().toJSONObject()`

### HMAC-SHA256 signing
`hmac.new(secret, payload, sha256).hexdigest()` → `javax.crypto.Mac` HmacSHA256 + `Hex.encodeHexString()`.

### SQLite → H2
Both are embedded file-based databases.  H2 in file mode (`jdbc:h2:file:./data/…`)
provides identical behaviour for the reference implementation.
For production, swap to PostgreSQL by changing the datasource URL and adding the driver.
