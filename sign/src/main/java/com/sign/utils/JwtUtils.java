package com.sign.utils;

import io.jsonwebtoken.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

public class JwtUtils {

    private static final String SECRET_KEY="4b9ce953b6a221989c9eed27d86c874b961fdebc9bfe38a8d68f4722ef84dbc7502b3cec84b6f4656fbcff04ed757e40d9eb8d75b66e0176b9b0e335af142731b0d42bd9327d9939fd76c34972339f1d1695899c8cc1ce633747132c4061a43eac24cfc6725f580a3d85d0c7d5f7206161fbedf6bd24da77d91808bb32f7d93898d86614abc4c916b0f6581dde61dd7f933e869c40574d10bc10d7726f6e7dcaf43a408c401a6202b6e215729f0f52ceeabe4f5f2e4db3b28c55d56ff2b899ee9a2b87015379ff633c776959e4bbd479a636d46e6854b62ee9c6a8acc8402f4f94ae8632324531f43f9a16063b12dc0f9136f0f3eea131e6210d72cc994f824c";

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
