package com.amex.ace.agent.controller;

import com.amex.ace.agent.dto.cards.AddCardRequest;
import com.amex.ace.agent.dto.cards.AddCardResponse;
import com.amex.ace.agent.dto.cards.CardResponse;
import com.amex.ace.agent.dto.cards.EncryptedAddCardRequest;
import com.amex.ace.agent.util.Constants;
import com.amex.ace.agent.dto.cards.*;
import com.amex.ace.agent.service.CardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Card management endpoints.
 * Maps Python src/api/routes/cards.py router.
 */
@RestController
@RequestMapping("/cards")
public class CardController {

    private final CardService cardService;
    private final String cardDataPublicKeyPem;

    @Autowired
    public CardController(CardService cardService,
                          String cardDataPublicKeyPem) {
        this.cardService          = cardService;
        this.cardDataPublicKeyPem = cardDataPublicKeyPem;
    }

    /** GET /cards/public-key — returns the RSA public key PEM for card encryption. */
    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(cardDataPublicKeyPem);
    }

    /** POST /cards — add a card (JWE-encrypted payload). */
    @PostMapping
    public AddCardResponse addCard(
            @Valid @RequestBody EncryptedAddCardRequest encRequest) {
        AddCardRequest request = cardService.decryptAddCardRequest(
                encRequest.encPaymentInstrument());
        return cardService.addCard(request);
    }

    /** GET /cards — list all cards. */
    @GetMapping
    public List<CardResponse> getCards() {
        return cardService.getCards();
    }

    /** GET /cards/{cardId} — get a specific card. */
    @GetMapping("/{cardId}")
    public CardResponse getCard(@PathVariable UUID cardId) {
        return cardService.getCard(cardId);
    }

    /** GET /cards/{cardId}/art — returns the card art image. */
    @GetMapping("/{cardId}/art")
    public ResponseEntity<byte[]> getCardArt(@PathVariable UUID cardId) {
        byte[] data     = cardService.getCardArtBytes(cardId);
        String mimeType = cardService.getCardArtMimeType(cardId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CACHE_CONTROL,
                        "public, max-age=" + Constants.CACHE_CONTROL_MAX_AGE)
                .body(data);
    }

    /** DELETE /cards/{cardId} — delete a card and deprovision its token. */
    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
    }
}
