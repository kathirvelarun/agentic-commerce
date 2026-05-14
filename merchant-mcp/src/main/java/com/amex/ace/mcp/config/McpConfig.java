package com.amex.ace.mcp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configures the RestClient that calls the merchant-backend REST API.
 * Equivalent to Node.js:  axios.create({ baseURL: `${API_BASE_URL}/api` })
 */
@Configuration
public class McpConfig {

    @Value("${api.base-url:http://localhost:8001}")
    private String apiBaseUrl;

    @Bean
    public RestClient merchantApiClient() {
        return RestClient.builder()
                .baseUrl(apiBaseUrl + "/api")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept",       "application/json")
                .build();
    }
}
