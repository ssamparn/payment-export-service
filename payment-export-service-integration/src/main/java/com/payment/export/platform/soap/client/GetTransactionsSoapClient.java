package com.payment.export.platform.soap.client;

import com.payment.export.platform.soap.model.req.GetTransactionsReq;
import com.payment.export.platform.soap.model.rpy.GetTransactionsRpy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

@Component
public class GetTransactionsSoapClient extends AbstractGenericSoapClient<GetTransactionsReq, GetTransactionsRpy> {

    public GetTransactionsSoapClient(Jaxb2Marshaller getTransactionsJaxb2Marshaller,
                                     @Value("${payment-export.soap.get-transactions.uri:http://localhost:7070/ws}") String uri) {
        setDefaultUri(uri);
        setMarshaller(getTransactionsJaxb2Marshaller);
        setUnmarshaller(getTransactionsJaxb2Marshaller);
    }

    @Override
    protected Object toSoapRequest(GetTransactionsReq request) {
        return request;
    }

    @Override
    protected GetTransactionsRpy toDomainResponse(GetTransactionsReq request, Object soapResponse) {
        if (!(soapResponse instanceof GetTransactionsRpy getTransactionsRpy)) {
            throw new IllegalStateException("Unexpected SOAP response type: " +
                    (soapResponse == null ? "null" : soapResponse.getClass().getName()));
        }
        return getTransactionsRpy;
    }
}

