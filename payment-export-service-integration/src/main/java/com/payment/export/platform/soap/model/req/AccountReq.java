package com.payment.export.platform.soap.model.req;

import com.payment.export.platform.soap.model.GetBatchSoapConstants;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccountReq", propOrder = {"iban", "currencyCode"})
public class AccountReq {

    @XmlElement(required = true, namespace = GetBatchSoapConstants.NAMESPACE_URI)
    private String iban;

    @XmlElement(required = true, namespace = GetBatchSoapConstants.NAMESPACE_URI)
    private String currencyCode;

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
}

