# OpenAI API Setup Guide

This document provides instructions to fix OpenAI API errors including `RestClientException`, `NonTransientAiException`, and 404 errors.

## Problem

```
org.springframework.ai.retry.NonTransientAiException: HTTP 404 - No response body available
```

or

```
org.springframework.web.client.RestClientException: Error while extracting response 
for type [org.springframework.ai.openai.api.OpenAiApi$ChatCompletion]
```

These errors indicate:
1. **Invalid base URL** - Double `/v1` path (API + Spring AI both adding it)
2. **Invalid model name** - Model doesn't exist or isn't available in your region
3. **Missing or invalid API key** (401 error)
4. **Account quota/rate limit issues** (429 error)
5. **Malformed configuration** - Endpoint path incorrect

## Solution

### 1. Verify OpenAI API Configuration

Check that your `application.yml` has the correct settings:

```yaml
spring.ai.openai:
  api-key: ${OPENAI_API_KEY}           # Required environment variable
  base-url: https://api.openai.com     # WITHOUT /v1 (Spring AI adds it)
  chat.options.model: gpt-4-turbo      # Use a widely available model
  timeout: PT60S                        # 60 second timeout
```

**Key Points:**
- ✅ Base URL: `https://api.openai.com` (NOT `https://api.openai.com/v1`)
- ✅ Model: Use `gpt-4-turbo` or `gpt-3.5-turbo` (widely available)
- ✅ API Key: Set via `OPENAI_API_KEY` environment variable

### 2. Set Your OpenAI API Key (Required)

```bash
# Set the environment variable
export OPENAI_API_KEY="sk-your-actual-api-key-here"

# Or add to your shell profile (~/.zshrc or ~/.bash_profile)
echo 'export OPENAI_API_KEY="sk-your-actual-api-key-here"' >> ~/.zshrc

# Verify it's set
echo $OPENAI_API_KEY
```

### 3. Choose the Right Model

Supported models and their availability:

| Model | Status | Notes |
|-------|--------|-------|
| `gpt-4-turbo` | ✅ Recommended | Latest GPT-4 with 128K context, widely available |
| `gpt-3.5-turbo` | ✅ Available | Faster, cheaper, good for most tasks |
| `gpt-4` | ⚠️ Check Availability | May not be available in all regions |
| `gpt-4o-mini` | ⚠️ Check Availability | Newer model, not available in all regions |

For this project, we recommend **`gpt-4-turbo`** as it's:
- Widely available across all regions
- Compatible with Spring AI 1.0.0
- Cost-effective
- Supports all required features (vision, function calling, JSON mode)

### 4. Verify Your API Key

Get a valid API key:
1. Go to https://platform.openai.com/api-keys
2. Create a new secret key
3. Copy and save it securely
4. Ensure your account has sufficient credits at https://platform.openai.com/account/billing/overview

### 5. Run the Application

```bash
# With environment variable
export OPENAI_API_KEY="sk-your-actual-api-key-here"
mvn spring-boot:run

# Or pass it directly
mvn spring-boot:run -DOPENAI_API_KEY="sk-your-actual-api-key-here"
```

### 6. Test the API

Once running, test with curl:

```bash
curl -X POST http://localhost:8000/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello, can you help me find a laptop?",
    "sessionId": "test-session-1"
  }'
```

Expected response:
```json
{
  "sessionId": "test-session-1",
  "response": "I'd be happy to help you find a laptop...",
  "role": "assistant",
  "timestamp": "2026-05-15T...",
  "intent": "SEARCH"
}
```

## Configuration Details

### Updated Configuration (application.yml)

The application now uses:

```yaml
spring.ai.openai:
  api-key: ${OPENAI_API_KEY}           # Required - no default
  base-url: https://api.openai.com     # Spring AI appends /v1 automatically
  chat.options.model: gpt-4-turbo      # Stable, widely available model
  chat.options.temperature: 0.7        # Balanced creativity
  chat.options.max-tokens: 2048        # Response size limit
  timeout: PT60S                        # 60 second timeout
```

### Spring AI Configuration

Spring AI automatically handles:
- Adding `/v1` to the base URL
- Formatting requests in OpenAI API format
- Deserializing responses
- Handling retries for transient errors

### Connection and Timeout Settings

Configured with:
- **Connect timeout**: 30 seconds (initial connection)
- **Read timeout**: 60 seconds (waiting for response)
- **Request factory**: Buffering client for full error diagnostics

## Error Messages and Solutions

### "HTTP 404 - No response body available"
**Cause**: Model doesn't exist, wrong endpoint, or configuration error
**Solution**:
1. Verify base URL is `https://api.openai.com` (NOT `/v1`)
2. Check model name: `gpt-4-turbo` or `gpt-3.5-turbo`
3. Enable DEBUG logging and check logs
4. Verify API key is valid

### "Authentication failed" (401)
**Cause**: API key is missing, invalid, or expired
**Solution**: 
1. Check that `OPENAI_API_KEY` environment variable is set: `echo $OPENAI_API_KEY`
2. Verify the key at https://platform.openai.com/api-keys
3. Ensure the key hasn't been revoked or rotated
4. Restart the application after setting the variable

