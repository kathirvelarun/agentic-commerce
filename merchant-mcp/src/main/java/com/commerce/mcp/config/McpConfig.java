package com.commerce.mcp.config;

import com.commerce.mcp.tool.MerchantTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class McpConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Exposes all @Tool methods as a ToolCallbackProvider bean.
     * Spring AI MCP server autoconfiguration detects this bean type
     * and registers the tools in the MCP tool listing (sent to clients
     * during the SSE handshake initialize → tools/list exchange).
     *
     * MethodToolCallbackProvider implements ToolCallbackProvider,
     * so the MCP server's McpServerAutoConfiguration picks it up automatically.
     */
    @Bean
    public ToolCallbackProvider merchantToolCallbackProvider(MerchantTools merchantTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(merchantTools)
                .build();
    }
}
