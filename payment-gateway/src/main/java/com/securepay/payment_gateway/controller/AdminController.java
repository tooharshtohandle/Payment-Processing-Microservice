package com.securepay.payment_gateway.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securepay.payment_gateway.entity.WebhookEvent;
import com.securepay.payment_gateway.repository.WebhookRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final WebhookRepository webhookRepository;

    @GetMapping("/webhooks")
    public List<WebhookEvent> listWebhooks() {
        return webhookRepository.findAll();
    }
}

