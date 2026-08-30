package com.payment.export.platform.soap.model.rpy;

import com.payment.export.platform.soap.model.GetBatchSoapConstants;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetBatchRpy", propOrder = {"requestId", "page", "pageSize", "moreResultsAvailable", "batches"})
@XmlRootElement(name = "GetBatchRpy", namespace = GetBatchSoapConstants.NAMESPACE_URI)
public class GetBatchRpy {

    @XmlElement(required = true)
    private String requestId;

    @XmlElement(required = true)
    private Integer page;

    @XmlElement(required = true)
    private Integer pageSize;

    @XmlElement(required = true)
    private boolean moreResultsAvailable;

    @XmlElement(name = "batch", required = true)
    private List<BatchRpy> batches = new ArrayList<>();

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public List<BatchRpy> getBatches() {
        return batches;
    }

    public void setBatches(List<BatchRpy> batches) {
        this.batches = batches;
    }
}
