package com.payment.export.platform.getbatchsoapstub.model.rpy;

import com.payment.export.platform.getbatchsoapstub.model.PaymentType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "BatchRpy",
        propOrder = {
                "batchId",
                "batchName",
                "iban",
                "currencyCode",
                "paymentType"
        }
)
public class BatchRpy {

    @XmlElement(required = true)
    private String batchId;

    @XmlElement(required = true)
    private String batchName;

    @XmlElement(required = true)
    private String iban;

    @XmlElement(required = true)
    private String currencyCode;

    @XmlElement(required = true)
    private PaymentType paymentType;

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

}

