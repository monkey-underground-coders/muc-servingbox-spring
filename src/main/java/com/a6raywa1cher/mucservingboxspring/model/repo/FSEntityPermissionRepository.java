package com.a6raywa1cher.mucservingboxspring.model.repo;

import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FSEntityPermissionRepository extends CrudRepository<FSEntityPermission, Long> {
	@Query(nativeQuery = true,
		value = "select case when count(*) <> 0 then true else false end\n" +
			"from (select p.*, length(f.entities_path) as length\n" +
			"      from fsentity_permission p\n" +
			"               inner join fsentity_permission_entities f on p.id = f.fsentity_permission_id\n" +
			"      where f.entities_path in :paths) as p\n" +
			"         left join fsentity_permission_affected_users fpau on p.id = fpau.fsentity_permission_id\n" +
			"         left join \"user\" u on u.id = fpau.affected_users_id\n" +
			"         left join fsentity_permission_affected_user_roles fpaur on p.id = fpaur.fsentity_permission_id\n" +
			"where (u.id = :userId or fpaur.affected_user_roles = :userRole)\n" +
			"  and p.mask in :allMasks ;")
	boolean checkAccess(@Param("paths") List<String> paths, @Param("userId") long userId,
						@Param("userRole") int userRole, @Param("allMasks") List<Integer> allMasks);

	@Query(nativeQuery = true,
		value = "select p.id, application_defined, mask\n" +
			"from (select p.*\n" +
			"      from fsentity_permission p\n" +
			"               inner join fsentity_permission_entities f on p.id = f.fsentity_permission_id\n" +
			"      where f.entities_path in :paths) as p\n" +
			"         left join fsentity_permission_affected_users fpau on p.id = fpau.fsentity_permission_id\n" +
			"         left join \"user\" u on u.id = fpau.affected_users_id\n" +
			"         left join fsentity_permission_affected_user_roles fpaur on p.id = fpaur.fsentity_permission_id\n" +
			"where (u.id = :userId or fpaur.affected_user_roles = :userRole)\n" +
			"  and p.mask in :allMasks ;")
	Optional<FSEntityPermission> checkAccessWithReturn(@Param("paths") List<String> paths, @Param("userId") long userId,
													   @Param("userRole") int userRole, @Param("allMasks") List<Integer> allMasks);

	@Query("select p from FSEntityPermission p, FSEntity e where e.path like concat(:path, '%')")
	List<FSEntityPermission> getAllByPath(@Param("path") String path);

	@Query(nativeQuery = true,
		value =
			"select p\n" +
				"from fsentity_permission as p\n" +
				"         left join fsentity_permission_affected_user_roles fpaur on p.id = fpaur.fsentity_permission_id\n" +
				"         left join fsentity_permission_entities fpe on p.id = fpe.fsentity_permission_id\n" +
				"         left join fsentity f on f.id = fpe.entities_id\n" +
				"         left join fsentity_permission_affected_users fpau on p.id = fpau.fsentity_permission_id\n" +
				"         left join \"user\" u on u.id = fpau.affected_users_id\n" +
				"where f.path in :paths\n" +
				"  and (u.id = :userId or fpaur.affected_user_roles = :userRole)\n" +
				"  and p.mask in :allMasks ;")
	List<FSEntityPermission> getAllByPathAccordingTo(@Param("paths") List<String> paths, @Param("userId") long userId,
													 @Param("userRole") UserRole userRole, @Param("allMasks") List<Integer> allMasks);
}
