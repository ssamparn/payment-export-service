package com.payment.export.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.payment.export.platform")
public class PaymentExportServiceWebApplication {

    static void main(String[] args) {
        SpringApplication.run(PaymentExportServiceWebApplication.class, args);
    }
}
