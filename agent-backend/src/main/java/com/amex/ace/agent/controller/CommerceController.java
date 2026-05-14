package com.amex.ace.agent.controller;

import com.amex.ace.agent.dto.commerce.AgenticCheckoutRequest;
import com.amex.ace.agent.dto.commerce.AgenticCheckoutResponse;
import com.amex.ace.agent.service.CommerceService;
import com.amex.ace.agent.util.Base64UrlUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Commerce / intent endpoints.
 * Maps Python src/api/routes/commerce.py router.
 */
@RestController
@RequestMapping("/intents")
public class CommerceController {

    private final CommerceService commerceService;

    @Autowired
    public CommerceController(CommerceService commerceService) {
        this.commerceService = commerceService;
    }

    /** POST /intents/agent — authorize an agentic intent and complete checkout. */
    @PostMapping("/agent")
    public AgenticCheckoutResponse handleAgenticCheckout(
            @Valid @RequestBody AgenticCheckoutRequest request) {
        // Base64-URL encode the user-agent string, matching Python:
        //   if agentic_checkout_request.user_agent:
        //       agentic_checkout_request.user_agent = base64url_encode(...)
        if (request.userAgent() != null && !request.userAgent().isBlank()) {
            request = new AgenticCheckoutRequest(
                    request.provisionedTokenId(),
                    request.amount(),
                    request.currencyCode(),
                    request.assuranceData(),
                    request.prompt(),
                    request.clientDeviceId(),
                    request.ip(),
                    Base64UrlUtil.encode(request.userAgent()),
                    request.deviceInfo());
        }
        return commerceService.handleAgenticCheckout(request);
    }
}
