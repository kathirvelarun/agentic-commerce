package com.amex.ace.agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI configuration.
 *
 * Replaces the Python lifespan() context manager in agent.py:
 *   - streamablehttp_client  → McpSyncClient with HttpClientSseClientTransport
 *   - load_mcp_tools()       → SyncMcpToolCallbackProvider
 *   - init_chat_model()      → OpenAiChatModel bean
 *   - create_agent()         → ChatClient with defaultToolCallbacks + memory advisor
 */
@Configuration
public class AiConfig {

    @Autowired
    private AppConfig appConfig;

    /**
     * Chat model bean.  Supports OpenAI-compatible endpoints (Anthropic, Ollama, etc.)
     * via llmBaseUrl override, matching Python's init_chat_model() flexibility.
     */
    @Bean
    public ChatModel chatModel() {
        var apiBuilder = OpenAiApi.builder()
                .apiKey(appConfig.llmApiKey());
        if (appConfig.llmBaseUrl() != null && !appConfig.llmBaseUrl().isBlank()) {
            apiBuilder.baseUrl(appConfig.llmBaseUrl());
        }
        var options = OpenAiChatOptions.builder()
                .model(appConfig.llmModel())
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(apiBuilder.build())
                .defaultOptions(options)
                .build();
    }

    /**
     * ChatClient — the Spring AI equivalent of LangChain create_agent() + LangGraph.
     * Wires together: model, system prompt, MCP tools, and in-memory conversation memory.
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(AgentPrompt.SYSTEM_PROMPT)
                .build();
    }
}
