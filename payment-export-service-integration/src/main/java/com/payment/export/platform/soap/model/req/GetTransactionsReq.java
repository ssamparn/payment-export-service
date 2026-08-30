package com.payment.export.platform.soap.model.req;

import com.payment.export.platform.soap.model.GetTransactionsSoapConstants;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetTransactionsReq", propOrder = {"batchId", "page", "pageSize"})
@XmlRootElement(name = "GetTransactionsReq", namespace = GetTransactionsSoapConstants.NAMESPACE_URI)
public class GetTransactionsReq {

    @XmlElement(required = true, namespace = GetTransactionsSoapConstants.NAMESPACE_URI)
    private String batchId;

    @XmlElement(required = true, namespace = GetTransactionsSoapConstants.NAMESPACE_URI)
    private Integer page;

    @XmlElement(required = true, namespace = GetTransactionsSoapConstants.NAMESPACE_URI)
    private Integer pageSize;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
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

