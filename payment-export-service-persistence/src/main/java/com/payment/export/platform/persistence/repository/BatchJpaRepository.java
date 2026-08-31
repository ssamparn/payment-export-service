package com.payment.export.platform.persistence.repository;

import com.payment.export.platform.persistence.entity.BatchEntity;
import com.payment.export.platform.persistence.entity.BatchJobStatus;
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

public interface BatchJpaRepository extends JpaRepository<BatchEntity, UUID> {

	@Query("""
			select b
			from BatchEntity b
			join fetch b.job j
			where j.status in :jobStatuses
			  and (b.status in :retryableStatuses
				   or (b.status = :inFlightStatus and b.updatedAt < :staleUpdatedBefore))
			order by j.createdAt asc, b.createdAt asc
			""")
	List<BatchEntity> findEligibleForTransactionFetch(@Param("jobStatuses") Collection<JobStatus> jobStatuses,
													  @Param("retryableStatuses") Collection<BatchJobStatus> retryableStatuses,
													  @Param("inFlightStatus") BatchJobStatus inFlightStatus,
													  @Param("staleUpdatedBefore") OffsetDateTime staleUpdatedBefore,
													  Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<BatchEntity> findByBatchId(UUID batchId);

	@Query("""
			select count(b)
			from BatchEntity b
			where b.job.jobId = :jobId
			  and b.status <> :completedStatus
			""")
	long countIncompleteByJobId(@Param("jobId") UUID jobId, @Param("completedStatus") BatchJobStatus completedStatus);

}

