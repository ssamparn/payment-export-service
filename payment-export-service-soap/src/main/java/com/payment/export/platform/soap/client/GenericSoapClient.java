package com.payment.export.platform.soap.client;

public interface GenericSoapClient<T, U> {
    U send(T request);
}

