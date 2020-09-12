package com.a6raywa1cher.mucservingboxspring.model.repo.impl;

import com.a6raywa1cher.mucservingboxspring.model.repo.AdditionalFSEntityRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class AdditionalFSEntityRepositoryImpl implements AdditionalFSEntityRepository {
	@PersistenceContext
	private EntityManager em;

}
