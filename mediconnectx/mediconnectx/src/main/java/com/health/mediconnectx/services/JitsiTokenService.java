package com.health.mediconnectx.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JitsiTokenService {

    @Value("${jitsi.app.id:mediconnectx-demo}")
    private String APP_ID;

    @Value("${jitsi.app.secret:mediconnectx-jitsi-secret-key-32chars!!}")
    private String APP_SECRET;

    /**
     * Generates a Jitsi-compatible JWT token.
     * For the free meet.jit.si server this token is informational;
     * for a JaaS (8x8.vc) account it enforces room access.
     */
    public String generateJitsiToken(String username, String roomName, boolean isDoctor) {
        long nowMillis = System.currentTimeMillis();
        Date exp = new Date(nowMillis + 3_600_000L); // 1-hour validity

        SecretKey signingKey = Keys.hmacShaKeyFor(
                APP_SECRET.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> userContext = new HashMap<>();
        userContext.put("name", username);
        userContext.put("moderator", isDoctor);

        Map<String, Object> context = new HashMap<>();
        context.put("user", userContext);

        return Jwts.builder()
                .setIssuer(APP_ID)
                .setSubject("meet.jit.si")
                .setAudience("jitsi")
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(exp)
                .claim("room", roomName)
                .claim("context", context)
                .signWith(signingKey)
                .compact();
    }
}
