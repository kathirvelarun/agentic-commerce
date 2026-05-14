package com.amex.ace.agent.util;

import java.util.Base64;

/**
 * URL-safe Base64 encoding without padding.
 * Equivalent to Python utils/encoder.py base64url_encode().
 */
public final class Base64UrlUtil {

    private Base64UrlUtil() {}

    public static String encode(String data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public static String encodeBytes(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