### "Rate limit exceeded" (429)
**Cause**: Too many requests to OpenAI API
**Solution**:
1. Implement exponential backoff retry logic
2. Check rate limits at https://platform.openai.com/account/rate-limits
3. Upgrade your OpenAI plan if needed
4. Reduce concurrent requests

### "Failed to parse OpenAI response" or "Error while extracting response for type ChatCompletion"
**Cause**: API returned an error response instead of ChatCompletion (usually due to invalid API key)
**Solution**: 
1. **Verify API key is valid**: `echo $OPENAI_API_KEY`
   - Must start with `sk-` and be 48+ characters
   - Check at https://platform.openai.com/api-keys
2. **Verify API key is set correctly**:
   - Restart terminal/IDE after setting variable
   - Run: `export OPENAI_API_KEY="sk-your-key"` before starting app
3. **Check OpenAI status**: https://status.openai.com/
4. **Enable DEBUG logging** and check full error messages
5. **Verify account has credits**: https://platform.openai.com/account/billing/overview

## Debugging

### Enable Debug Logging

Add to `application.yml`:

```yaml
logging:
  level:
    org.springframework.ai: DEBUG
    org.springframework.web.client: DEBUG
    com.commerce.agent: DEBUG
    com.fasterxml.jackson: DEBUG
```

### Test OpenAI API Directly

Verify your API key and connectivity:

```bash
# Test with curl
curl -X POST https://api.openai.com/v1/chat/completions \
  -H "Authorization: Bearer $OPENAI_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-4-turbo",
    "messages": [{"role": "user", "content": "Hello"}],
    "max_tokens": 10
  }' | jq .
```

**Expected response**: A JSON object with `choices` and `message` fields
**If 401 error**: Your API key is invalid or not set
**If 404 error**: Check base URL and model name
**If error response**: Check OpenAI status and account credits

### View Full Error Details

Run with this command to see detailed logs:

```bash
export OPENAI_API_KEY="sk-your-api-key"
mvn spring-boot:run 2>&1 | grep -E "(ERROR|WARN|ChatCompletion|HttpMessage|JsonMapping)" | head -50
```

## Diagnosing Deserialization Errors

If you see: **"Error while extracting response for type ChatCompletion"**

This means OpenAI API returned a response that couldn't be parsed as ChatCompletion. Causes:

1. **Invalid API Key (Most Common)**
   ```bash
   # Check if key is set
   echo $OPENAI_API_KEY
   
   # Test directly
   curl -H "Authorization: Bearer $OPENAI_API_KEY" \
     https://api.openai.com/v1/models
   
   # Should return models list, not 401/403
   ```

2. **Wrong Configuration**
   ```bash
   # Verify base URL
   grep "base-url:" agent-backend/src/main/resources/application.yml
   # Should be: https://api.openai.com (NOT /v1)
   
   # Verify model
   grep "model:" agent-backend/src/main/resources/application.yml
   # Should be: gpt-4-turbo or gpt-3.5-turbo
   ```

3. **Account Issues**
   - No credits: https://platform.openai.com/account/billing/overview
   - No organization: https://platform.openai.com/account/organization/overview
   - API disabled: https://platform.openai.com/account/api-keys

4. **OpenAI API Status**
   - Check: https://status.openai.com/
export OPENAI_API_KEY="sk-your-api-key"
mvn spring-boot:run | grep -E "(ERROR|WARN|DEBUG.*openai)" | head -50
```

### Test OpenAI Connectivity

Verify you can reach OpenAI API:

```bash
curl -H "Authorization: Bearer $OPENAI_API_KEY" \
  https://api.openai.com/v1/models
```

Should return a list of available models.

## Testing Without OpenAI (Optional)

For development, you could mock the OpenAI client:

```java
@MockBean
private ChatClient.Builder chatClientBuilder;

@BeforeEach
void setUp() {
    ChatClient mockClient = mock(ChatClient.class);
    when(chatClientBuilder.build()).thenReturn(mockClient);
    // ... configure mock responses
}
```

## Common Issues and Fixes

| Issue | Cause | Fix |
|-------|-------|-----|
| 404 error | Base URL has `/v1` | Remove `/v1` from base URL |
| 404 error | Invalid model name | Use `gpt-4-turbo` or `gpt-3.5-turbo` |
| 401 error | Missing API key | Set `OPENAI_API_KEY` environment variable |
| 429 error | Rate limited | Wait or upgrade plan |
| Empty response | API returned nothing | Check logs, verify configuration |
| Connection timeout | Network issue | Check internet, verify firewall |

## Reference

- Spring AI Docs: https://docs.spring.io/spring-ai/reference/
- OpenAI API: https://platform.openai.com/docs/api-reference
- OpenAI Status: https://status.openai.com/
- OpenAI API Keys: https://platform.openai.com/api-keys
- Usage & Limits: https://platform.openai.com/account/billing/overview

