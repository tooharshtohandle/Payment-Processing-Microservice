package com.securepay.payment_gateway.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securepay.payment_gateway.entity.WebhookEvent;

public interface WebhookRepository extends JpaRepository<WebhookEvent, String> {
    List<WebhookEvent> findByDeliveryStatusAndNextAttemptAtBefore(String deliveryStatus, OffsetDateTime before);
}
