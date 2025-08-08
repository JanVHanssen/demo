package be.ucll.se.demo.util;

import be.ucll.se.demo.config.JwtConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final JwtConfig config;
    private SecretKey secretKey;
    private long expiration;

    public JwtUtil(JwtConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        String secret = config.getSecret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret is null of te kort (minstens 32 tekens vereist)");
        }

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = config.getExpiration();

        System.out.println("JWT secret succesvol geïnitialiseerd.");
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println("Ongeldig token: " + e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            System.out.println("Kan username niet uit token halen: " + e.getMessage());
            return null;
        }
    }
}