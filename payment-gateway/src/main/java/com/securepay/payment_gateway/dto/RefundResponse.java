package com.securepay.payment_gateway.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class RefundResponse {

    private String refundId;
    private String refundReferenceId;
    private BigDecimal amount;
    private String status;

    private String transactionId;
    private BigDecimal totalRefunded;
    private String paymentStatus; // REFUNDED / PARTIALLY_REFUNDED etc.
}