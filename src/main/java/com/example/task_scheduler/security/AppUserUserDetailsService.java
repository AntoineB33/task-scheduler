package com.example.task_scheduler.security;

import com.example.task_scheduler.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserUserDetailsService implements UserDetailsService {

	private final AppUserRepository appUserRepository;

	public AppUserUserDetailsService(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return appUserRepository.findByUsername(username)
				.map(AuthenticatedUser::fromEntity)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}
}
