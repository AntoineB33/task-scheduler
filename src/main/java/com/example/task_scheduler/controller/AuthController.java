package com.example.task_scheduler.controller;

import com.example.task_scheduler.entity.AppUser;
import com.example.task_scheduler.repository.AppUserRepository;
import com.example.task_scheduler.security.AuthenticatedUser;
import com.example.task_scheduler.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthController(
			AuthenticationManager authenticationManager,
			AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService) {
		this.authenticationManager = authenticationManager;
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@PostMapping("/login")
	public TokenResponse login(@RequestBody LoginRequest body) {
		if (body.username() == null || body.username().isBlank() || body.password() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username and password required");
		}
		Authentication auth = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(body.username().trim(), body.password()));
		AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
		return new TokenResponse(jwtService.generateToken(user), user.getId());
	}

	@PostMapping("/register")
	public TokenResponse register(@RequestBody RegisterRequest body) {
		if (body.username() == null || body.username().isBlank() || body.password() == null
				|| body.password().length() < 4) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username and password (min 4 chars) required");
		}
		String username = body.username().trim();
		if (appUserRepository.existsByUsername(username)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "username already taken");
		}
		AppUser saved = appUserRepository.save(new AppUser(username, passwordEncoder.encode(body.password())));
		AuthenticatedUser principal = AuthenticatedUser.fromEntity(saved);
		return new TokenResponse(jwtService.generateToken(principal), saved.getId());
	}

	public record LoginRequest(String username, String password) {
	}

	public record RegisterRequest(String username, String password) {
	}

	public record TokenResponse(String token, java.util.UUID userId) {
	}
}
