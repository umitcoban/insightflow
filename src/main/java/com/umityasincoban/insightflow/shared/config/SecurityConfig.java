package com.umityasincoban.insightflow.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(AbstractHttpConfigurer::disable)	.authorizeHttpRequests(auth ->
				auth.requestMatchers("/actuator/health").permitAll()
						.requestMatchers("/api/v1/tenants/**").permitAll()
						.requestMatchers("/api/v1/feedbacks/**").permitAll()
						.requestMatchers("/api/v1/customers/**").permitAll()
						.anyRequest().authenticated()
		)
		.build();
	}
	
}
