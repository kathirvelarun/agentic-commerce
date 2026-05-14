package com.amex.ace.agent.service.vdp;

import com.amex.ace.agent.dto.passkey.BrowserData;
import com.amex.ace.agent.dto.passkey.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.amex.ace.agent.config.AppConfig;
import com.amex.ace.agent.dto.commerce.AssuranceData;
import com.amex.ace.agent.dto.commerce.TransactionData;
import com.amex.ace.agent.dto.commerce.VicDeviceData;
import com.amex.ace.agent.util.Base64UrlUtil;
import com.amex.ace.agent.util.Constants;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * Visa Developer Platform (VDP) HTTP client.
 * Direct translation of Python src/services/vdp_client.py VDPClient class.
 *
 * Responsibilities:
 *  - HMAC-SHA256 request signing (xv2 token scheme)
 *  - JWE MLE encryption/decryption (RSA-OAEP-256 + A256GCM)
 *  - JWE field-level encryption (A256GCMKW + A256GCM)
 *  - All VTS + VIC API method calls
 *  - Per-request logging (appended to JSON responses by VdpLoggingAdvice)
 */
@Component
@RequestScope
@Slf4j
public class VdpClient {

    private final AppConfig cfg;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final List<Map<String, Object>> requestLogs = new ArrayList<>();

    private RSAKey mleEncCert;
    private OctetSequenceKey mleDecKey;

