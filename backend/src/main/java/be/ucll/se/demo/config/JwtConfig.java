package be.ucll.se.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class JwtConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtConfig.class);

    @Value("${jwt.secret:abcdefghijklmnopqrstuvwxyz1234567890}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private long expiration;

    public String getSecret() {
        return secret;
    }

    public long getExpiration() {
        return expiration;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("JWT Secret loaded with length: {}", secret.length());
        LOGGER.info("JWT Expiration: {}", expiration);
    }
}