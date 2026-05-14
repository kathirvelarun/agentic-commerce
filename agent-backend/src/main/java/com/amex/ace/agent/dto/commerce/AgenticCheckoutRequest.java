package com.amex.ace.agent.dto.commerce;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public record AgenticCheckoutRequest(
        @NotBlank String provisionedTokenId,
        @NotNull  Double amount,
        @NotBlank String currencyCode,
        @NotNull  AssuranceData assuranceData,
                  String prompt,
                  String clientDeviceId,
                  String ip,
                  String userAgent,
                  VicDeviceData deviceInfo
) {}
