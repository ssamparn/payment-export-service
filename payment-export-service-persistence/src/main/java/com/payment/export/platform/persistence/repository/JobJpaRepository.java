package com.payment.export.platform.persistence.repository;

import com.payment.export.platform.persistence.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobJpaRepository extends JpaRepository<JobEntity, UUID> {

}


