package com.a6raywa1cher.mucservingboxspring.model.repo;

import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FSEntityRepository extends CrudRepository<FSEntity, Long> {
	Optional<FSEntity> findByPath(String path);

	@Query("from FSEntity e where e.path like concat(:path, '%')")
	List<FSEntity> findChildrenByPath(@Param("path") String path);

	@Query("from FSEntity e where e.path like concat(:path, '%') and not e.isFolder")
	List<FSEntity> findChildrenFilesByPath(@Param("path") String path);

	@Query("from FSEntity e where e.path like concat(:path, '%') and e.isFolder")
	List<FSEntity> findChildrenFoldersByPath(@Param("path") String path);
}
