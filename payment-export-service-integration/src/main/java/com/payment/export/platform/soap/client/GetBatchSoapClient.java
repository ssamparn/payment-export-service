package com.payment.export.platform.soap.client;

import com.payment.export.platform.soap.model.req.GetBatchReq;
import com.payment.export.platform.soap.model.rpy.GetBatchRpy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

@Component
public class GetBatchSoapClient extends AbstractGenericSoapClient<GetBatchReq, GetBatchRpy> {

    public GetBatchSoapClient(Jaxb2Marshaller getBatchJaxb2Marshaller,
                              @Value("${payment-export.soap.get-batch.uri:http://localhost:6060/ws}") String uri) {
        setDefaultUri(uri);
        setMarshaller(getBatchJaxb2Marshaller);
        setUnmarshaller(getBatchJaxb2Marshaller);
    }

    @Override
    protected Object toSoapRequest(GetBatchReq request) {
        return request;
    }

    @Override
    protected GetBatchRpy toDomainResponse(GetBatchReq request, Object soapResponse) {
        if (!(soapResponse instanceof GetBatchRpy getBatchRpy)) {
            throw new IllegalStateException("Unexpected SOAP response type: " +
                    (soapResponse == null ? "null" : soapResponse.getClass().getName()));
        }
        return getBatchRpy;
    }
}

