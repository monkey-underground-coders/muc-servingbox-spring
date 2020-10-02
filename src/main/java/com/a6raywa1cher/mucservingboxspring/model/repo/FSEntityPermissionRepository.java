package com.a6raywa1cher.mucservingboxspring.model.repo;

import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FSEntityPermissionRepository extends CrudRepository<FSEntityPermission, Long> {
	@Query("select case when count(p) <> 0 then true else false end \n" +
		"from FSEntityPermission p left join p.affectedUserRoles r left join p.affectedUsers u left join p.entities e \n" +
		"where (u.id = :userId or r = :userRole) and p.mask in :allMasks and e.path in :paths")
	boolean checkAccess(@Param("paths") List<String> paths, @Param("userId") long userId,
						@Param("userRole") UserRole userRole, @Param("allMasks") List<Integer> allMasks);

	@Query("select p from FSEntityPermission p, FSEntity e where e.path like concat(:path, '%')")
	List<FSEntityPermission> getAllByPath(@Param("path") String path);
}
