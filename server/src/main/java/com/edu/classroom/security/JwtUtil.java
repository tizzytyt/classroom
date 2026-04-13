package com.edu.classroom.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
  private final String secret;
  private final String issuer;
  private final int expireHours;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public JwtUtil(@Value("${jwt.secret}") String secret,
                 @Value("${jwt.issuer}") String issuer,
                 @Value("${jwt.expire-hours}") int expireHours) {
    this.secret = secret;
    this.issuer = issuer;
    this.expireHours = expireHours;
  }

  public String generateToken(Long userId, String username, String roleCode) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    Map<String, Object> claims = new HashMap<>();
    claims.put("uid", userId);
    claims.put("uname", username);
    claims.put("role", roleCode);
    Date now = new Date();
    Date exp = new Date(now.getTime() + expireHours * 3600_000L);
    return Jwts.builder()
        .setIssuer(issuer)
        .setSubject(String.valueOf(userId))
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(exp)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Map<String, Object> parse(String token) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}
