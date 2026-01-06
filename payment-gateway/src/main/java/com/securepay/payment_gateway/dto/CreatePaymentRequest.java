package com.securepay.payment_gateway.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {

    @NotBlank
    private String externalReferenceId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String currency; // e.g. "INR"

    @NotBlank
    private String paymentMethod; // CARD, UPI, WALLET, NET_BANKING

    private String returnUrl;
}