package com.securepay.payment_gateway.service;

import com.securepay.payment_gateway.entity.WebhookEvent;
import com.securepay.payment_gateway.entity.Merchant;
import com.securepay.payment_gateway.repository.WebhookRepository;
import com.securepay.payment_gateway.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final MerchantRepository merchantRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public WebhookEvent createEvent(Merchant merchant, String eventType, String payloadJson) {
        WebhookEvent e = WebhookEvent.builder()
            .merchantId(merchant.getId())
            .eventType(eventType)
            .payloadJson(payloadJson)
            .deliveryStatus("PENDING")
            .retryCount(0)
            .nextAttemptAt(OffsetDateTime.now())
            .build();
        return webhookRepository.save(e);
    }

    // runs every 15 seconds (tweak for demo). Attempts to deliver pending events.
    @Scheduled(fixedDelay = 15000)
    public void sendPendingEvents() {
        List<WebhookEvent> pending = webhookRepository
            .findByDeliveryStatusAndNextAttemptAtBefore("PENDING", OffsetDateTime.now());

        for (WebhookEvent e : pending) {
            try {
                Merchant merchant = merchantRepository.findById(e.getMerchantId()).orElse(null);
                if (merchant == null || merchant.getCallbackUrl() == null) {
                    markFailed(e, "no-callback");
                    continue;
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                // For demo: add a simple signature header (in real systems use HMAC with shared secret)
                headers.add("X-SecurePay-Signature", "demo-signature");

                HttpEntity<String> req = new HttpEntity<>(e.getPayloadJson(), headers);

                ResponseEntity<String> resp = restTemplate.exchange(merchant.getCallbackUrl(), HttpMethod.POST, req, String.class);

                if (resp.getStatusCode().is2xxSuccessful()) {
                    e.setDeliveryStatus("SENT");
                    e.setRetryCount(e.getRetryCount() + 1);
                    webhookRepository.save(e);
                } else {
                    scheduleRetry(e);
                }

            } catch (Exception ex) {
                scheduleRetry(e);
            }
        }
    }

    private void scheduleRetry(WebhookEvent e) {
        int nextRetry = e.getRetryCount() + 1;
        e.setRetryCount(nextRetry);
        // exponential backoff: next attempt after 5 * 2^(retry-1) seconds
        int seconds = 5 * (1 << Math.max(0, nextRetry-1));
        e.setNextAttemptAt(OffsetDateTime.now().plusSeconds(seconds));
        // after 5 retries, mark as FAILED
        if (nextRetry >= 5) {
            e.setDeliveryStatus("FAILED");
        }
        webhookRepository.save(e);
    }

    private void markFailed(WebhookEvent e, String reason) {
        e.setDeliveryStatus("FAILED");
        e.setNextAttemptAt(null);
        webhookRepository.save(e);
    }
}
