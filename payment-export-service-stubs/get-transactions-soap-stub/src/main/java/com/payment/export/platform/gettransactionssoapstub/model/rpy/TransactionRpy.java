package com.payment.export.platform.gettransactionssoapstub.model.rpy;

import com.payment.export.platform.gettransactionssoapstub.model.BatchStatus;
import com.payment.export.platform.gettransactionssoapstub.model.PaymentType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.math.BigDecimal;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "TransactionRpy",
        propOrder = {
                "transactionId",
                "batchId",
                "batchName",
                "paymentType",
                "batchStatus",
                "accountHolderName",
                "transactionAmount",
                "currencyCode"
        }
)
public class TransactionRpy {

    @XmlElement(required = true)
    private String transactionId;

    @XmlElement(required = true)
    private String batchId;

    @XmlElement(required = true)
    private String batchName;

    @XmlElement(required = true)
    private PaymentType paymentType;

    @XmlElement(required = true)
    private BatchStatus batchStatus;

    @XmlElement(required = true)
    private String accountHolderName;

    @XmlElement(required = true)
    private BigDecimal transactionAmount;

    @XmlElement(required = true)
    private String currencyCode;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public BatchStatus getBatchStatus() {
        return batchStatus;
    }

    public void setBatchStatus(BatchStatus batchStatus) {
        this.batchStatus = batchStatus;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}

