package com.payment.export.platform.persistence.repository;

import com.payment.export.platform.persistence.entity.JobEntity;
import com.payment.export.platform.persistence.entity.JobStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import java.util.UUID;

public interface JobJpaRepository extends JpaRepository<JobEntity, UUID> {

	@Query("""
			select j
			from JobEntity j
			where j.status in :retryableStatuses
			   or (j.status = :inFlightStatus and j.updatedAt < :staleUpdatedBefore)
			order by j.createdAt asc
			""")
	List<JobEntity> findEligibleForBatchFetch(@Param("retryableStatuses") Collection<JobStatus> retryableStatuses,
											 @Param("inFlightStatus") JobStatus inFlightStatus,
											 @Param("staleUpdatedBefore") OffsetDateTime staleUpdatedBefore,
											 Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<JobEntity> findByJobId(UUID jobId);
}


