package com.amex.ace.agent.dto.commerce;
public record PurchaseSummary(String merchant, String overallAmount,
                              String orderId, String trackingCode) {}
