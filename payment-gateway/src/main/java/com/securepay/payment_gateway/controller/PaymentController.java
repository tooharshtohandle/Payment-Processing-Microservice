package com.securepay.payment_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securepay.payment_gateway.dto.CreatePaymentRequest;
import com.securepay.payment_gateway.dto.CreatePaymentResponse;
import com.securepay.payment_gateway.dto.CreateRefundRequest;
import com.securepay.payment_gateway.dto.PaymentDetailsResponse;
import com.securepay.payment_gateway.dto.RefundResponse;
import com.securepay.payment_gateway.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        CreatePaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
public ResponseEntity<PaymentDetailsResponse> getPayment(
        @PathVariable String transactionId) {

    PaymentDetailsResponse response = paymentService.getPaymentById(transactionId);
    return ResponseEntity.ok(response);
}

@PostMapping("/{transactionId}/authorize")
public ResponseEntity<PaymentDetailsResponse> authorizePayment(
        @PathVariable String transactionId) {

    PaymentDetailsResponse response = paymentService.authorizePayment(transactionId);
    return ResponseEntity.ok(response);
}

@PostMapping("/{transactionId}/capture")
public ResponseEntity<PaymentDetailsResponse> capturePayment(
        @PathVariable String transactionId) {

    PaymentDetailsResponse response = paymentService.capturePayment(transactionId);
    return ResponseEntity.ok(response);
}

@PostMapping("/{transactionId}/refunds")
public ResponseEntity<RefundResponse> createRefund(
        @PathVariable String transactionId,
        @Valid @RequestBody CreateRefundRequest request) {

    RefundResponse response = paymentService.createRefund(transactionId, request);
    return ResponseEntity.ok(response);
}


}
