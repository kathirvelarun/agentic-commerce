package com.amex.ace.agent.service;

import com.amex.ace.agent.dto.commerce.*;
import com.amex.ace.agent.entity.Card;
import com.amex.ace.agent.entity.Intent;
import com.amex.ace.agent.entity.Mandate;
import com.amex.ace.agent.entity.Transaction;
import com.amex.ace.agent.entity.enums.IntentStatus;
import com.amex.ace.agent.entity.enums.MandateStatus;
import com.amex.ace.agent.entity.enums.TransactionStatus;
import com.amex.ace.agent.repository.CardRepository;
import com.amex.ace.agent.repository.IntentRepository;
import com.amex.ace.agent.repository.MandateRepository;
import com.amex.ace.agent.repository.TransactionRepository;
import com.amex.ace.agent.dto.commerce.*;
import com.amex.ace.agent.entity.*;
import com.amex.ace.agent.entity.enums.*;
import com.amex.ace.agent.repository.*;
import com.amex.ace.agent.service.vdp.VdpClient;
import com.amex.ace.agent.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Commerce service — VIC token enrollment and agentic checkout orchestration.
 * Direct translation of Python src/services/commerce.py CommerceService.
 */
@Service
@Slf4j
public class CommerceService {

    private final VdpClient vdpClient;
    private final CardRepository cardRepository;
    private final IntentRepository intentRepository;
    private final MandateRepository mandateRepository;
    private final TransactionRepository transactionRepository;
    private final ChatService chatService;

    @Autowired
    public CommerceService(VdpClient vdpClient,
                           CardRepository cardRepository,
                           IntentRepository intentRepository,
                           MandateRepository mandateRepository,
                           TransactionRepository transactionRepository,
                           ChatService chatService) {
        this.vdpClient             = vdpClient;
        this.cardRepository        = cardRepository;
        this.intentRepository      = intentRepository;
        this.mandateRepository     = mandateRepository;
        this.transactionRepository = transactionRepository;
        this.chatService           = chatService;
    }

    // -------------------------------------------------------------------------
    // Token enrollment (POST /tokens/enroll)
    // -------------------------------------------------------------------------

    @Transactional
    public Map<String, Object> enrollToken(EnrollRequest request) {
        Card card = cardRepository.findByTokenId(request.provisionedTokenId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Card not found for the provided token ID."));

        Map<String, Object> enrollResponse = vdpClient.enrollCard(
                request.provisionedTokenId(),
                request.clientDeviceId(),
                request.ip(),
                request.userAgent(),
                request.deviceInfo());

        if (enrollResponse != null
                && "ACTIVE".equals(enrollResponse.get("status"))) {
            card.setStatus("ACTIVE");
            cardRepository.save(card);
        }
        return enrollResponse;
    }

    // -------------------------------------------------------------------------
    // Agentic checkout (POST /intents/agent)
    // -------------------------------------------------------------------------

    @Transactional
    public AgenticCheckoutResponse handleAgenticCheckout(AgenticCheckoutRequest req) {

        // 1. Validate card
        Card card = cardRepository.findByTokenId(req.provisionedTokenId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Card not found for the provided token ID."));

        // 2. Create intent in VIC
        UUID mandateId         = UUID.randomUUID();
        long now               = Instant.now().getEpochSecond();
        long effectiveUntilTime = now + Constants.THREE_DAYS_SECONDS;

        Map<String, Object> intentResponse = vdpClient.createIntent(
                req.provisionedTokenId(),
                mandateId,
                req.amount(),
                req.currencyCode(),
                req.assuranceData(),
                now,
                effectiveUntilTime,
                req.prompt(),
                req.clientDeviceId(),
                req.ip(),
                req.userAgent(),
                req.deviceInfo());

        if (intentResponse == null || "PENDING".equals(intentResponse.get("status"))) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create intent.");
        }

        // 3. Persist intent + mandate
        Intent intent = intentRepository.save(Intent.builder()
                .instructionId((String) intentResponse.get("instructionId"))
                .card(card)
                .last4(card.getLast4())
                .status(IntentStatus.ACTIVE)
                .build());

        Mandate mandate = mandateRepository.save(Mandate.builder()
                .id(mandateId)
                .intent(intent)
                .amount(String.valueOf(req.amount()))
                .currencyCode(req.currencyCode())
                .effectiveUntilTime(effectiveUntilTime)
                .description(req.prompt() != null
                        ? req.prompt() : Constants.DEFAULT_INTENT_DESCRIPTION)
                .status(MandateStatus.ACTIVE)
                .build());

        // 4. Retrieve credentials from VIC
        TransactionAmount txAmount = new TransactionAmount(
                String.valueOf(req.amount()), req.currencyCode());
        TransactionData txData = new TransactionData(
                txAmount,
                req.prompt() != null ? req.prompt() : "",
                Constants.DEFAULT_COUNTRY_CODE,
                Constants.DEFAULT_MERCHANT_URL,
                mandate.getId());

        Map<String, Object> credsResponse = vdpClient.retrieveCredentials(
                req.provisionedTokenId(), intent.getInstructionId(), List.of(txData));

        if (credsResponse == null || !"COMPLETED".equals(credsResponse.get("status"))) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve credentials.");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dynamicData =
                (List<Map<String, Object>>) credsResponse.get("dynamicData");
        if (dynamicData == null || dynamicData.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No payment credentials found in the response.");
        }
        Map<String, Object> paymentCredential = dynamicData.get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> token = (Map<String, Object>) paymentCredential.get("token");

        Credentials credentials = new Credentials(
                (String) token.get("paymentToken"),
                (String) token.get("tokenExpirationMonth"),
                (String) token.get("tokenExpirationYear"),
                (String) paymentCredential.get("dynamicDataValue"));

        // 5. Persist transaction
        Transaction transaction = transactionRepository.save(Transaction.builder()
                .id(txData.transactionReferenceId())
                .mandate(mandate)
                .amount(String.valueOf(req.amount()))
                .currencyCode(req.currencyCode())
                .merchantName(req.prompt() != null ? req.prompt() : "")
                .merchantUrl(Constants.DEFAULT_MERCHANT_URL)
                .merchantCountryCode(Constants.DEFAULT_COUNTRY_CODE)
                .status(TransactionStatus.ACTIVE)
                .build());

        // 6. Complete checkout via AI agent (triggers MCP checkout_cart + elicitation)
        AgenticCheckoutResponse checkoutResponse =
                chatService.completeCheckout(credentials);

        // 7. Confirm transaction in VIC
        String txStatus = checkoutResponse.purchaseSummary() != null
                ? Constants.TRANSACTION_STATUS_APPROVED
                : Constants.TRANSACTION_STATUS_DECLINED;
        String orderId  = checkoutResponse.purchaseSummary() != null
                ? checkoutResponse.purchaseSummary().orderId() : null;

        try {
            vdpClient.confirmTransaction(
                    intent.getInstructionId(),
                    transaction.getId().toString(),
                    (String) paymentCredential.get("dynamicDataId"),
                    String.valueOf(req.amount()),
                    req.currencyCode(),
                    txStatus,
                    orderId);
        } catch (Exception e) {
            log.error("Failed to confirm transaction with VIC (non-fatal)", e);
        }

        // 8. Update transaction status
        transaction.setStatus(checkoutResponse.purchaseSummary() != null
                ? TransactionStatus.SUCCESS : TransactionStatus.FAILURE);
        transactionRepository.save(transaction);

        return checkoutResponse;
    }
}
