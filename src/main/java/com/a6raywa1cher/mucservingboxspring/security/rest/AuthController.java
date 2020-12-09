package com.a6raywa1cher.mucservingboxspring.security.rest;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.security.jpa.RefreshToken;
import com.a6raywa1cher.mucservingboxspring.security.jwt.JwtRefreshPair;
import com.a6raywa1cher.mucservingboxspring.security.jwt.service.JwtRefreshPairService;
import com.a6raywa1cher.mucservingboxspring.security.jwt.service.RefreshTokenService;
import com.a6raywa1cher.mucservingboxspring.security.rest.req.GetNewJwtTokenRequest;
import com.a6raywa1cher.mucservingboxspring.security.rest.req.InvalidateTokenRequest;
import com.a6raywa1cher.mucservingboxspring.utils.AuthenticationResolver;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final RefreshTokenService refreshTokenService;

	private final AuthenticationResolver authenticationResolver;

	private final JwtRefreshPairService jwtRefreshPairService;

	public AuthController(AuthenticationResolver authenticationResolver, RefreshTokenService refreshTokenService,
						  JwtRefreshPairService jwtRefreshPairService) {
		this.authenticationResolver = authenticationResolver;
		this.refreshTokenService = refreshTokenService;
		this.jwtRefreshPairService = jwtRefreshPairService;
	}

	@GetMapping("/user")
	@JsonView(Views.DetailedInternal.class)
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<User> getCurrentUser(@Parameter(hidden = true) User user) {
		return ResponseEntity.ok(user);
	}

	@PostMapping("/convert")
	@Operation(security = @SecurityRequirement(name = "basic"))
	public ResponseEntity<JwtRefreshPair> convertToJwt(HttpServletRequest request, Authentication authentication) {
		if (authentication instanceof UsernamePasswordAuthenticationToken) {
			User user = authenticationResolver.getUser();
			JwtRefreshPair pair = jwtRefreshPairService.issue(user);
			SecurityContextHolder.clearContext();
			request.getSession().invalidate();
			return ResponseEntity.ok(pair);
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/get_access")
	public ResponseEntity<JwtRefreshPair> getNewJwtToken(@RequestBody @Valid GetNewJwtTokenRequest request) {
		Optional<RefreshToken> optional = refreshTokenService.getByToken(request.getRefreshToken());
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		refreshTokenService.invalidate(optional.get());
		User user = optional.get().getUser();
		return ResponseEntity.ok(jwtRefreshPairService.issue(user));
	}

	@DeleteMapping("/invalidate")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<Void> invalidateToken(@RequestBody @Valid InvalidateTokenRequest request) {
		User user = authenticationResolver.getUser();
		Optional<RefreshToken> optional = refreshTokenService.getByToken(request.getRefreshToken());
		if (optional.isPresent()) {
			RefreshToken refreshToken = optional.get();
			if (user.equals(refreshToken.getUser())) {
				refreshTokenService.invalidate(refreshToken);
			}
		}
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/invalidate_all")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<Void> invalidateAllTokens() {
		User user = authenticationResolver.getUser();
		refreshTokenService.invalidateAll(user);
		return ResponseEntity.ok().build();
	}
}
