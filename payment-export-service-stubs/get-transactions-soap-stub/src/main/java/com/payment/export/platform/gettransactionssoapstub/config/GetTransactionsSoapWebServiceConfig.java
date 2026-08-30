package com.payment.export.platform.gettransactionssoapstub.config;

import com.payment.export.platform.gettransactionssoapstub.GetTransactionsSoapConstants;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

@EnableWs
@Configuration
public class GetTransactionsSoapWebServiceConfig {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, GetTransactionsSoapConstants.LOCATION_URI + "/*");
    }

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan(
                "com.payment.export.platform.gettransactionssoapstub.model",
                "com.payment.export.platform.gettransactionssoapstub.model.req",
                "com.payment.export.platform.gettransactionssoapstub.model.rpy"
        );
        return marshaller;
    }
}

