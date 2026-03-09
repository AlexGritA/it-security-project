package se.apiva.chatserver.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public final class JwtUtils {

    private final SecretKey key;
    private final String issuer;
    private final String audience;

    public JwtUtils(String hmacSecret, String issuer, String audience) {
        Objects.requireNonNull(hmacSecret, "hmacSecret");
        // För HS256 behöver secret vara tillräckligt långt (minst 256 bit = 32 bytes).
        this.key = Keys.hmacShaKeyFor(hmacSecret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
    }

    /**
     * Verifierar signatur + exp/nbf automatiskt, samt issuer/audience om angivet.
     * Returnerar Claims om allt är OK, annars kastas JwtException.
     */
    public Claims verify(String jwt) throws JwtException {
        var parser = Jwts.parser()
                .verifyWith(key)
                .build();

        Claims claims = parser
                .parseSignedClaims(jwt)
                .getPayload();

        if (issuer != null && !issuer.equals(claims.getIssuer())) {
            throw new JwtException("Invalid issuer");
        }
        if (audience != null) {
            // aud kan vara sträng eller lista; JJWT mappar ofta till String eller Collection.
            Object aud = claims.get("aud");
            if (aud == null || !aud.toString().contains(audience)) {
                throw new JwtException("Invalid audience");
            }
        }

        return claims;
    }

    /**
     * Skapar token
     */
    public String createToken(String subject, Map<String, String> claims) {

        JwtBuilder builder = Jwts.builder()
                                .setSubject(subject)
                                .setIssuer(issuer)
                                .setAudience(audience)
                                .setIssuedAt(new Date())
                                .setExpiration(new Date(System.currentTimeMillis() + 3600000)); // 1hr

        // Set Claims (data) if any
        if(claims != null && !claims.isEmpty()) {
            for (Map.Entry<String, String> entry : claims.entrySet()) {
                builder.claim(entry.getKey(), entry.getValue());
            }
        }

        // Generate token
        return builder.signWith(key).compact();
    }
}