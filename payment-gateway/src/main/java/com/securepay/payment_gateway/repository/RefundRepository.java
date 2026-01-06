package com.securepay.payment_gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securepay.payment_gateway.entity.PaymentTransaction;
import com.securepay.payment_gateway.entity.Refund;

public interface RefundRepository extends JpaRepository<Refund, String> {

    List<Refund> findByTransactionAndStatus(PaymentTransaction transaction, Refund.Status status);

    boolean existsByTransactionAndRefundReferenceId(PaymentTransaction transaction, String refundReferenceId);
}