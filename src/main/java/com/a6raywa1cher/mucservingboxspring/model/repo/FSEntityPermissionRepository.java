package com.a6raywa1cher.mucservingboxspring.model.repo;

import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface FSEntityPermissionRepository extends CrudRepository<FSEntityPermission, Long> {
	@Query("select case when count(p) <> 0 then true else false end \n" +
		"from FSEntityPermission p left join p.affectedUserRoles r left join p.affectedUsers u\n" +
		"where (u.id = :userId or r = :userRole) and p.mask in :allMasks and p.entity is not null and p.entity.path in :paths " +
		"and p.startAt < :now and p.endAt > :now")
	boolean checkAccess(@Param("paths") List<String> paths, @Param("userId") long userId,
						@Param("userRole") UserRole userRole, @Param("allMasks") List<Integer> allMasks, @Param("now") ZonedDateTime now);

	@Query("select p \n" +
		"from FSEntityPermission p left join p.affectedUserRoles r left join p.affectedUsers u\n" +
		"where (u.id = :userId or r = :userRole) and p.entity is not null and p.entity.path in :paths " +
		"and p.startAt < :now and p.endAt > :now")
	List<FSEntityPermission> getAllActiveApplicableToEntity(@Param("paths") List<String> paths, @Param("userId") long userId,
															@Param("userRole") UserRole userRole, @Param("now") ZonedDateTime now);

	@Query("select p from FSEntityPermission p, FSEntity e where e.path like concat(:path, '%')")
	List<FSEntityPermission> getAllByPath(@Param("path") String path);

	List<FSEntityPermission> getByEntity(FSEntity fsEntity);

	@Query("from FSEntityPermission p left join p.affectedUserRoles r left join p.affectedUsers u " +
		"where (u.id = :userId or r = :userRole) and p.mask in :allMasks and p.entity is not null " +
		"and p.startAt < :now and p.endAt > :now")
	List<FSEntityPermission> getActiveByUserAndMasks(@Param("userRole") UserRole userRole, @Param("userId") long userId,
													 @Param("allMasks") List<Integer> allMasks, @Param("now") ZonedDateTime now);
}
