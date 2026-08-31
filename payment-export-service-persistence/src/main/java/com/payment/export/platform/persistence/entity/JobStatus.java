package com.payment.export.platform.persistence.entity;

public enum JobStatus {
    CREATED,
    FETCHING_BATCHES,
    BATCHES_FETCHED,
    BATCHES_FETCH_FAILED,
    FETCHING_TRANSACTIONS,
    TRANSACTIONS_FETCHED,
    GENERATING_CSV_LINK,
    GENERATING_CSV_FAILED,
    CAN_BE_DOWNLOADED,
    FAILED
}
