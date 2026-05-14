package com.amex.ace.agent.service;

import com.amex.ace.agent.dto.passkey.*;
import com.amex.ace.agent.dto.passkey.*;
import com.amex.ace.agent.service.vdp.VdpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Passkey / FIDO2 service.
 * Direct translation of Python src/services/passkey.py PasskeyService.
 */
@Service
public class PasskeyService {

    private final VdpClient vdpClient;

    @Autowired
    public PasskeyService(VdpClient vdpClient) {
        this.vdpClient = vdpClient;
    }

    public AuthenticationContext attestationOptionsAuthenticate(
            AttestationOptionsAuthenticateRequest request) {
        Map<String, Object> ctx = vdpClient.attestationOptionsAuthenticate(
                request.clientReferenceId().toString(),
                request.provisionedTokenId(),
                request.sessionContext(),
                request.browserData(),
                request.amount()       != null ? request.amount()       : "0",
                request.currencyCode() != null ? request.currencyCode() : "840",
                request.prompt());
        return new AuthenticationContext(ctx);
    }

    public Map<String, Object> deviceBinding(DeviceBindingRequest request) {
        return vdpClient.deviceBinding(
                request.clientReferenceId().toString(),
                request.provisionedTokenId(),
                request.expMonth(),
                request.expYear(),
                request.sessionContext(),
                request.browserData());
    }

    public Map<String, Object> createChallenge(CreateChallengeRequest request) {
        return vdpClient.createChallenge(
                request.clientReferenceId().toString(),
                request.provisionedTokenId(),
                request.identifier());
    }

    public Map<String, Object> solveChallenge(SolveChallengeRequest request) {
        return vdpClient.solveChallenge(
                request.clientReferenceId().toString(),
                request.provisionedTokenId(),
                request.code());
    }

    public AuthenticationContext attestationOptionsRegister(
            AttestationOptionsRegisterRequest request) {
        Map<String, Object> ctx = vdpClient.attestationOptionsRegister(
                request.clientReferenceId().toString(),
                request.provisionedTokenId(),
                request.sessionContext(),
                request.browserData());
        return new AuthenticationContext(ctx);
    }
}
