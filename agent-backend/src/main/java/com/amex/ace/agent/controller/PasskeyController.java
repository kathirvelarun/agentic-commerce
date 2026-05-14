package com.amex.ace.agent.controller;

import com.amex.ace.agent.dto.passkey.*;
import com.amex.ace.agent.dto.passkey.*;
import com.amex.ace.agent.service.PasskeyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Passkey / FIDO2 endpoints.
 * Maps Python src/api/routes/passkey.py router.
 */
@RestController
@RequestMapping("/passkey")
public class PasskeyController {

    private final PasskeyService passkeyService;

    @Autowired
    public PasskeyController(PasskeyService passkeyService) {
        this.passkeyService = passkeyService;
    }

    @PostMapping("/attestation-options/authenticate")
    public AuthenticationContext attestationOptionsAuthenticate(
            @Valid @RequestBody AttestationOptionsAuthenticateRequest request) {
        return passkeyService.attestationOptionsAuthenticate(request);
    }

    @PostMapping("/attestation-options/register")
    public AuthenticationContext attestationOptionsRegister(
            @Valid @RequestBody AttestationOptionsRegisterRequest request) {
        return passkeyService.attestationOptionsRegister(request);
    }

    @PostMapping("/device-binding")
    public Map<String, Object> deviceBinding(
            @Valid @RequestBody DeviceBindingRequest request) {
        return passkeyService.deviceBinding(request);
    }

    @PostMapping("/create-challenge")
    public Map<String, Object> createChallenge(
            @Valid @RequestBody CreateChallengeRequest request) {
        return passkeyService.createChallenge(request);
    }

    @PostMapping("/solve-challenge")
    public Map<String, Object> solveChallenge(
            @Valid @RequestBody SolveChallengeRequest request) {
        return passkeyService.solveChallenge(request);
    }
}
