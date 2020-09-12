package com.a6raywa1cher.mucservingboxspring.model.spec;

import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity_;
import org.springframework.data.jpa.domain.Specification;

public interface FSEntitySpecifications {
	static Specification<FSEntity> child(FSEntity parent) {
		return (e, cq, cb) -> cb.like(e.get(FSEntity_.path), parent.getPath() + '%');
	}

	static Specification<FSEntity> isFile() {
		return (e, cq, cb) -> cb.not(e.get(FSEntity_.isFolder));
	}

	static Specification<FSEntity> isFolder() {
		return (e, cq, cb) -> cb.not(e.get(FSEntity_.isFolder));
	}
}
