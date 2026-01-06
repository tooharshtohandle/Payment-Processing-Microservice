package com.securepay.payment_gateway.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity
@Table(name = "webhook_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WebhookEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private String eventType; // PAYMENT_CAPTURED, PAYMENT_AUTHORIZED, REFUND_SUCCESS, etc.

    @Column(columnDefinition = "TEXT")
    private String payloadJson;

    @Column(nullable = false)
    private String deliveryStatus; // PENDING, SENT, FAILED

    private int retryCount;

    private OffsetDateTime nextAttemptAt;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.deliveryStatus == null) this.deliveryStatus = "PENDING";
        if (this.nextAttemptAt == null) this.nextAttemptAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
