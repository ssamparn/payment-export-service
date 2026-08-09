package com.payment.export.platform.persistence.entity;

public enum JobStatus {
    CREATED,
    FETCHING_BATCHES,
    BATCHES_FETCHED,
    FETCHING_TRANSACTIONS,
    TRANSACTIONS_FETCHED,
    GENERATING_CSV,
    CAN_BE_DOWNLOADED,
    FAILED
}