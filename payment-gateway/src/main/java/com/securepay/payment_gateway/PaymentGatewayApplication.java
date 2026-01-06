package com.securepay.payment_gateway;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.securepay.payment_gateway.entity.Merchant;
import com.securepay.payment_gateway.repository.MerchantRepository;

@SpringBootApplication
@EnableScheduling
public class PaymentGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentGatewayApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedMerchant(MerchantRepository merchantRepository) {
        return args -> {
            if (merchantRepository.count() == 0) {
                String apiKey = "TEST_API_KEY_" + UUID.randomUUID();
                String apiSecret = "TEST_SECRET_" + UUID.randomUUID();

                String secretHash = hash(apiSecret);

                Merchant merchant = Merchant.builder()
                    .name("Test Merchant")
                    .apiKey(apiKey)
                    .apiSecretHash(secretHash)
                    .callbackUrl("https://example.com/callback")
                    .status(Merchant.MerchantStatus.ACTIVE)
                    .build();

                merchantRepository.save(merchant);

                System.out.println("==============================================");
                System.out.println("Seeded Test Merchant:");
                System.out.println("  Name: " + merchant.getName());
                System.out.println("  API KEY (use in X-API-KEY header): " + apiKey);
                System.out.println("  API SECRET (just for info): " + apiSecret);
                System.out.println("==============================================");
            }
        };
    }

    private String hash(String value) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("SHA-256 algorithm not available", e);
    }
}

@Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
