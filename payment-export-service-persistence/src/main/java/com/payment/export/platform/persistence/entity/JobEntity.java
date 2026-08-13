package com.payment.export.platform.persistence.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "job")
public class JobEntity {

    @Id
    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_agreement_id")
    private String customerAgreementId;

    @Column(name = "job_type", nullable = false)
    private String type;

    @Column(name = "date_from", nullable = false)
    private LocalDate dateFrom;

    @Column(name = "date_to", nullable = false)
    private LocalDate dateTo;

    @Column(name = "payment_type", nullable = false)
    private String paymentType;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "account_ibans", nullable = false, columnDefinition = "TEXT[]")
    private String[] accountIbans;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "account_currency_codes", nullable = false, columnDefinition = "TEXT[]")
    private String[] accountCurrencyCodes;

    @Column(name = "jwt_token", nullable = false, columnDefinition = "TEXT")
    private String jwtToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status;

    @Column(name = "total_batches", nullable = false)
    private Integer totalBatches;

    @Column(name = "processed_batches", nullable = false)
    private Integer processedBatches;

    @Column(name = "total_transactions", nullable = false)
    private Integer totalTransactions;

    @Column(name = "processed_transactions", nullable = false)
    private Integer processedTransactions;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "last_batch_page_processed", nullable = false)
    private Integer lastBatchPageProcessed;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "csv_file_location")
    private String csvFileLocation;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String[] getAccountIbans() {
        return accountIbans;
    }

    public void setAccountIbans(String[] accountIbans) {
        this.accountIbans = accountIbans;
    }

    public String[] getAccountCurrencyCodes() {
        return accountCurrencyCodes;
    }

    public void setAccountCurrencyCodes(String[] accountCurrencyCodes) {
        this.accountCurrencyCodes = accountCurrencyCodes;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Integer getTotalBatches() {
        return totalBatches;
    }

    public void setTotalBatches(Integer totalBatches) {
        this.totalBatches = totalBatches;
    }

    public Integer getProcessedBatches() {
        return processedBatches;
    }

    public void setProcessedBatches(Integer processedBatches) {
        this.processedBatches = processedBatches;
    }

    public Integer getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Integer totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public Integer getProcessedTransactions() {
        return processedTransactions;
    }

    public void setProcessedTransactions(Integer processedTransactions) {
        this.processedTransactions = processedTransactions;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getLastBatchPageProcessed() {
        return lastBatchPageProcessed;
    }

    public void setLastBatchPageProcessed(Integer lastBatchPageProcessed) {
        this.lastBatchPageProcessed = lastBatchPageProcessed;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getCsvFileLocation() {
        return csvFileLocation;
    }

    public void setCsvFileLocation(String csvFileLocation) {
        this.csvFileLocation = csvFileLocation;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAgreementId() {
        return customerAgreementId;
    }

    public void setCustomerAgreementId(String customerAgreementId) {
        this.customerAgreementId = customerAgreementId;
    }

    public Long getVersion() {
        return version;
    }
}

