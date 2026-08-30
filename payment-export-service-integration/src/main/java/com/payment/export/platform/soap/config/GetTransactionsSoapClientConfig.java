package com.payment.export.platform.soap.config;

import com.payment.export.platform.soap.model.BatchStatus;
import com.payment.export.platform.soap.model.PaymentType;
import com.payment.export.platform.soap.model.req.GetTransactionsReq;
import com.payment.export.platform.soap.model.rpy.GetTransactionsRpy;
import com.payment.export.platform.soap.model.rpy.TransactionRpy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class GetTransactionsSoapClientConfig {

    @Bean
    public Jaxb2Marshaller getTransactionsJaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(
                GetTransactionsReq.class,
                GetTransactionsRpy.class,
                TransactionRpy.class,
                PaymentType.class,
                BatchStatus.class
        );
        return marshaller;
    }
}

