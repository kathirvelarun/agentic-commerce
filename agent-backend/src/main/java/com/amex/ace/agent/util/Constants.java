package com.amex.ace.agent.util;

/**
 * Application-wide constants — direct translation of Python utils/constants.py.
 */
public final class Constants {

    private Constants() {}

    // ── User / Agent Identity ─────────────────────────────────────────────────
    public static final String USER_ID    = "66d4743e-d13b-4f03-a664-6598cbbb8ee0";
    public static final String USER_EMAIL = "test@visa.com";
    public static final String AGENT_ID   = "visaagent";
    public static final String AGENT_NAME = "VisaAgent";
    public static final String AGENT_URL  = "https://visa.com";
    public static final String HASH_KEY   = "some_hash_key";

    // ── VDP Client Request ────────────────────────────────────────────────────
    public static final String DEFAULT_LOCALE        = "en_US";
    public static final String DEFAULT_COUNTRY_CODE  = "US";
    public static final String DEFAULT_LANGUAGE_CODE = "en";
    public static final String DEFAULT_CURRENCY_CODE = "840";        // USD
    public static final String DEFAULT_MERCHANT_URL  = "http://localhost:8000";

    // ── VTS Token Provisioning ────────────────────────────────────────────────
    public static final String PAN_SOURCE_MANUAL          = "MANUALLYENTERED";
    public static final String CONSUMER_ENTRY_MODE_KEY    = "KEYENTERED";
    public static final String PRESENTATION_TYPE_AI_AGENT = "AI_AGENT";
    public static final String PROTECTION_TYPE_CLOUD      = "CLOUD";
    public static final String ACCOUNT_TYPE_WALLET        = "WALLET";

    // ── VTS Device Binding ────────────────────────────────────────────────────
    public static final String INTENT_FIDO      = "FIDO";
    public static final String PLATFORM_TYPE_WEB = "WEB";

    // ── VTS Attestation ───────────────────────────────────────────────────────
    public static final String   ATTESTATION_TYPE_REGISTER     = "REGISTER";
    public static final String   ATTESTATION_TYPE_AUTHENTICATE = "AUTHENTICATE";
    public static final String   REASON_CODE_DEVICE_BINDING    = "DEVICE_BINDING";
    public static final String   REASON_CODE_PAYMENT           = "PAYMENT";

    // ── VIC Enrollment ────────────────────────────────────────────────────────
    public static final String ENROLLMENT_REFERENCE_TYPE_TOKEN     = "TOKEN_REFERENCE_ID";
    public static final String ENROLLMENT_REFERENCE_PROVIDER_VTS   = "VTS";

    // ── Time ──────────────────────────────────────────────────────────────────
    public static final long THREE_DAYS_SECONDS      = 3L * 86_400L;  // 259200 s
    public static final int  REQUEST_TIMEOUT_SECONDS = 60;
    public static final int  CACHE_CONTROL_MAX_AGE   = 3600;

    // ── Update Reason ─────────────────────────────────────────────────────────
    public static final String UPDATE_REASON_CUSTOMER_CONFIRMED = "CUSTOMER_CONFIRMED";

    // ── Assurance Data ────────────────────────────────────────────────────────
    public static final String   VERIFICATION_TYPE_DEVICE   = "DEVICE";
    public static final String   VERIFICATION_ENTITY_ID     = "10";
    public static final String[] VERIFICATION_EVENT_CODES   = {"01", "02"};
    public static final String   VERIFICATION_METHOD_CODE   = "23";
    public static final String   VERIFICATION_RESULT_SUCCESS = "01";

    // ── Transaction ───────────────────────────────────────────────────────────
    public static final String TRANSACTION_TYPE_PURCHASE   = "PURCHASE";
    public static final String TRANSACTION_STATUS_APPROVED = "APPROVED";
    public static final String TRANSACTION_STATUS_DECLINED = "DECLINED";
    public static final String DEFAULT_INTENT_DESCRIPTION  = "Agentic Intent";

    // ── Device ────────────────────────────────────────────────────────────────
    public static final String DEVICE_TYPE_DESKTOP = "Desktop";
    public static final String DEVICE_BRAND_UNKNOWN = "Unknown";
}
