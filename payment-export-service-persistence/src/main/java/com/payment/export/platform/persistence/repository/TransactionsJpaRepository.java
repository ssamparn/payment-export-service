package com.payment.export.platform.persistence.repository;

import com.payment.export.platform.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TransactionsJpaRepository extends JpaRepository<TransactionEntity, UUID> {

	List<TransactionEntity> findByBatch_BatchId(UUID batchId);

	List<TransactionEntity> findByBatch_BatchIdAndTransactionIdIn(UUID batchId, Collection<String> transactionIds);
}
