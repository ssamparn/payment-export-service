package com.payment.export.platform.gettransactionssoapstub.service;

import com.payment.export.platform.gettransactionssoapstub.model.BatchStatus;
import com.payment.export.platform.gettransactionssoapstub.model.PaymentType;
import com.payment.export.platform.gettransactionssoapstub.model.req.GetTransactionsReq;
import com.payment.export.platform.gettransactionssoapstub.model.rpy.GetTransactionsRpy;
import com.payment.export.platform.gettransactionssoapstub.model.rpy.TransactionRpy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class MockGetTransactionsSoapService {

    private static final int TOTAL_AVAILABLE_TRANSACTIONS = 240;
    private static final String DEFAULT_BATCH_ID = "INT-EUR-0001";

    public GetTransactionsRpy getTransactions(GetTransactionsReq request) {
        String normalizedBatchId = normalizeBatchId(request.getBatchId());
        int normalizedPage = Math.max(1, request.getPage() == null ? 1 : request.getPage());
        int normalizedPageSize = Math.max(1, request.getPageSize() == null ? 1 : request.getPageSize());
        String batchName = createBatchName(normalizedBatchId);

        GetTransactionsRpy response = new GetTransactionsRpy();
        response.setRequestId("REQ-TRX-" + normalizedBatchId + "-P" + normalizedPage + "-S" + normalizedPageSize);
        response.setBatchId(normalizedBatchId);
        response.setBatchName(batchName);
        response.setPage(normalizedPage);
        response.setPageSize(normalizedPageSize);
        response.setMoreResultsAvailable(hasMoreResultsAvailable(normalizedPage, normalizedPageSize));
        response.setTransactions(createTransactions(normalizedBatchId, batchName, normalizedPage, normalizedPageSize));
        return response;
    }

    private List<TransactionRpy> createTransactions(String batchId, String batchName, int page, int pageSize) {
        int startIndex = ((page - 1) * pageSize) + 1;
        if (startIndex > TOTAL_AVAILABLE_TRANSACTIONS) {
            return List.of();
        }

        int endIndex = Math.min(startIndex + pageSize - 1, TOTAL_AVAILABLE_TRANSACTIONS);
        List<TransactionRpy> transactions = new ArrayList<>(endIndex - startIndex + 1);
        PaymentType paymentType = resolvePaymentType(batchId);
        BatchStatus batchStatus = resolveBatchStatus(batchId);
        String currencyCode = resolveCurrencyCode(batchId);

        for (int sequence = startIndex; sequence <= endIndex; sequence++) {
            TransactionRpy transaction = new TransactionRpy();
            transaction.setTransactionId(batchId + "-TRX-" + String.format("%05d", sequence));
            transaction.setBatchId(batchId);
            transaction.setBatchName(batchName);
            transaction.setPaymentType(paymentType);
            transaction.setBatchStatus(batchStatus);
            transaction.setAccountHolderName("Account Holder " + String.format("%03d", sequence));
            transaction.setTransactionAmount(calculateAmount(sequence));
            transaction.setCurrencyCode(currencyCode);
            transactions.add(transaction);
        }
        return transactions;
    }

    private String normalizeBatchId(String batchId) {
        if (batchId == null || batchId.isBlank()) {
            return DEFAULT_BATCH_ID;
        }
        return batchId.trim();
    }

    private String createBatchName(String batchId) {
        return "BATCH-" + batchId;
    }

    private PaymentType resolvePaymentType(String batchId) {
        String normalized = batchId.toUpperCase(Locale.ROOT);
        if (normalized.contains("DD")) {
            return PaymentType.DD;
        }
        return PaymentType.CT;
    }

    private BatchStatus resolveBatchStatus(String batchId) {
        int selector = Math.floorMod(batchId.hashCode(), 4);
        return switch (selector) {
            case 0 -> BatchStatus.CREATED;
            case 1 -> BatchStatus.PROCESSING;
            case 2 -> BatchStatus.COMPLETED;
            default -> BatchStatus.FAILED;
        };
    }

    private String resolveCurrencyCode(String batchId) {
        String normalized = batchId.toUpperCase(Locale.ROOT);
        if (normalized.contains("USD")) {
            return "USD";
        }
        if (normalized.contains("GBP")) {
            return "GBP";
        }
        return "EUR";
    }

    private BigDecimal calculateAmount(int sequence) {
        return BigDecimal.valueOf(1000L + (sequence * 37L), 2)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean hasMoreResultsAvailable(int page, int pageSize) {
        return (long) page * pageSize < TOTAL_AVAILABLE_TRANSACTIONS;
    }
}


