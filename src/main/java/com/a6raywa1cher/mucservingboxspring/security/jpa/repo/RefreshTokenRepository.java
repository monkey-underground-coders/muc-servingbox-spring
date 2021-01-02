package com.a6raywa1cher.mucservingboxspring.security.jpa.repo;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.security.jpa.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
	List<RefreshToken> findAllByUser(User user);

	Optional<RefreshToken> findByToken(String token);
}
