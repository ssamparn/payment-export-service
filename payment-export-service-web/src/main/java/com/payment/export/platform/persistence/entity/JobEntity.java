package com.payment.export.platform.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "job")
public class JobEntity {

    @Id
    private UUID jobId;

    private String userId;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private String type;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private Integer totalBatches;

    private Integer processedBatches;

    private Integer totalTransactions;

    private Integer processedTransactions;

    private Integer retryCount;

    private Integer lastProcessedPage;

    private String csvFileLocation;

    @Version
    private Long version;
}
