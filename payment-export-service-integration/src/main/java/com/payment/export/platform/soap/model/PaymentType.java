package com.payment.export.platform.soap.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "PaymentType")
@XmlEnum
public enum PaymentType {
    CT,
    DD
}

