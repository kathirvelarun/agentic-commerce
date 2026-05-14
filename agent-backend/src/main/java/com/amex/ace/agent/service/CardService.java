package com.amex.ace.agent.service;

import com.amex.ace.agent.dto.cards.AddCardRequest;
import com.amex.ace.agent.dto.cards.AddCardResponse;
import com.amex.ace.agent.dto.cards.CardResponse;
import com.amex.ace.agent.dto.cards.ProvisionTokenRequest;
import com.amex.ace.agent.entity.Card;
import com.amex.ace.agent.entity.CardArt;
import com.amex.ace.agent.entity.CardArtMap;
import com.amex.ace.agent.repository.*;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.RSAKey;
import com.amex.ace.agent.dto.cards.*;
import com.amex.ace.agent.entity.*;
import com.amex.ace.agent.repository.*;
import com.amex.ace.agent.service.vdp.VdpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Card management service.
 * Direct translation of Python src/services/card.py CardService.
 */
@Service
@Slf4j
public class CardService {

    private final VdpClient vdpClient;
    private final CardRepository cardRepository;
    private final CardArtRepository cardArtRepository;
    private final CardArtMapRepository cardArtMapRepository;
    private final IntentRepository intentRepository;
    private final MandateRepository mandateRepository;
    private final RSAKey cardDataRsaKey;
    private final ObjectMapper objectMapper;

    @Autowired
    public CardService(VdpClient vdpClient,
                       CardRepository cardRepository,
                       CardArtRepository cardArtRepository,
                       CardArtMapRepository cardArtMapRepository,
                       IntentRepository intentRepository,
                       MandateRepository mandateRepository,
                       RSAKey cardDataRsaKey,
                       ObjectMapper objectMapper) {
        this.vdpClient           = vdpClient;
        this.cardRepository      = cardRepository;
        this.cardArtRepository   = cardArtRepository;
        this.cardArtMapRepository = cardArtMapRepository;
        this.intentRepository    = intentRepository;
        this.mandateRepository   = mandateRepository;
        this.cardDataRsaKey      = cardDataRsaKey;
        this.objectMapper        = objectMapper;
    }

    // -------------------------------------------------------------------------
    // Decrypt card data
    // -------------------------------------------------------------------------

