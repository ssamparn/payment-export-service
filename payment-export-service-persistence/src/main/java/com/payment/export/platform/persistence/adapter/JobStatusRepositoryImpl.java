package com.payment.export.platform.persistence.adapter;

import com.payment.export.platform.domain.dto.web.request.Account;
import com.payment.export.platform.domain.dto.web.response.JobStatusItemResponse;
import com.payment.export.platform.domain.ports.output.repository.JobStatusRepository;
import com.payment.export.platform.persistence.entity.JobStatus;
import com.payment.export.platform.persistence.mapper.JobDataAccessMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JobStatusRepositoryImpl implements JobStatusRepository {

    private static final String FIND_JOB_STATUS_SQL = """
            select j.job_id,
                   j.payment_type,
                   j.accounts,
                   j.status,
                   j.created_at,
                   j.updated_at
            from job j
            where j.customer_agreement_id = :customerAgreementId
              and j.job_id = :jobId
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JobDataAccessMapper jobDataAccessMapper;

    public JobStatusRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate,
                                   JobDataAccessMapper jobDataAccessMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jobDataAccessMapper = jobDataAccessMapper;
    }

    @Override
    public Optional<JobStatusItemResponse> findByCustomerAgreementIdAndJobId(String customerAgreementId, UUID jobId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerAgreementId", customerAgreementId)
                .addValue("jobId", jobId);

        List<JobStatusItemResponse> items = jdbcTemplate.query(FIND_JOB_STATUS_SQL, params, (rs, rowNum) -> mapRow(rs));
        return items.stream().findFirst();
    }

    private JobStatusItemResponse mapRow(ResultSet rs) throws SQLException {
        String[] accountReferences = extractAccountReferences(rs);
        List<Account> accounts = mapAccounts(accountReferences);

        JobStatus status = JobStatus.valueOf(rs.getString("status"));
        return new JobStatusItemResponse(
                UUID.fromString(rs.getString("job_id")),
                com.payment.export.platform.domain.dto.PaymentType.valueOf(rs.getString("payment_type")),
                accounts,
                jobDataAccessMapper.toBusinessStatus(status),
                rs.getObject("created_at", java.time.OffsetDateTime.class),
                rs.getObject("updated_at", java.time.OffsetDateTime.class)
        );
    }

    private String[] extractAccountReferences(ResultSet rs) throws SQLException {
        Array sqlArray = rs.getArray("accounts");
        if (sqlArray == null || sqlArray.getArray() == null) {
            return new String[0];
        }

        return (String[]) sqlArray.getArray();
    }

    private List<Account> mapAccounts(String[] accountReferences) {
        List<Account> mapped = new ArrayList<>();
        for (String accountReference : accountReferences) {
            if (accountReference == null || accountReference.isBlank()) {
                continue;
            }

            String[] parts = accountReference.split(":", 2);
            if (parts.length != 2) {
                continue;
            }

            String iban = parts[0].trim();
            String currencyCode = parts[1].trim().toUpperCase();
            if (iban.isBlank() || currencyCode.isBlank()) {
                continue;
            }
            mapped.add(new Account(iban, currencyCode));
        }
        return mapped;
    }

}

