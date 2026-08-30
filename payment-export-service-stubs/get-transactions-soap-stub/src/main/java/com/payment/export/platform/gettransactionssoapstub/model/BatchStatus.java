package com.payment.export.platform.gettransactionssoapstub.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "BatchStatus")
@XmlEnum
public enum BatchStatus {
    CREATED,
    PROCESSING,
    COMPLETED,
    FAILED
}

