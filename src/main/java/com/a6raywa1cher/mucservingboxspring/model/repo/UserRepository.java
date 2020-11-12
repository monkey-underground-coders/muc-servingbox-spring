package com.a6raywa1cher.mucservingboxspring.model.repo;

import com.a6raywa1cher.mucservingboxspring.model.QUser;
import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.querydsl.core.types.dsl.StringExpression;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Long>,
	QuerydslPredicateExecutor<User>,
	QuerydslBinderCustomizer<QUser> {
	Optional<User> findByUsername(String username);

	Optional<User> findFirstByUserRole(UserRole userRole);

	List<User> findTop1ByUserRole(UserRole userRole, Pageable pageable);

	@Override
	default void customize(QuerydslBindings bindings, QUser root) {
		bindings.bind(root.name).first(StringExpression::containsIgnoreCase);
		bindings.excluding(root.password, root.rootFolder);
	}
}
