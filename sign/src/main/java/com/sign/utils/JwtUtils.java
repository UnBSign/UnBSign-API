package com.sign.utils;

import io.jsonwebtoken.*;
import java.util.Date;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

public class JwtUtils {

    private static final EnvConfig config = EnvConfig.getInstance();

    private static final String SECRET_KEY= config.SECRET_KEY;

    private static Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(getPublicSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private static SecretKey getPublicSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public static String extractUserId(String token) {
        return extractClaims(token).get("user_id", String.class);
    }

    public static boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public static boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
