package com.payment.export.platform.persistence.repository;

import com.payment.export.platform.persistence.entity.BatchEntity;
import com.payment.export.platform.persistence.entity.BatchJobStatus;
import jakarta.persistence.LockModeType;
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

	@Query(value = """
			select b.*
			from batch b
			join job j on j.job_id = b.job_id
			where j.status in (:jobStatuses)
			  and (b.status in (:retryableStatuses)
				   or (b.status = :inFlightStatus and b.updated_at < :staleUpdatedBefore))
			order by j.created_at asc, b.created_at asc
			limit :limit
			for update of b skip locked
			""", nativeQuery = true)
	List<BatchEntity> claimEligibleForTransactionFetch(@Param("jobStatuses") Collection<String> jobStatuses,
													   @Param("retryableStatuses") Collection<String> retryableStatuses,
													   @Param("inFlightStatus") String inFlightStatus,
													   @Param("staleUpdatedBefore") OffsetDateTime staleUpdatedBefore,
													   @Param("limit") int limit);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<BatchEntity> findByBatchId(UUID batchId);

	List<BatchEntity> findByJob_JobIdAndInternalBatchIdIn(UUID jobId, Collection<String> internalBatchIds);

	long countByJob_JobId(UUID jobId);

	@Query("""
			select count(b)
			from BatchEntity b
			where b.job.jobId = :jobId
			  and b.status <> :completedStatus
			""")
	long countIncompleteByJobId(@Param("jobId") UUID jobId, @Param("completedStatus") BatchJobStatus completedStatus);

}

