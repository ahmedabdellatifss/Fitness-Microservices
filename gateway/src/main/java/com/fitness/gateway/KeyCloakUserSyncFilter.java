package com.fitness.gateway;

import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeyCloakUserSyncFilter implements WebFilter {

    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        // Skip processing if no token
        if (token == null || !token.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        RegisterRequest registerRequest = getUserDetails(token);
        if (registerRequest == null) {
            return chain.filter(exchange);
        }

        log.info("Processing user sync for: {}", registerRequest.getEmail());
        log.info("keycloak ID is : {}", registerRequest.getKeycloakId());
        log.info("X-User-ID header: {}", userId);
        log.info("RegisterRequest is : {}", registerRequest);

        // Use keycloakId as userId if X-User-ID header is missing
        if (userId == null) {
            userId = registerRequest.getKeycloakId();
        }

        if (userId != null) {
            String finalUserId = userId;
            return userService.validateUser(userId)
                    .flatMap(exists -> {
                        if (!exists) {
                            log.info("User doesn't exist, registering new user with keycloakId: {}", finalUserId);
                            return userService.registerUser(registerRequest)
                                    .doOnSuccess(response -> log.info("User registered successfully: {}", response))
                                    .doOnError(error -> log.error("Failed to register user: {}", error.getMessage()))
                                    .then(Mono.empty());
                        } else {
                            log.info("User already exists with keycloakId: {}, skipping registration", finalUserId);
                            return Mono.empty();
                        }
                    })
                    .onErrorResume(error -> {
                        log.error("Error during user validation/registration: {}", error.getMessage());
                        // Continue with the request even if user sync fails
                        return Mono.empty();
                    })
                    .then(Mono.defer(() -> {
                        // Add/update the X-User-ID header
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-User-ID", finalUserId)
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }));
        }

        return chain.filter(exchange);
    }

    private RegisterRequest getUserDetails(String token) {
        try {
            String tokenWithoutBearer = token.substring(7);
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(claimsSet.getStringClaim("email"));
            registerRequest.setKeycloakId(claimsSet.getStringClaim("sub"));
            registerRequest.setPassword("dummy@123123"); // Consider using a more secure approach
            registerRequest.setFirstName(claimsSet.getStringClaim("given_name"));
            registerRequest.setLastName(claimsSet.getStringClaim("family_name"));

            return registerRequest;
        } catch (Exception e) {
            log.error("Failed to parse JWT token", e);
            return null;
        }
    }
}