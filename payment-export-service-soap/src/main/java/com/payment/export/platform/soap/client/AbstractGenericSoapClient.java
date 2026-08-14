package com.payment.export.platform.soap.client;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import java.util.Objects;

public abstract class AbstractGenericSoapClient<T, U> extends WebServiceGatewaySupport implements GenericSoapClient<T, U> {

    @Override
    public U send(T request) {
        Object soapRequest = toSoapRequest(request);
        String defaultUri = Objects.requireNonNull(getDefaultUri(), "Default SOAP URI must be configured");
        Object soapResponse = getWebServiceTemplate().marshalSendAndReceive(defaultUri, soapRequest);
        return toDomainResponse(request, soapResponse);
    }

    protected abstract Object toSoapRequest(T request);

    protected abstract U toDomainResponse(T request, Object soapResponse);
}

