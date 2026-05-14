package com.example.task_scheduler.security;

import com.example.task_scheduler.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final SecretKey secretKey;
	private final long expirationMs;

	public JwtService(JwtProperties jwtProperties) {
		String secret = jwtProperties.getSecret();
		long expirationMs = jwtProperties.getExpirationMs();
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		if (keyBytes.length < 32) {
			throw new IllegalStateException("app.jwt.secret must be at least 32 bytes for HS256");
		}
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
		this.expirationMs = expirationMs;
	}

	public String generateToken(AuthenticatedUser user) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.subject(user.getId().toString())
				.claim("username", user.getUsername())
				.issuedAt(now)
				.expiration(exp)
				.signWith(secretKey)
				.compact();
	}

	public AuthenticatedUser parseToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		UUID id = UUID.fromString(claims.getSubject());
		String username = claims.get("username", String.class);
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("Invalid token");
		}
		return AuthenticatedUser.fromJwt(id, username);
	}
}
