package com.a6raywa1cher.mucservingboxspring.model.spec;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.User_;
import com.a6raywa1cher.mucservingboxspring.model.file.*;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

public interface FSEntityPermissionSpecifications {
	static Specification<FSEntity> hasAccess(User user, ActionType actionType) {
		return (e, cq, cb) -> {
			Subquery<Integer> integerSubquery = cq.subquery(Integer.class);
			{
				Root<FSEntityPermission> permissionRoot = integerSubquery.from(FSEntityPermission.class);
				ListJoin<FSEntityPermission, FSEntity> e1 = permissionRoot.join(FSEntityPermission_.entities, JoinType.LEFT);
				ListJoin<FSEntityPermission, User> u = permissionRoot.join(FSEntityPermission_.affectedUsers, JoinType.LEFT);
				integerSubquery
					.select(cb.length(e1.get(FSEntity_.path)))
					.where(cb.and(
						cb.like(e.get(FSEntity_.path), cb.concat(e1.get(FSEntity_.path), "%")),
						cb.or(
							cb.equal(u.get(User_.id), user.getId()),
							cb.isMember(user.getUserRole(), permissionRoot.joinCollection(FSEntityPermission_.AFFECTED_USER_ROLES))
						),
						permissionRoot.get(FSEntityPermission_.mask).in(actionType.allMasks)
					));
			}
			Subquery<Boolean> subquery = cq.subquery(Boolean.class);
			Root<FSEntityPermission> permissionRoot = subquery.from(FSEntityPermission.class);
			ListJoin<FSEntityPermission, FSEntity> join = permissionRoot.join(FSEntityPermission_.entities, JoinType.LEFT);
			subquery
				.select(cb.literal(true))
				.where(cb.and(
					cb.ge(cb.length(join.get(FSEntity_.path)), cb.max(integerSubquery)),
					cb.equal(permissionRoot.get(FSEntityPermission_.allow), true)
				));
			return cb.exists(subquery);
		};
	}
}
