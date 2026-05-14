package com.amex.ace.agent.dto.passkey;
public record BrowserData(boolean browserJavaEnabled,
                          boolean browserJavascriptEnabled,
                          String browserHeader,
                          String browserLanguage,
                          String browserColorDepth,
                          String browserScreenHeight,
                          String browserScreenWidth,
                          String browserTimeZone,
                          String userAgent,
                          String ipAddress) {}
