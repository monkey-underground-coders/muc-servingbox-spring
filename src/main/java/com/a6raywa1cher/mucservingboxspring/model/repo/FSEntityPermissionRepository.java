package com.a6raywa1cher.mucservingboxspring.model.repo;

import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface FSEntityPermissionRepository extends CrudRepository<FSEntityPermission, Long> {
	@Query("from FSEntityPermission p, FSEntity e where :fsEntityPath like concat(e.path, '%')")
	Stream<FSEntityPermission> getPermissions(String fsEntityPath);

	@Query("from FSEntityPermission p inner join p.affectedUserRoles r, FSEntity e, User u " +
		"where :fsEntityPath like concat(e.path, '%') and (u.id = :userId or r = :userRole) and bitwise_and(p.mask, :mask) <> 0 order by length(e.path) desc")
	FSEntityPermission getTopPermission(String fsEntityPath, long userId, UserRole userRole, int mask);
}