    @Autowired
    public VdpClient(AppConfig cfg, ObjectMapper objectMapper) {
        this.cfg = cfg;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @PostConstruct
    void init() {
        // For POC, skip MLE key parsing since we use dummy values
        log.info("Skipping MLE key initialization for POC mode");
        // try {
        //     mleEncCert = RSAKey.parse(cfg.mleEncCert());
        //     mleDecKey  = OctetSequenceKey.parse(cfg.mleDecKey());
        // } catch (Exception e) {
        //     throw new IllegalStateException("Failed to parse MLE keys", e);
        // }
    }

    // =========================================================================
    // Core HTTP request dispatcher
    // =========================================================================

    @SuppressWarnings("unchecked")
    public Map<String, Object> makeRequest(String method, String path,
                                           Object body, boolean messageEncrypted) {
        // For POC, return mock responses instead of making real VDP API calls
        log.info("Mock VDP request: {} {} with body: {}", method, path, body);
        return getMockResponse(path);
    }

    private Map<String, Object> getMockResponse(String path) {
        if (path.contains("/vts/panEnrollments")) {
            if (path.endsWith("/panEnrollments")) {
                // Enroll PAN
                return Map.of(
                        "panEnrollmentId", "mock-pan-enrollment-id-" + UUID.randomUUID(),
                        "status", "ENROLLED"
                );
            } else if (path.contains("/provisionedTokens")) {
                // Provision Token
                return Map.of(
                        "vProvisionedTokenID", "mock-provisioned-token-" + UUID.randomUUID(),
                        "tokenInfo", Map.of(
                                "vTokenID", "mock-vtoken-" + UUID.randomUUID(),
                                "lastFour", "1234",
                                "cardBrand", "Visa"
                        )
                );
            } else {
                // Get Card Metadata
                return Map.of(
                        "cardMetaData", Map.of(
                                "cardArtId", "mock-card-art-id",
                                "issuerName", "Mock Bank"
                        )
                );
            }
        } else if (path.contains("/vts/cps/getContent")) {
            // Get Card Art
            return Map.of(
                    "content", List.of(Map.of(
                            "cardArtUrl", "https://example.com/mock-card-art.png"
                    ))
            );
        } else if (path.contains("/vts/provisionedTokens")) {
            if (path.contains("/deviceBinding")) {
                // Device Binding
                return Map.of("status", "SUCCESS");
            } else if (path.contains("/stepUpOptions")) {
                if (path.contains("/method")) {
                    // Create Challenge
                    return Map.of("challengeId", "mock-challenge-" + UUID.randomUUID());
                } else {
                    // Solve Challenge
                    return Map.of("status", "VERIFIED");
                }
            } else if (path.contains("/attestation/options")) {
                // Attestation Options
                return Map.of(
                        "authenticationContext", Map.of(
                                "challenge", "mock-challenge-data",
                                "rpId", "example.com"
                        )
                );
            }
        } else if (path.startsWith("/vacp/")) {
            if (path.contains("/cards")) {
                // Enroll Card
                return Map.of(
                        "cardId", "mock-card-id-" + UUID.randomUUID(),
                        "status", "ENROLLED"
                );
            } else if (path.contains("/instructions")) {
                if (path.contains("/credentials")) {
                    // Retrieve Credentials
                    return Map.of(
                            "signedPayload", "mock-jwt-payload",
                            "status", "SUCCESS",
                            "credentials", Map.of(
                                    "cardNumber", "4111111111111111",
                                    "expiryMonth", "12",
                                    "expiryYear", "2025"
                            )
                    );
                } else if (path.contains("/confirmations")) {
                    // Confirm Transaction
                    return Map.of("status", "CONFIRMED");
                } else {
                    // Create Intent
                    return Map.of(
                            "instructionId", "mock-instruction-id-" + UUID.randomUUID(),
                            "status", "CREATED"
                    );
                }
            }
        }
        // Default mock response
        return Map.of("status", "MOCK_SUCCESS", "message", "Mock response for " + path);
    }

    public List<Map<String, Object>> getLogs() {
        return Collections.unmodifiableList(requestLogs);
    }

    // =========================================================================
    // Authentication helpers
    // =========================================================================

    private String computeXPayToken(String sharedSecret, String resourcePath,
                                    String queryString, String body) {
        long timestamp  = Instant.now().getEpochSecond();
        String preHash  = timestamp + resourcePath + queryString + body;
        String hash     = hmacSha256Hex(sharedSecret, preHash);
        return "xv2:" + timestamp + ":" + hash;
    }

    private String getResourcePath(String path) {
        if (path.startsWith("/vacp/")) {
            String[] parts = path.strip().split("/");
            return String.join("/", Arrays.copyOfRange(parts, 2, parts.length));
        }
        return path.replaceAll("^/|/$", "");
    }

    private String hmacSha256Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Hex.encodeHexString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 failed", e);
        }
    }

    String hmacSha256Base64Url(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 (base64url) failed", e);
        }
    }

    // =========================================================================
    // JWE helpers
    // =========================================================================

    /** MLE: RSA-OAEP-256 + A256GCM — for VIC API calls. */
    private String encryptWithCert(Object payload) {
        try {
            JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256,
                    EncryptionMethod.A256GCM)
                    .keyID(cfg.mleKeyId())
                    .customParam("iat", Instant.now().toEpochMilli())
                    .build();
            JWEObject jwe = new JWEObject(header,
                    new Payload(objectMapper.writeValueAsString(payload)));
            jwe.encrypt(new RSAEncrypter(mleEncCert.toRSAPublicKey()));
            return jwe.serialize();
        } catch (Exception e) {
            throw new RuntimeException("MLE encryption failed", e);
        }
    }

    /** MLE decryption — parses the signedPayload for retrieve_credentials. */
    @SuppressWarnings("unchecked")
    private Map<String, Object> decryptWithKey(String encryptedPayload) {
        try {
            JWEObject jwe = JWEObject.parse(encryptedPayload);
            jwe.decrypt(new RSADecrypter(mleDecKey.toRSAKey()));
            return objectMapper.readValue(jwe.getPayload().toString(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("MLE decryption failed", e);
        }
    }

    /** Field-level encryption: A256GCMKW + A256GCM — for PAN/card data. */
    String encryptWithSecret(String secret, String kid, Object payload) {
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            OctetSequenceKey key = new OctetSequenceKey.Builder(keyBytes)
                    .keyID(kid).build();
            JWEHeader header = new JWEHeader(JWEAlgorithm.A256GCMKW, EncryptionMethod.A256GCM);
            JWEObject jwe    = new JWEObject(header,
                    new Payload(objectMapper.writeValueAsString(payload)));
            jwe.encrypt(new AESEncrypter(key));
            return jwe.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Field-level encryption failed", e);
        }
    }

    // =========================================================================
    // Browser data helper
    // =========================================================================

    private Map<String, Object> processBrowserData(
            BrowserData bd) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("browserJavaEnabled",       String.valueOf(bd.browserJavaEnabled()));
        m.put("browserJavascriptEnabled", String.valueOf(bd.browserJavascriptEnabled()));
        m.put("browserHeader",            Base64UrlUtil.encode(bd.browserHeader()));
        m.put("browserLanguage",          bd.browserLanguage());
        m.put("browserColorDepth",        bd.browserColorDepth());
        m.put("browserScreenHeight",      bd.browserScreenHeight());
        m.put("browserScreenWidth",       bd.browserScreenWidth());
        m.put("browserTimeZone",          bd.browserTimeZone());
        m.put("userAgent",                Base64UrlUtil.encode(bd.userAgent()));
        m.put("ipAddress",                bd.ipAddress());
        return m;
    }

    private Map<String, Object> deviceDataMap(VicDeviceData deviceInfo) {
        if (deviceInfo != null) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type",  deviceInfo.type());
            m.put("brand", deviceInfo.brand());
            if (deviceInfo.manufacturer() != null) m.put("manufacturer", deviceInfo.manufacturer());
            if (deviceInfo.model()        != null) m.put("model",        deviceInfo.model());
            return m;
        }
        return Map.of("type", Constants.DEVICE_TYPE_DESKTOP,
                      "brand", Constants.DEVICE_BRAND_UNKNOWN);
    }

    // =========================================================================
    // VTS API Methods
    // =========================================================================

    public Map<String, Object> enrollPan(String cardNumber, String name,
                                         int expMonth, int expYear, String cvv) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("locale",               Constants.DEFAULT_LOCALE);
        body.put("clientAppID",          cfg.trAppId());
        body.put("clientWalletAccountID",cfg.trId());
        body.put("panSource",            Constants.PAN_SOURCE_MANUAL);
        body.put("consumerEntryMode",    Constants.CONSUMER_ENTRY_MODE_KEY);
        body.put("encPaymentInstrument", encryptWithSecret(cfg.trEncSharedSecret(),
                cfg.trEncApiKey(), Map.of(
                        "accountNumber",    cardNumber,
                        "name",             name,
                        "expirationDate",   Map.of("month", expMonth, "year", expYear),
                        "cvv2",             cvv)));
        Map<String, Object> resp = makeRequest("POST", "/vts/panEnrollments", body, false);
        if (resp == null) throw new RuntimeException("Empty response from Enroll PAN");
        return resp;
    }

    public Map<String, Object> provisionTokenGivenPanEnrollmentId(
            String panEnrollmentId, int expMonth, int expYear) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clientAppID",           cfg.trAppId());
        body.put("clientWalletAccountID", cfg.trId());
        body.put("clientWalletAccountEmailAddress",
                "test@example.com");
        body.put("clientWalletAccountEmailAddressHash",
                hmacSha256Base64Url(Constants.HASH_KEY, "test@example.com"));
        body.put("presentationType",  List.of(Constants.PRESENTATION_TYPE_AI_AGENT));
        body.put("protectionType",    Constants.PROTECTION_TYPE_CLOUD);
        body.put("accountType",       Constants.ACCOUNT_TYPE_WALLET);
        body.put("encRiskDataInfo",   encryptWithSecret(cfg.trEncSharedSecret(),
                cfg.trEncApiKey(), List.of(
                        Map.of("name", "paymentInstrument.expirationDate.month", "value", expMonth),
                        Map.of("name", "paymentInstrument.expirationDate.year",  "value", expYear))));

        Map<String, Object> resp = makeRequest("POST",
                "/vts/panEnrollments/" + panEnrollmentId + "/provisionedTokens", body, false);
        if (resp == null) throw new RuntimeException("Empty response from Provision Token");

        @SuppressWarnings("unchecked")
        Map<String, Object> token = new LinkedHashMap<>(
                (Map<String, Object>) resp.get("tokenInfo"));
        token.put("vProvisionedTokenID", resp.get("vProvisionedTokenID"));
        return token;
    }

    public void deprovision(String provisionedTokenId) {
        makeRequest("PUT", "/vts/provisionedTokens/" + provisionedTokenId + "/delete",
                Map.of("updateReason", Map.of("reasonCode",
                        Constants.UPDATE_REASON_CUSTOMER_CONFIRMED)), false);
    }

    public Map<String, Object> getCardMetadata(String panEnrollmentId) {
        Map<String, Object> resp = makeRequest("GET",
                "/vts/panEnrollments/" + panEnrollmentId, null, false);
        if (resp == null) throw new RuntimeException("Empty response from Get Card Metadata");
        @SuppressWarnings("unchecked")
        Map<String, Object> meta = (Map<String, Object>) resp.get("cardMetaData");
        return meta;
    }

    public Map<String, Object> getCardArt(String cardArtId) {
        Map<String, Object> resp = makeRequest("GET",
                "/vts/cps/getContent/" + cardArtId, null, false);
        if (resp == null) throw new RuntimeException("Empty response from Get Card Art");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) resp.get("content");
        return content.get(0);
    }

    public Map<String, Object> deviceBinding(String id, String provisionedTokenId,
                                              int expMonth, int expYear,
                                              SessionContext sessionContext,
                                              BrowserData browserData) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clientReferenceID",                    id);
        body.put("clientAppID",                          cfg.trAppId());
        body.put("clientWalletAccountEmailAddressHash",
                hmacSha256Base64Url(Constants.HASH_KEY, Constants.USER_EMAIL));
        body.put("intent",        Constants.INTENT_FIDO);
        body.put("platformType",  Constants.PLATFORM_TYPE_WEB);
        body.put("encBillingInfo", encryptWithSecret(cfg.trEncSharedSecret(),
                cfg.trEncApiKey(), Map.of("email", Constants.USER_EMAIL)));
        body.put("encDeviceRiskDataInfo", encryptWithSecret(cfg.trEncSharedSecret(),
                cfg.trEncApiKey(), List.of(
                        Map.of("name", "paymentInstrument.expirationDate.month", "value", expMonth),
                        Map.of("name", "paymentInstrument.expirationDate.year",  "value", expYear))));
        body.put("sessionContext", Map.of("secureToken", sessionContext.secureToken()));
        body.put("browserData",   processBrowserData(browserData));

        Map<String, Object> resp = makeRequest("POST",
                "/vts/provisionedTokens/" + provisionedTokenId + "/deviceBinding", body, false);
        if (resp == null) throw new RuntimeException("Empty response from Device Binding");
        return resp;
    }

    public Map<String, Object> createChallenge(String id, String provisionedTokenId,
                                               String identifier) {
        Map<String, Object> body = Map.of(
                "clientReferenceId", id,
                "stepUpRequestID",   identifier,
                "date",              Instant.now().getEpochSecond());
        Map<String, Object> resp = makeRequest("PUT",
                "/vts/provisionedTokens/" + provisionedTokenId + "/stepUpOptions/method",
                body, false);
        if (resp == null) throw new RuntimeException("Empty response from Create Challenge");
        return resp;
    }

    public Map<String, Object> solveChallenge(String id, String provisionedTokenId,
                                              String code) {
        Map<String, Object> body = Map.of(
                "clientReferenceId", id,
                "otpValue",          code,
                "date",              Instant.now().getEpochSecond());
        Map<String, Object> resp = makeRequest("POST",
                "/vts/provisionedTokens/" + provisionedTokenId + "/stepUpOptions/validateOTP",
                body, false);
        if (resp == null) throw new RuntimeException("Empty response from Solve Challenge");
        return resp;
    }

    public Map<String, Object> attestationOptionsRegister(
            String id, String provisionedTokenId,
            SessionContext sessionContext,
            BrowserData browserData) {
        Map<String, Object> body = buildAttestationBody(id, provisionedTokenId,
                Constants.ATTESTATION_TYPE_REGISTER, Constants.REASON_CODE_DEVICE_BINDING,
                sessionContext, browserData, "0", "840", null);
        body.put("authenticationPreferencesRequested",
                Map.of("selectedPopupForRegister", true));

        Map<String, Object> resp = makeRequest("POST",
                "/vts/provisionedTokens/" + provisionedTokenId + "/attestation/options",
                body, false);
        if (resp == null) throw new RuntimeException("Empty response from Attestation Register");
        return (Map<String, Object>) resp.get("authenticationContext");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> attestationOptionsAuthenticate(
            String id, String provisionedTokenId,
            SessionContext sessionContext,
            BrowserData browserData,
            String amount, String currencyCode, String merchants) {
        Map<String, Object> body = buildAttestationBody(id, provisionedTokenId,
                Constants.ATTESTATION_TYPE_AUTHENTICATE, Constants.REASON_CODE_PAYMENT,
                sessionContext, browserData, amount, currencyCode,
                merchants != null ? merchants : Constants.AGENT_NAME);
        body.put("authenticationPreferencesRequested",
                Map.of("selectedPopupForAuthenticate", true));

        Map<String, Object> resp = makeRequest("POST",
                "/vts/provisionedTokens/" + provisionedTokenId + "/attestation/options",
                body, false);
        if (resp == null) throw new RuntimeException("Empty response from Attestation Authenticate");
        return (Map<String, Object>) resp.get("authenticationContext");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildAttestationBody(
            String id, String provisionedTokenId,
            String type, String reasonCode,
            SessionContext sessionContext,
            BrowserData browserData,
            String amount, String currencyCode, String merchantName) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clientReferenceID", id);
        body.put("type",              type);
        body.put("reasonCode",        reasonCode);
        body.put("sessionContext",    Map.of("secureToken", sessionContext.secureToken()));
        body.put("browserData",       processBrowserData(browserData));
        body.put("dynamicData",       Map.of(
                "authenticationAmount", amount != null ? amount : "0",
                "currencyCode",         currencyCode != null ? currencyCode : "840",
                "merchantIdentifier",   Map.of(
                        "externalClientId", Base64UrlUtil.encode(Constants.AGENT_ID),
                        "applicationUrl",   Base64UrlUtil.encode(Constants.AGENT_URL),
                        "merchantName",     Base64UrlUtil.encode(
                                merchantName != null ? merchantName : Constants.AGENT_NAME))));
        body.put("encAuthenticationData", encryptWithSecret(cfg.trEncSharedSecret(),
                cfg.trEncApiKey(),
                Map.of("consumerInfo", Map.of("emailAddress", Constants.USER_EMAIL))));
        return body;
    }

    // =========================================================================
    // VIC API Methods
    // =========================================================================

    public Map<String, Object> enrollCard(String provisionedTokenId,
                                          String clientDeviceId, String ip,
                                          String userAgent, VicDeviceData deviceInfo) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clientReferenceId", Constants.USER_ID);
        body.put("client", Map.of("externalClientId", cfg.trClientId(),
                                  "externalAppId",    cfg.trAppId()));
        body.put("appInstance", buildAppInstance(userAgent, ip, clientDeviceId, deviceInfo));
        body.put("enrollmentReferenceData", Map.of(
                "enrollmentReferenceId",       provisionedTokenId,
                "enrollmentReferenceType",     Constants.ENROLLMENT_REFERENCE_TYPE_TOKEN,
                "enrollmentReferenceProvider", Constants.ENROLLMENT_REFERENCE_PROVIDER_VTS));
        body.put("consumer", Map.of(
                "consumerId",    Constants.USER_ID,
                "countryCode",   Constants.DEFAULT_COUNTRY_CODE,
                "languageCode",  Constants.DEFAULT_LANGUAGE_CODE,
                "consumerIdentity", Map.of("identityType",  "EMAIL_ADDRESS",
                                           "identityValue", Constants.USER_EMAIL)));

        Map<String, Object> resp = makeRequest("POST", "/vacp/v1/cards", body, true);
        if (resp == null) throw new RuntimeException("Empty response from Enroll Card");
        return resp;
    }

    public Map<String, Object> createIntent(String provisionedTokenId, UUID mandateId,
                                            double amount, String currencyCode,
                                            AssuranceData assuranceData,
                                            long verificationTimestamp, long expirationTimestamp,
                                            String prompt, String clientDeviceId,
                                            String ip, String userAgent,
                                            VicDeviceData deviceInfo) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clientReferenceId", Constants.USER_ID);
        body.put("client", Map.of("externalClientId", cfg.trClientId(),
                                  "externalAppId",    cfg.trAppId()));
        body.put("appInstance",       buildAppInstance(userAgent, ip, clientDeviceId, deviceInfo));
        body.put("consumerId",        Constants.USER_ID);
        body.put("tokenId",           provisionedTokenId);
        body.put("assuranceData",     List.of(Map.of(
                "verificationType",    Constants.VERIFICATION_TYPE_DEVICE,
                "verificationEntity",  Constants.VERIFICATION_ENTITY_ID,
                "verificationEvents",  Constants.VERIFICATION_EVENT_CODES,
                "verificationMethod",  Constants.VERIFICATION_METHOD_CODE,
                "verificationResults", Constants.VERIFICATION_RESULT_SUCCESS,
                "verificationTimestamp", String.valueOf(verificationTimestamp),
                "methodResults", Map.of(
                        "dfpSessionId",      "ALLOW_ME",
                        "identifier",        assuranceData.identifier(),
                        "fidoAssertionData", Map.of("code", assuranceData.fidoAssertionData().code())),
                "additionalData", "str")));
        body.put("mandates", List.of(Map.of(
                "mandateId",      mandateId.toString(),
                "description",    prompt,
                "declineThreshold", Map.of("amount", amount, "currencyCode", currencyCode),
                "effectiveUntilTime", String.valueOf(expirationTimestamp))));
        body.put("compressedPrompt", prompt);

        Map<String, Object> resp = makeRequest("POST", "/vacp/v1/instructions", body, true);
        if (resp == null) throw new RuntimeException("Empty response from Create Intent");
        return resp;
    }

    public Map<String, Object> retrieveCredentials(String provisionedTokenId,
                                                    String instructionId,
                                                    List<TransactionData> transactionDataList) {
        List<Map<String, Object>> txList = new ArrayList<>();
        for (TransactionData td : transactionDataList) {
            txList.add(Map.of(
                    "transactionReferenceId", td.transactionReferenceId().toString(),
                    "transactionAmount",      Map.of(
                            "transactionAmount",       td.transactionAmount().transactionAmount(),
                            "transactionCurrencyCode", td.transactionAmount().transactionCurrencyCode()),
                    "merchantName",           td.merchantName(),
                    "merchantCountryCode",    td.merchantCountryCode(),
                    "merchantUrl",            td.merchantUrl(),
                    "mandateReferenceData",   List.of(td.mandateId().toString())));
        }
        Map<String, Object> body = Map.of(
                "clientReferenceId", Constants.USER_ID,
                "client",            Map.of("externalClientId", cfg.trClientId(),
                                            "externalAppId",    cfg.trAppId()),
                "tokenId",           provisionedTokenId,
                "transactionData",   txList);

        Map<String, Object> resp = makeRequest("POST",
                "/vacp/v1/instructions/" + instructionId + "/credentials", body, true);
        if (resp == null) throw new RuntimeException("Empty response from Retrieve Credentials");

        // For POC mock, return the credentials directly
        if (resp.containsKey("credentials")) {
            return (Map<String, Object>) resp.get("credentials");
        }

        // Decode the signed JWT payload (unverified, matching Python python-jose usage)
        try {
            String signedPayload = (String) resp.get("signedPayload");
            JWTClaimsSet claims  = JWTClaimsSet.parse(
                    com.nimbusds.jwt.SignedJWT.parse(signedPayload).getPayload().toJSONObject());
            Map<String, Object> result = new LinkedHashMap<>(claims.getClaims());
            result.put("status", resp.get("status"));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse signed JWT payload", e);
        }
    }

    public Map<String, Object> confirmTransaction(String instructionId,
                                                   String transactionReferenceId,
                                                   String dynamicDataId,
                                                   String amount, String currencyCode,
                                                   String transactionStatus,
                                                   String orderId) {
        Map<String, Object> confirmEntry = new LinkedHashMap<>();
        confirmEntry.put("transactionReferenceId", transactionReferenceId);
        if (orderId != null) {
            confirmEntry.put("orderData", Map.of("orderId", orderId));
        }
        confirmEntry.put("paymentConfirmationData", Map.of(
                "dynamicDataId",      dynamicDataId,
                "transactionType",    Constants.TRANSACTION_TYPE_PURCHASE,
                "transactionStatus",  transactionStatus,
                "transactionTimestamp", String.valueOf(Instant.now().getEpochSecond()),
                "transactionAmount",  Map.of("transactionAmount",       amount,
                                             "transactionCurrencyCode", currencyCode)));

        Map<String, Object> body = Map.of(
                "clientReferenceId", Constants.USER_ID,
                "confirmationData",  List.of(confirmEntry));

        Map<String, Object> resp = makeRequest("POST",
                "/vacp/v1/instructions/" + instructionId + "/confirmations", body, true);
        if (resp == null) throw new RuntimeException("Empty response from Confirm Transaction");
        return resp;
    }

    // =========================================================================
    // Shared helpers
    // =========================================================================

    private Map<String, Object> buildAppInstance(String userAgent, String ip,
                                                  String clientDeviceId,
                                                  VicDeviceData deviceInfo) {
        Map<String, Object> ai = new LinkedHashMap<>();
        ai.put("userAgent",        userAgent);
        ai.put("ipAddress",        ip);
        ai.put("clientDeviceId",   clientDeviceId);
        ai.put("applicationName",  Constants.AGENT_NAME);
        ai.put("countryCode",      Constants.DEFAULT_COUNTRY_CODE);
        ai.put("deviceData",       deviceDataMap(deviceInfo));
        return ai;
    }
}
