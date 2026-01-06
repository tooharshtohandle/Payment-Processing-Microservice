package com.securepay.payment_gateway.dto;


import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CreateRefundRequest {

    @NotBlank
    private String refundReferenceId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
