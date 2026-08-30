package com.payment.export.platform.getbatchsoapstub.config;

import com.payment.export.platform.getbatchsoapstub.GetBatchSoapConstants;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

@EnableWs
@Configuration
public class GetBatchSoapWebServiceConfig {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, GetBatchSoapConstants.LOCATION_URI + "/*");
    }

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan(
                "com.payment.export.platform.getbatchsoapstub.model",
                "com.payment.export.platform.getbatchsoapstub.model.req",
                "com.payment.export.platform.getbatchsoapstub.model.rpy"
        );
        return marshaller;
    }
}