    public AddCardRequest decryptAddCardRequest(String encPaymentInstrument) {
        try {
            JWEObject jwe = JWEObject.parse(encPaymentInstrument);
            jwe.decrypt(new RSADecrypter(cardDataRsaKey));
            return objectMapper.readValue(
                    jwe.getPayload().toString(), AddCardRequest.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Failed to decrypt payment instrument: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Add card
    // -------------------------------------------------------------------------

    @Transactional
    public AddCardResponse addCard(AddCardRequest request) {
        UUID cardId = UUID.randomUUID();

        // 1. Enroll PAN (fatal)
        Map<String, Object> panEnrollResponse;
        String panEnrollmentId;
        try {
            panEnrollResponse = vdpClient.enrollPan(
                    request.cardNumber(), request.nameOnCard(),
                    request.expMonth(), request.expYear(), request.cvv());
            panEnrollmentId   = (String) panEnrollResponse.get("vPanEnrollmentID");
            if (panEnrollmentId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "PAN enrollment failed");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAN enrollment failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "PAN enrollment failed");
        }

        // 2. Persist card (fatal)
        Card card;
        try {
            card = cardRepository.save(Card.builder()
                    .id(cardId)
                    .last4(request.cardNumber().substring(request.cardNumber().length() - 4))
                    .type("V")
                    .expMonth(request.expMonth())
                    .expYear(request.expYear())
                    .status("PENDING")
                    .panEnrollmentId(panEnrollmentId)
                    .build());
        } catch (DataIntegrityViolationException e) {
            log.error("IntegrityError adding card", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Card already exists or invalid data");
        }

        // 3. Provision token (non-fatal)
        try {
            provisionToken(new ProvisionTokenRequest(cardId));
        } catch (Exception e) {
            log.error("Token provisioning failed (non-fatal)", e);
        }

        // 4. Fetch and persist card art (non-fatal)
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> cardMetadata = (Map<String, Object>)
                    panEnrollResponse.get("cardMetaData");
            String cardArtId = extractCardArtId(cardMetadata);
            putCardArt(panEnrollmentId, cardArtId);
        } catch (Exception e) {
            log.error("Failed to save card art (non-fatal)", e);
        }

        return new AddCardResponse(card.getId());
    }

    // -------------------------------------------------------------------------
    // Provision token
    // -------------------------------------------------------------------------

    @Transactional
    public CardResponse provisionToken(ProvisionTokenRequest request) {
        Card card = cardRepository.findById(request.cardId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Card not found."));
        if (card.getTokenId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Token already provisioned for this card.");
        }
        try {
            Map<String, Object> token = vdpClient.provisionTokenGivenPanEnrollmentId(
                    card.getPanEnrollmentId(), card.getExpMonth(), card.getExpYear());
            String tokenId = (String) token.get("vProvisionedTokenID");
            if (tokenId == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Token provisioning failed");
            }
            card.setTokenId(tokenId);
            cardRepository.save(card);
            return toCardResponse(card);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token provisioning failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Token provisioning failed");
        }
    }

    // -------------------------------------------------------------------------
    // Get card(s)
    // -------------------------------------------------------------------------

    public CardResponse getCard(UUID cardId) {
        return cardRepository.findById(cardId)
                .map(this::toCardResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Card not found."));
    }

    public List<CardResponse> getCards() {
        return cardRepository.findAll().stream().map(this::toCardResponse).toList();
    }

    // -------------------------------------------------------------------------
    // Card art
    // -------------------------------------------------------------------------

    public byte[] getCardArtBytes(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Card not found."));

        Optional<CardArtMap> mapOpt = cardArtMapRepository
                .findByPanEnrollmentId(card.getPanEnrollmentId());
        String artId;
        if (mapOpt.isEmpty()) {
            artId = putCardArt(card.getPanEnrollmentId(), null);
        } else {
            artId = mapOpt.get().getCardArtId();
        }
        CardArt art = cardArtRepository.findById(artId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Card art not found."));
        return art.getData();
    }

    public String getCardArtMimeType(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Card not found."));
        Optional<CardArtMap> mapOpt = cardArtMapRepository
                .findByPanEnrollmentId(card.getPanEnrollmentId());
        String artId = mapOpt.map(CardArtMap::getCardArtId)
                .orElseGet(() -> putCardArt(card.getPanEnrollmentId(), null));
        return cardArtRepository.findById(artId)
                .map(CardArt::getMimeType)
                .orElse("image/png");
    }

    @Transactional
    public String putCardArt(String panEnrollmentId, String cardArtId) {
        if (cardArtId == null) {
            Map<String, Object> meta     = vdpClient.getCardMetadata(panEnrollmentId);
            cardArtId = extractCardArtId(meta);
            if (cardArtId == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Card art GUID not found for PAN.");
            }
        }
        final String finalCardArtId = cardArtId;

        if (!cardArtRepository.existsById(finalCardArtId)) {
            Map<String, Object> artResp = vdpClient.getCardArt(finalCardArtId);
            if (artResp == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Card art image not found.");
            }
            byte[] data = Base64.getDecoder().decode((String) artResp.get("encodedData"));
            cardArtRepository.save(CardArt.builder()
                    .id(finalCardArtId)
                    .mimeType((String) artResp.get("mimeType"))
                    .data(data)
                    .build());
        }

        Optional<CardArtMap> existing = cardArtMapRepository
                .findByPanEnrollmentId(panEnrollmentId);
        if (existing.isEmpty()) {
            cardArtMapRepository.save(new CardArtMap(panEnrollmentId, finalCardArtId));
        } else if (!finalCardArtId.equals(existing.get().getCardArtId())) {
            cardArtMapRepository.save(new CardArtMap(panEnrollmentId, finalCardArtId));
        }
        return finalCardArtId;
    }

    // -------------------------------------------------------------------------
    // Delete card
    // -------------------------------------------------------------------------

    @Transactional
    public void deleteCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Card not found."));
        String panEnrollmentId = card.getPanEnrollmentId();

        // Deprovision token (non-fatal)
        if (card.getTokenId() != null) {
            try {
                vdpClient.deprovision(card.getTokenId());
            } catch (Exception e) {
                log.error("Deprovisioning failed", e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Token deprovisioning failed");
            }
        }

        // Delete card (fatal)
        try {
            cardRepository.delete(card);
        } catch (DataIntegrityViolationException e) {
            log.error("IntegrityError deleting card", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete card");
        }

        // Clean up card art mapping if no other cards use this PAN (non-fatal)
        try {
            if (cardRepository.findByPanEnrollmentId(panEnrollmentId).isEmpty()) {
                cardArtMapRepository.deleteByPanEnrollmentId(panEnrollmentId);
            }
        } catch (Exception e) {
            log.error("Failed to delete card art mapping (non-fatal)", e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CardResponse toCardResponse(Card c) {
        return new CardResponse(c.getId(), c.getExpMonth(), c.getExpYear(),
                c.getLast4(), c.getStatus(), c.getTokenId());
    }

    @SuppressWarnings("unchecked")
    private String extractCardArtId(Map<String, Object> cardMetadata) {
        if (cardMetadata == null) return null;
        List<Map<String, Object>> cardData =
                (List<Map<String, Object>>) cardMetadata.get("cardData");
        if (cardData == null) return null;
        return cardData.stream()
                .filter(d -> "digitalCardArt".equals(d.get("contentType")))
                .findFirst()
                .map(d -> (String) d.get("guid"))
                .orElse(null);
    }
}
