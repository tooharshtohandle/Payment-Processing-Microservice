package com.securepay.payment_gateway.config;

import com.securepay.payment_gateway.entity.Merchant;
import com.securepay.payment_gateway.repository.MerchantRepository;
import com.securepay.payment_gateway.util.MerchantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final MerchantRepository merchantRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String path = request.getRequestURI();

            // Allow some endpoints without auth if you want later
            if (path.startsWith("/actuator") || path.startsWith("/error")) {
                filterChain.doFilter(request, response);
                return;
            }

            String apiKey = request.getHeader("X-API-KEY");
            if (apiKey == null || apiKey.isBlank()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing X-API-KEY header");
                return;
            }

            Merchant merchant = merchantRepository.findByApiKey(apiKey)
                .orElse(null);

            if (merchant == null || merchant.getStatus() != Merchant.MerchantStatus.ACTIVE) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or inactive API key");
                return;
            }

            MerchantContext.set(merchant);
            filterChain.doFilter(request, response);
        } finally {
            MerchantContext.clear();
        }
    }
}
