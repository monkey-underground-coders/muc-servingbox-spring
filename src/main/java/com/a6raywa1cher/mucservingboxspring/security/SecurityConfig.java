package com.a6raywa1cher.mucservingboxspring.security;

import com.a6raywa1cher.mucservingboxspring.config.AppConfigProperties;
import com.a6raywa1cher.mucservingboxspring.security.jwt.JwtAuthenticationFilter;
import com.a6raywa1cher.mucservingboxspring.security.jwt.service.BlockedRefreshTokensService;
import com.a6raywa1cher.mucservingboxspring.security.jwt.service.JwtTokenService;
import com.a6raywa1cher.mucservingboxspring.security.providers.JwtAuthenticationProvider;
import com.a6raywa1cher.mucservingboxspring.security.providers.UsernamePasswordAuthenticationProvider;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import com.a6raywa1cher.mucservingboxspring.utils.AuthenticationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private final UserService userService;

	private final AppConfigProperties appConfigProperties;

	private final JwtTokenService jwtTokenService;

	private final AuthenticationResolver authenticationResolver;

	private final PasswordEncoder passwordEncoder;

	private final BlockedRefreshTokensService blockedRefreshTokensService;

	@Autowired
	public SecurityConfig(UserService userService, JwtTokenService jwtTokenService,
						  AuthenticationResolver authenticationResolver, AppConfigProperties appConfigProperties,
						  PasswordEncoder passwordEncoder, BlockedRefreshTokensService blockedRefreshTokensService) {
		this.userService = userService;
		this.appConfigProperties = appConfigProperties;
		this.jwtTokenService = jwtTokenService;
		this.authenticationResolver = authenticationResolver;
		this.passwordEncoder = passwordEncoder;
		this.blockedRefreshTokensService = blockedRefreshTokensService;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) {
		auth
			.authenticationProvider(new JwtAuthenticationProvider(userService, blockedRefreshTokensService))
			.authenticationProvider(new UsernamePasswordAuthenticationProvider(userService, passwordEncoder));
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.authorizeRequests()
			.antMatchers("/").permitAll()
			.antMatchers("/user/create").hasRole("ADMIN")
			.antMatchers("/user/temp").permitAll()
			.antMatchers(HttpMethod.GET, "/user/").hasRole("MODERATOR")
			.antMatchers(HttpMethod.GET, "/user/{uid:[0-9]+}/internal").hasRole("ADMIN")
			.antMatchers("/user/change_password").hasRole("CUSTOMER")
			.antMatchers(HttpMethod.DELETE, "/user/{uid:[0-9]+}").hasRole("ADMIN")
			.antMatchers("/v3/api-docs/**", "/webjars/**", "/swagger-resources", "/swagger-resources/**",
				"/swagger-ui.html", "/swagger-ui/**").permitAll()
			.antMatchers("/csrf").permitAll()
			.antMatchers("/ws-entry").permitAll()
			.antMatchers("/auth/convert").hasAuthority("CONVERTIBLE")
			.antMatchers("/auth/get_access").permitAll()
			.antMatchers("/logout").authenticated()
			.antMatchers("/favicon.ico").permitAll()
			.anyRequest().hasRole("USER")
			.and()
			.cors()
			.configurationSource(corsConfigurationSource(appConfigProperties));
		http
			.httpBasic()
			.and()
			.formLogin();
		http
			.exceptionHandling()
			.authenticationEntryPoint(new CustomAuthenticationEntryPoint());
		http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenService, authenticationManagerBean()), UsernamePasswordAuthenticationFilter.class);
		http.addFilterAfter(new LastVisitFilter(userService, authenticationResolver), SecurityContextHolderAwareRequestFilter.class);
//		http.addFilterBefore(new CriticalActionLimiterFilter(criticalActionLimiterService), JwtAuthenticationFilter.class);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(AppConfigProperties appConfigProperties) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(appConfigProperties.getCorsAllowedOrigins()));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PATCH", "PUT", "HEAD", "OPTIONS"));
		configuration.setAllowedHeaders(Collections.singletonList("*"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
