package com.securepay.payment_gateway.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securepay.payment_gateway.dto.CreatePaymentRequest;
import com.securepay.payment_gateway.dto.CreatePaymentResponse;
import com.securepay.payment_gateway.dto.CreateRefundRequest;
import com.securepay.payment_gateway.dto.PaymentDetailsResponse;
import com.securepay.payment_gateway.dto.RefundResponse;
import com.securepay.payment_gateway.entity.Merchant;
import com.securepay.payment_gateway.entity.PaymentTransaction;
import com.securepay.payment_gateway.entity.Refund;
import com.securepay.payment_gateway.repository.PaymentTransactionRepository;
import com.securepay.payment_gateway.repository.RefundRepository;
import com.securepay.payment_gateway.util.MerchantContext;
import com.securepay.payment_gateway.service.WebhookService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository transactionRepository;
    private final RefundRepository refundRepository;
    private final WebhookService webhookService;
 

    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        Merchant merchant = MerchantContext.get();
        if (merchant == null) {
            throw new IllegalStateException("Merchant not found in context");
        }

        PaymentTransaction.PaymentMethod method =
                PaymentTransaction.PaymentMethod.valueOf(request.getPaymentMethod());

        PaymentTransaction txn = PaymentTransaction.builder()
            .merchant(merchant)
            .externalReferenceId(request.getExternalReferenceId())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .paymentMethod(method)
            .status(PaymentTransaction.Status.CREATED)
            .build();

        txn = transactionRepository.save(txn);

        String sessionToken = UUID.randomUUID().toString();

        return CreatePaymentResponse.builder()
            .transactionId(txn.getId())
            .status(txn.getStatus().name())
            .paymentSessionToken(sessionToken)
            .redirectUrl("https://securepay.local/pay?sess=" + sessionToken)
            .build();
    }
    

    public PaymentDetailsResponse getPaymentById(String transactionId) {
    var merchant = MerchantContext.get();
    if (merchant == null) {
        throw new IllegalStateException("Merchant not found in context");
    }

    var txn = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

    // Extra safety: ensure merchant only sees its own payments
    if (!txn.getMerchant().getId().equals(merchant.getId())) {
        throw new IllegalArgumentException("Transaction does not belong to this merchant");
    }

    return PaymentDetailsResponse.builder()
        .transactionId(txn.getId())
        .externalReferenceId(txn.getExternalReferenceId())
        .amount(txn.getAmount())
        .currency(txn.getCurrency())
        .paymentMethod(txn.getPaymentMethod().name())
        .status(txn.getStatus().name())
        .build();
}

private PaymentDetailsResponse mapToResponse(PaymentTransaction txn) {
    return PaymentDetailsResponse.builder()
        .transactionId(txn.getId())
        .externalReferenceId(txn.getExternalReferenceId())
        .amount(txn.getAmount())
        .currency(txn.getCurrency())
        .paymentMethod(txn.getPaymentMethod().name())
        .status(txn.getStatus().name())
        .build();
}


@Transactional
public PaymentDetailsResponse authorizePayment(String transactionId) {
    var merchant = MerchantContext.get();
    if (merchant == null) {
        throw new IllegalStateException("Merchant not found in context");
    }

    var txn = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

    if (!txn.getMerchant().getId().equals(merchant.getId())) {
        throw new IllegalArgumentException("Transaction does not belong to this merchant");
    }

    if (txn.getStatus() != PaymentTransaction.Status.CREATED) {
        throw new IllegalStateException("Only CREATED payments can be authorized");
    }

    // simulate successful authorization from provider
    txn.setStatus(PaymentTransaction.Status.AUTHORIZED);
    txn.setProviderTransactionId("PROV-" + transactionId);
    txn.setAuthCode("AUTH-" + transactionId.substring(0, 6));
    String payload = "{ \"eventType\": \"PAYMENT_AUTHORIZED\", \"transactionId\": \"" + txn.getId() + "\", \"status\": \"" + txn.getStatus().name() + "\" }";
    webhookService.createEvent(merchant, "PAYMENT_AUTHORIZED", payload);

    // JPA will auto-flush thanks to @Transactional

    return mapToResponse(txn);
}

