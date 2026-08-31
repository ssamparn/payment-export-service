package com.payment.export.platform.persistence.mapper;

import com.payment.export.platform.domain.dto.soap.response.GetTransactionsResponse;
import com.payment.export.platform.persistence.entity.BatchEntity;
import com.payment.export.platform.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TransactionsDataAccessMapper {

    public List<TransactionEntity> toTransactionEntities(BatchEntity batchEntity, List<GetTransactionsResponse.TransactionDetails> transactions) {
        return transactions.stream()
                .map(transaction -> toTransactionEntity(batchEntity, transaction))
                .toList();
    }

    private TransactionEntity toTransactionEntity(BatchEntity batchEntity, GetTransactionsResponse.TransactionDetails transaction) {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId(UUID.randomUUID());
        transactionEntity.setTransactionId(transaction.transactionId());
        transactionEntity.setBatchName(transaction.batchName());
        transactionEntity.setPaymentType(transaction.paymentType());
        transactionEntity.setBatchStatus(transaction.batchStatus());
        transactionEntity.setAccountHolderName(transaction.accountHolderName());
        transactionEntity.setTransactionAmount(transaction.transactionAmount());
        transactionEntity.setCurrencyCode(transaction.currencyCode());
        batchEntity.addTransaction(transactionEntity);
        return transactionEntity;
    }
}

