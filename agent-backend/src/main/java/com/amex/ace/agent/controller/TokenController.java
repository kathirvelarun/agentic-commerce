package com.amex.ace.agent.controller;

import com.amex.ace.agent.dto.cards.CardResponse;
import com.amex.ace.agent.dto.cards.ProvisionTokenRequest;
import com.amex.ace.agent.dto.commerce.EnrollRequest;
import com.amex.ace.agent.service.CardService;
import com.amex.ace.agent.service.CommerceService;
import com.amex.ace.agent.util.Base64UrlUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Token management endpoints.
 * Maps Python src/api/routes/tokens.py router.
 */
@RestController
@RequestMapping("/tokens")
public class TokenController {

    private final CardService cardService;
    private final CommerceService commerceService;

    @Autowired
    public TokenController(CardService cardService, CommerceService commerceService) {
        this.cardService     = cardService;
        this.commerceService = commerceService;
    }

    /** POST /tokens — provision a VTS token for the given card ID. */
    @PostMapping
    public CardResponse provisionToken(@Valid @RequestBody ProvisionTokenRequest request) {
        return cardService.provisionToken(request);
    }

    /** POST /tokens/enroll — enroll a token in VIC. */
    @PostMapping("/enroll")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enrollToken(@Valid @RequestBody EnrollRequest request) {
        EnrollRequest finalRequest = request;
        if (request.userAgent() != null && !request.userAgent().isBlank()) {
            finalRequest = new EnrollRequest(
                    request.provisionedTokenId(),
                    request.clientDeviceId(),
                    request.ip(),
                    Base64UrlUtil.encode(request.userAgent()),
                    request.deviceInfo());
        }
        commerceService.enrollToken(finalRequest);
    }
}
