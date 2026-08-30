package com.payment.export.platform.soap.config;

import com.payment.export.platform.soap.model.req.AccountReq;
import com.payment.export.platform.soap.model.req.GetBatchReq;
import com.payment.export.platform.soap.model.rpy.BatchRpy;
import com.payment.export.platform.soap.model.rpy.GetBatchRpy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class GetBatchSoapClientConfig {

    @Bean
    public Jaxb2Marshaller getBatchJaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(AccountReq.class, GetBatchReq.class, GetBatchRpy.class, BatchRpy.class);
        return marshaller;
    }

}

