package com.payment.export.platform.getbatchsoapstub.model.req;

import com.payment.export.platform.getbatchsoapstub.GetBatchSoapConstants;
import com.payment.export.platform.getbatchsoapstub.model.PaymentType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetBatchReq", propOrder = {"paymentType", "page", "pageSize", "accounts"})
@XmlRootElement(name = "GetBatchReq", namespace = GetBatchSoapConstants.NAMESPACE_URI)
public class GetBatchReq {

    @XmlElement(required = true, namespace = GetBatchSoapConstants.NAMESPACE_URI)
    private PaymentType paymentType;

    @XmlElement(required = true, namespace = GetBatchSoapConstants.NAMESPACE_URI)
    private Integer page;

    @XmlElement(required = true, namespace = GetBatchSoapConstants.NAMESPACE_URI)
    private Integer pageSize;

    @XmlElement(name = "account", required = true, namespace = GetBatchSoapConstants.NAMESPACE_URI)
    private List<AccountReq> accounts = new ArrayList<>();

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<AccountReq> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountReq> accounts) {
        this.accounts = accounts;
    }
}

