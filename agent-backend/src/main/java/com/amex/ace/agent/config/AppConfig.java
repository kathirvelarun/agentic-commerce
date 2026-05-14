package com.amex.ace.agent.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Application configuration loaded from environment variables / application.properties.
 * Maps 1:1 to the Python config.py Settings class.
 *
 * Environment variables are bound directly (e.g. LLM_API_KEY).
 */
@ConfigurationProperties
@Validated
public record AppConfig(

        // ── LLM settings ─────────────────────────────────────────────────────
        @NotBlank String llmProvider,
        @NotBlank String llmApiKey,
        @NotBlank String llmModel,
                  String llmBaseUrl,

        // ── Visa Token Service (VTS) ─────────────────────────────────────────
        String vtsBaseUrl,
        String vtsApiKey,
        String vtsSharedSecret,

        // ── Visa Intelligent Commerce (VIC) ──────────────────────────────────
        String vicBaseUrl,
        String vicApiKey,
        String vicSharedSecret,

        // ── Token Registration credentials ───────────────────────────────────
        String trId,
        String trClientId,
        String trAppId,
        String trEncApiKey,
        String trEncSharedSecret,

        // ── Message Level Encryption (MLE) ───────────────────────────────────
        String mleEncCert,
        String mleKeyId,
        String mleDecKey,

        // ── Merchant MCP URL ─────────────────────────────────────────────────
        String merchantMcpUrl

) {
    public AppConfig {
        llmProvider = llmProvider != null ? llmProvider : "openai";
        llmApiKey = llmApiKey != null ? llmApiKey : "dummy-key";
        llmModel = llmModel != null ? llmModel : "gpt-4";
        llmBaseUrl = llmBaseUrl != null ? llmBaseUrl : "https://api.openai.com/v1";

        // Provide dummy defaults for POC to avoid requiring Visa configs
        vtsBaseUrl = vtsBaseUrl != null ? vtsBaseUrl : "https://cert.api.visa.com";
        vtsApiKey = vtsApiKey != null ? vtsApiKey : "dummy-vts-key";
        vtsSharedSecret = vtsSharedSecret != null ? vtsSharedSecret : "dummy-vts-secret";
        vicBaseUrl = vicBaseUrl != null ? vicBaseUrl : "https://cert.api.visa.com";
        vicApiKey = vicApiKey != null ? vicApiKey : "dummy-vic-key";
        vicSharedSecret = vicSharedSecret != null ? vicSharedSecret : "dummy-vic-secret";
        trId = trId != null ? trId : "dummy-tr-id";
        trClientId = trClientId != null ? trClientId : "dummy-tr-client-id";
        trAppId = trAppId != null ? trAppId : "dummy-tr-app-id";
        trEncApiKey = trEncApiKey != null ? trEncApiKey : "dummy-tr-enc-key";
        trEncSharedSecret = trEncSharedSecret != null ? trEncSharedSecret : "dummy-tr-enc-secret";
        mleEncCert = mleEncCert != null ? mleEncCert : "dummy-mle-cert";
        mleKeyId = mleKeyId != null ? mleKeyId : "dummy-mle-key-id";
        mleDecKey = mleDecKey != null ? mleDecKey : "dummy-mle-dec-key";
    }

    /** Provides default value for merchantMcpUrl when not configured. */
    public String merchantMcpUrl() {
        return merchantMcpUrl != null ? merchantMcpUrl : "http://localhost:8002/mcp";
    }
}
