package com.amex.ace.agent.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Generates an ephemeral RSA-2048 key pair at startup for encrypting card data
 * in transit between the frontend and backend.
 *
 * Equivalent to Python cards.py:
 *   _rsa_key = JsonWebKey.generate_key('RSA', 2048, is_private=True)
 *   CARD_DATA_PRIVATE_KEY = _rsa_key
 *   CARD_DATA_PUBLIC_KEY_PEM = _rsa_key.as_pem(is_private=False)
 */
@Configuration
public class JweKeyConfig {

    private final RSAKey rsaKey;

    public JweKeyConfig() throws JOSEException {
        rsaKey = new RSAKeyGenerator(2048)
                .keyUse(KeyUse.ENCRYPTION)
                .generate();
    }

    @Bean
    public RSAKey cardDataRsaKey() {
        return rsaKey;
    }

    /** Public key in PEM format — returned by GET /cards/public-key. */
    @Bean
    public String cardDataPublicKeyPem() throws Exception {
        RSAPublicKey pub = rsaKey.toRSAPublicKey();
        byte[] encoded = pub.getEncoded();
        String b64 = java.util.Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(encoded);
        return "-----BEGIN PUBLIC KEY-----\n" + b64 + "\n-----END PUBLIC KEY-----\n";
    }
}
