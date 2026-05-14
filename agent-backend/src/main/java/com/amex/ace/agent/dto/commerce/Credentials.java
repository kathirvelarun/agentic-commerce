package com.amex.ace.agent.dto.commerce;
public record Credentials(String cardNumber, String expMonth,
                          String expYear, String cvv) {}
