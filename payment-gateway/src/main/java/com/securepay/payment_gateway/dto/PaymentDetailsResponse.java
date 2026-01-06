package com.securepay.payment_gateway.dto;


import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PaymentDetailsResponse {
    private String transactionId;
    private String externalReferenceId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String status;
}
