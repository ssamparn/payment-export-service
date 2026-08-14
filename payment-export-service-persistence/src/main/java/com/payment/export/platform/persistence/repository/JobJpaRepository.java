package com.payment.export.platform.persistence.repository;

import com.payment.export.platform.persistence.entity.JobEntity;
import com.payment.export.platform.persistence.entity.JobStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

import java.util.UUID;

public interface JobJpaRepository extends JpaRepository<JobEntity, UUID> {

	List<JobEntity> findByStatusOrderByCreatedAtAsc(JobStatus status, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<JobEntity> findByJobId(UUID jobId);
}


