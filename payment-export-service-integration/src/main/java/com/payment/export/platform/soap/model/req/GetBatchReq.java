package com.payment.export.platform.soap.model.req;

import com.payment.export.platform.soap.model.GetBatchSoapConstants;
import com.payment.export.platform.soap.model.PaymentType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetBatchReq", propOrder = {"jobId", "paymentType", "page", "pageSize"})
@XmlRootElement(name = "GetBatchReq", namespace = GetBatchSoapConstants.NAMESPACE_URI)
public class GetBatchReq {

    @XmlElement(required = true, namespace = GetBatchSoapConstants.NAMESPACE_URI)
    private String jobId;

    @XmlElement(required = true, namespace = GetBatchSoapConstants.NAMESPACE_URI)
    private PaymentType paymentType;

    @XmlElement(required = true, namespace = GetBatchSoapConstants.NAMESPACE_URI)
    private Integer page;

    @XmlElement(required = true, namespace = GetBatchSoapConstants.NAMESPACE_URI)
    private Integer pageSize;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

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
}