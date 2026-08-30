package com.payment.export.platform.gettransactionssoapstub.model.rpy;

import com.payment.export.platform.gettransactionssoapstub.GetTransactionsSoapConstants;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetTransactionsRpy", propOrder = {"requestId", "batchId", "batchName", "page", "pageSize", "moreResultsAvailable", "transactions"})
@XmlRootElement(name = "GetTransactionsRpy", namespace = GetTransactionsSoapConstants.NAMESPACE_URI)
public class GetTransactionsRpy {

    @XmlElement(required = true)
    private String requestId;

    @XmlElement(required = true)
    private String batchId;

    @XmlElement(required = true)
    private String batchName;

    @XmlElement(required = true)
    private Integer page;

    @XmlElement(required = true)
    private Integer pageSize;

    @XmlElement(required = true)
    private boolean moreResultsAvailable;

    @XmlElement(name = "transaction", required = true)
    private List<TransactionRpy> transactions = new ArrayList<>();

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public boolean isMoreResultsAvailable() {
        return moreResultsAvailable;
    }

    public void setMoreResultsAvailable(boolean moreResultsAvailable) {
        this.moreResultsAvailable = moreResultsAvailable;
    }

    public List<TransactionRpy> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionRpy> transactions) {
        this.transactions = transactions;
    }
}

