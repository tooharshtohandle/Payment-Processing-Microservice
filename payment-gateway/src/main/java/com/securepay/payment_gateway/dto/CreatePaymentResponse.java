package com.securepay.payment_gateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentResponse {
    
    private String transactionId;
    private String status;
    private String paymentSessionToken;
    private String redirectUrl;

}
