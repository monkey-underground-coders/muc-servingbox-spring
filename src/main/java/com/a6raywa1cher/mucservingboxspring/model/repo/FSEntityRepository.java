package com.a6raywa1cher.mucservingboxspring.model.repo;

import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface FSEntityRepository extends CrudRepository<FSEntity, Long> {
	Stream<FSEntity> findByPath(String path);

	@Query("from FSEntity e where e.path like concat(:path, '%')")
	List<FSEntity> getTreeByPath(@Param("path") String path);

	@Query("from FSEntity e where e.path like concat(:path, '%') and e.isFolder = false")
	List<FSEntity> getFilesTreeByPath(@Param("path") String path);

	@Query("from FSEntity e where e.path like concat(:path, '%') and e.isFolder = true")
	List<FSEntity> getFoldersTreeByPath(@Param("path") String path);

	@Query("delete from FSEntity e where e.path like concat(:path, '%')")
	void deleteAllTree(@Param("path") String path);

	Optional<FSEntity> getById(Long id);
}
