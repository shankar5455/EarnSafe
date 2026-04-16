package com.earnsafe.service;

import com.earnsafe.entity.Claim;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripePayoutGateway {

    @Value("${app.payout.provider:stripe}")
    private String provider;

    @Value("${app.payout.stripe.api-key:}")
    private String stripeApiKey;

    @Value("${app.payout.stripe.destination-account:}")
    private String destinationAccount;

    @Value("${app.payout.timeout-ms:8000}")
    private long timeoutMs;

    private final RestTemplateBuilder restTemplateBuilder;

    public String triggerPayout(Claim claim) {
        if (!"stripe".equalsIgnoreCase(provider)) {
            throw new IllegalStateException("Unsupported payout provider: " + provider);
        }
        if (stripeApiKey == null || stripeApiKey.isBlank()) {
            throw new IllegalStateException("Stripe API key not configured");
        }
        if (destinationAccount == null || destinationAccount.isBlank()) {
            throw new IllegalStateException("Stripe destination account not configured");
        }

        BigDecimal amount = claim.getPayoutAmount() == null ? BigDecimal.ZERO : claim.getPayoutAmount();
        long amountInPaise = amount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValue();
        if (amountInPaise <= 0) {
            throw new IllegalStateException("Payout amount must be positive");
        }

        RestTemplate client = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(stripeApiKey, "");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("amount", String.valueOf(amountInPaise));
        body.add("currency", "inr");
        body.add("destination", destinationAccount);
        body.add("description", "EarnSafe auto payout for claim " + claim.getClaimNumber());

        try {
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = client.postForEntity("https://api.stripe.com/v1/transfers", entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Stripe payout failed with non-success response");
            }

            Object transferId = response.getBody().get("id");
            if (transferId == null) {
                throw new RuntimeException("Stripe payout response missing transfer id");
            }
            return transferId.toString();
        } catch (RestClientException ex) {
            throw new RuntimeException("Stripe payout transport error: " + ex.getMessage(), ex);
        }
    }
}
