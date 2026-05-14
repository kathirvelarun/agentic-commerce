package com.amex.ace.agent.dto.commerce;
import jakarta.validation.constraints.NotBlank;
public record EnrollRequest(@NotBlank String provisionedTokenId,
                            String clientDeviceId, String ip,
                            String userAgent, VicDeviceData deviceInfo) {}
