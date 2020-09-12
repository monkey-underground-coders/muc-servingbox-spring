package com.a6raywa1cher.mucservingboxspring.model.repo;

import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface FSEntityPermissionRepository extends CrudRepository<FSEntityPermission, Long> {
	@Query("from FSEntityPermission p, FSEntity e where :fsEntityPath like concat(e.path, '%')")
	Stream<FSEntityPermission> getPermissions(@Param("fsEntityPath") String fsEntityPath);

	@Query("from FSEntityPermission p inner join p.affectedUserRoles r, FSEntity e, User u " +
		"where :fsEntityPath like concat(e.path, '%') and (u.id = :userId or r = :userRole) and " +
		"bitwise_and(p.mask,:mask) <> 0 order by length(e.path) desc")
	List<FSEntityPermission> getPermissions(@Param("fsEntityPath") String fsEntityPath, @Param("userId") long userId,
											@Param("userRole") UserRole userRole, @Param("mask") int mask);


	// TODO: optimization. on 1M records in each table - 700ms
	@Query(nativeQuery = true,
		value = "select case when count(*) <> 0 then true else false end \n" +
			"from (\n" +
			"         select *\n" +
			"         from fsentity_permission as p\n" +
			"                  left join fsentity_permission_affected_user_roles fpaur on p.id = fpaur.fsentity_permission_id\n" +
			"                  left join fsentity_permission_entities fpe on p.id = fpe.fsentity_permission_id\n" +
			"                  left join fsentity f on f.id = fpe.entities_id\n" +
			"                  left join fsentity_permission_affected_users fpau on p.id = fpau.fsentity_permission_id\n" +
			"                  left join \"user\" u on u.id = fpau.affected_users_id\n" +
			"         where :fsEntityPath like f.path || '%'\n" +
			"           and (u.id = :userId or fpaur.affected_user_roles = :userRole)\n" +
			"           and p.mask & :mask = :mask\n" +
			"         order by length(f.path) desc\n" +
			"         limit 1\n" +
			"     ) as p\n" +
			"where p.allow = true;")
	boolean checkAccess(@Param("fsEntityPath") String fsEntityPath, @Param("userId") long userId,
						@Param("userRole") UserRole userRole, @Param("mask") int mask);
}
