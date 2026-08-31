package com.payment.export.platform.persistence.entity;

import com.payment.export.platform.domain.dto.PaymentType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "batch",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_batch_job_internal_batch_id",
                        columnNames = {"job_id", "internal_batch_id"}
                )
        }
)
public class BatchEntity {

    @Id
    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false, foreignKey = @ForeignKey(name = "fk_batch_job"))
    private JobEntity job;

    @Column(name = "internal_batch_id", nullable = false, length = 128)
    private String internalBatchId;

    @Column(name = "iban", length = 34)
    private String iban;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 8)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 64)
    private BatchJobStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionEntity> transactions = new ArrayList<>();

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

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public JobEntity getJob() {
        return job;
    }

    public void setJob(JobEntity job) {
        this.job = job;
    }

    public String getInternalBatchId() {
        return internalBatchId;
    }

    public void setInternalBatchId(String internalBatchId) {
        this.internalBatchId = internalBatchId;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public BatchJobStatus getStatus() {
        return status;
    }

    public void setStatus(BatchJobStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        for (TransactionEntity transaction : this.transactions) {
            transaction.setBatch(null);
        }
        this.transactions.clear();

        if (transactions == null) {
            return;
        }

        for (TransactionEntity transaction : transactions) {
            addTransaction(transaction);
        }
    }

    public void addTransaction(TransactionEntity transaction) {
        if (transaction == null) {
            return;
        }

        boolean alreadyPresent = this.transactions.stream()
                .anyMatch(existing -> existing.getTransactionId() != null
                        && existing.getTransactionId().equals(transaction.getTransactionId()));
        if (alreadyPresent) {
            return;
        }

        this.transactions.add(transaction);
        transaction.setBatch(this);
    }

    public void removeTransaction(TransactionEntity transaction) {
        if (transaction == null) {
            return;
        }

        if (this.transactions.remove(transaction)) {
            transaction.setBatch(null);
        }
    }
}

