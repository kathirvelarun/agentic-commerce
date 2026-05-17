package com.commerce.agent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class AgentConfig {

    /**
     * Build a SyncMcpToolCallbackProvider from all McpSyncClient beans
     * auto-configured by spring-ai-starter-mcp-client.
     *
     * We expose the resolved ToolCallback[] as a named bean — the service
     * injects this array directly so it never references ToolCallbackProvider
     * at compile time (avoids the class-not-found issue).
     *
     * IMPORTANT: initialized-on-start must be true in application.yml so the
     * MCP client handshakes with the server at startup and populates the tool list.
     * With false, getToolCallbacks() returns an empty array every time.
     */
    @Bean(name = "resolvedMcpToolCallbacks")
    public ToolCallback[] resolvedMcpToolCallbacks(List<McpSyncClient> mcpSyncClients) {
        ToolCallback[] callbacks = new SyncMcpToolCallbackProvider(mcpSyncClients)
                .getToolCallbacks();
        System.out.println(">>> MCP tools resolved: " + callbacks.length);
        for (ToolCallback cb : callbacks) {
            System.out.println("    tool: " + cb.getToolDefinition().name());
        }
        return callbacks;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
