package com.amex.ace.agent.service;

import com.amex.ace.agent.dto.commerce.Credentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MCP elicitation handler — provides card credentials when the merchant-mcp
 * checkout_cart tool triggers an elicitation request for payment information.
 *
 * Equivalent to Python agent.py on_elicitation() callback registered via:
 *   ClientSession(read, write, elicitation_callback=on_elicitation)
 *
 * Note: MCP integration is currently disabled, so this handler does nothing.
 */
@Component
@Slf4j
public class ElicitationHandler {

    private volatile Credentials currentCredentials;

    public void setCredentials(Credentials credentials) {
        this.currentCredentials = credentials;
    }

    public void clearCredentials() {
        this.currentCredentials = null;
    }

    // Since MCP is disabled, no elicitation is performed
}