@Transactional
public PaymentDetailsResponse capturePayment(String transactionId) {
    var merchant = MerchantContext.get();
    if (merchant == null) {
        throw new IllegalStateException("Merchant not found in context");
    }

    var txn = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

    if (!txn.getMerchant().getId().equals(merchant.getId())) {
        throw new IllegalArgumentException("Transaction does not belong to this merchant");
    }

    if (txn.getStatus() != PaymentTransaction.Status.AUTHORIZED) {
        throw new IllegalStateException("Only AUTHORIZED payments can be captured");
    }

    // simulate capture success
    txn.setStatus(PaymentTransaction.Status.CAPTURED);
    String payload = "{ \"eventType\": \"PAYMENT_CAPTURED\", \"transactionId\": \"" + txn.getId() + "\", \"status\": \"" + txn.getStatus().name() + "\" }";
    webhookService.createEvent(merchant, "PAYMENT_CAPTURED", payload);


    return mapToResponse(txn);
}

@Transactional
public RefundResponse createRefund(String transactionId, CreateRefundRequest request) {
    var merchant = MerchantContext.get();
    if (merchant == null) {
        throw new IllegalStateException("Merchant not found in context");
    }

    var txn = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

    if (!txn.getMerchant().getId().equals(merchant.getId())) {
        throw new IllegalArgumentException("Transaction does not belong to this merchant");
    }

    // only captured or partially_refunded payments can be refunded
    if (txn.getStatus() != PaymentTransaction.Status.CAPTURED
            && txn.getStatus() != PaymentTransaction.Status.PARTIALLY_REFUNDED) {
        throw new IllegalStateException("Only CAPTURED or PARTIALLY_REFUNDED payments can be refunded");
    }

    // idempotency on refundReferenceId
    if (refundRepository.existsByTransactionAndRefundReferenceId(txn, request.getRefundReferenceId())) {
        throw new IllegalStateException("Refund with this reference already exists");
    }

    // calculate total already refunded
    var successfulRefunds = refundRepository.findByTransactionAndStatus(txn, Refund.Status.SUCCESS);

    java.math.BigDecimal alreadyRefunded = successfulRefunds.stream()
        .map(Refund::getAmount)
        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

    java.math.BigDecimal remaining = txn.getAmount().subtract(alreadyRefunded);

    if (request.getAmount().compareTo(remaining) > 0) {
        throw new IllegalStateException("Refund amount exceeds remaining refundable amount");
    }

    // create refund
    Refund refund = Refund.builder()
        .transaction(txn)
        .refundReferenceId(request.getRefundReferenceId())
        .amount(request.getAmount())
        .status(Refund.Status.INITIATED)
        .build();

    // simulate provider success
    refund.setStatus(Refund.Status.SUCCESS);
    refund = refundRepository.save(refund);

    // update payment status
    java.math.BigDecimal newTotalRefunded = alreadyRefunded.add(request.getAmount());

    if (newTotalRefunded.compareTo(txn.getAmount()) == 0) {
        txn.setStatus(PaymentTransaction.Status.REFUNDED);
    } else {
        txn.setStatus(PaymentTransaction.Status.PARTIALLY_REFUNDED);
    }

    String payload = "{ \"eventType\": \"REFUND_SUCCESS\", \"transactionId\": \"" + txn.getId() + "\", \"refundId\": \"" + refund.getId() + "\", \"amount\": " + refund.getAmount() + " }";
    webhookService.createEvent(merchant, "REFUND_SUCCESS", payload);

    // JPA flush via @Transactional

    return RefundResponse.builder()
        .refundId(refund.getId())
        .refundReferenceId(refund.getRefundReferenceId())
        .amount(refund.getAmount())
        .status(refund.getStatus().name())
        .transactionId(txn.getId())
        .totalRefunded(newTotalRefunded)
        .paymentStatus(txn.getStatus().name())
        .build();
}



}
