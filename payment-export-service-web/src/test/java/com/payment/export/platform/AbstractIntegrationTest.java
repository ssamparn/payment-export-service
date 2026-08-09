package com.payment.export.platform;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles("integrationtest")
public abstract class AbstractIntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("payment_export_service_test")
            .withUsername("postgres")
            .withPassword("password");

    static {
        POSTGRESQL.start();
    }

    @DynamicPropertySource
    protected static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
        registry.add("spring.flyway.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRESQL::getUsername);
        registry.add("spring.flyway.password", POSTGRESQL::getPassword);
    }
}

