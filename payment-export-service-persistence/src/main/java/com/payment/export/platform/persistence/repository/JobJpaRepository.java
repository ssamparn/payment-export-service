package com.payment.export.platform.persistence.repository;

import com.payment.export.platform.persistence.entity.JobEntity;
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

public interface JobJpaRepository extends JpaRepository<JobEntity, UUID> {

	@Query(value = """
			select j.*
			from job j
			where j.status in (:retryableStatuses)
			   or (j.status = :inFlightStatus and j.updated_at < :staleUpdatedBefore)
			order by j.created_at asc
			limit :limit
			for update of j skip locked
			""", nativeQuery = true)
	List<JobEntity> claimEligibleForBatchFetch(@Param("retryableStatuses") Collection<String> retryableStatuses,
											   @Param("inFlightStatus") String inFlightStatus,
											   @Param("staleUpdatedBefore") OffsetDateTime staleUpdatedBefore,
											   @Param("limit") int limit);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<JobEntity> findByJobId(UUID jobId);
}


