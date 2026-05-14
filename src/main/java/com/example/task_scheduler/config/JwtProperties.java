package com.example.task_scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT settings from {@code application.properties} under prefix {@code app.jwt}.
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	/**
	 * HS256 signing secret; UTF-8 byte length must be at least 32.
	 */
	private String secret;

	/**
	 * Token lifetime in milliseconds.
	 */
	private long expirationMs = 86_400_000L;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public long getExpirationMs() {
		return expirationMs;
	}

	public void setExpirationMs(long expirationMs) {
		this.expirationMs = expirationMs;
	}
}
