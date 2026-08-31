package com.payment.export.platform.persistence.adapter;

import com.payment.export.platform.domain.dto.web.request.Account;
import com.payment.export.platform.domain.dto.web.request.JobStatusQueryRequest;
import com.payment.export.platform.domain.dto.web.response.BusinessStatus;
import com.payment.export.platform.domain.dto.web.response.JobStatusItemResponse;
import com.payment.export.platform.domain.dto.web.response.JobStatusPageResponse;
import com.payment.export.platform.domain.ports.output.repository.JobStatusRepository;
import com.payment.export.platform.persistence.entity.JobStatus;
import com.payment.export.platform.persistence.mapper.JobDataAccessMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Component
public class JobStatusRepositoryImpl implements JobStatusRepository {

    private static final EnumSet<JobStatus> IN_PROGRESS_STATUSES = EnumSet.of(
            JobStatus.FETCHING_BATCHES,
            JobStatus.BATCHES_FETCHED,
            JobStatus.BATCHES_FETCH_FAILED,
            JobStatus.FETCHING_TRANSACTIONS,
            JobStatus.TRANSACTIONS_FETCHED,
            JobStatus.GENERATING_CSV_LINK,
            JobStatus.GENERATING_CSV_FAILED
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JobDataAccessMapper jobDataAccessMapper;

    public JobStatusRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate,
                                   JobDataAccessMapper jobDataAccessMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jobDataAccessMapper = jobDataAccessMapper;
    }

    @Override
    public JobStatusPageResponse findByCustomerAgreementId(String customerAgreementId, JobStatusQueryRequest request) {
        QueryParts queryParts = buildQuery(customerAgreementId, request);

        long totalElements = queryTotalElements(queryParts.countSql(), queryParts.params());
        List<JobStatusItemResponse> content = queryItems(queryParts.dataSql(), queryParts.params());

        int totalPages = request.size() <= 0
                ? 0
                : (int) Math.ceil((double) totalElements / request.size());

        return new JobStatusPageResponse(content, request.page(), request.size(), totalElements, totalPages);
    }

    private QueryParts buildQuery(String customerAgreementId, JobStatusQueryRequest request) {
        StringBuilder whereClause = new StringBuilder(" where j.customer_agreement_id = :customerAgreementId");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerAgreementId", customerAgreementId)
                .addValue("limit", request.size())
                .addValue("offset", request.page() * request.size());

        if (request.fromDate() != null) {
            whereClause.append(" and j.date_from >= :fromDate");
            params.addValue("fromDate", request.fromDate());
        }
        if (request.toDate() != null) {
            whereClause.append(" and j.date_to <= :toDate");
            params.addValue("toDate", request.toDate());
        }
        if (request.paymentType() != null) {
            whereClause.append(" and j.payment_type = :paymentType");
            params.addValue("paymentType", request.paymentType().name());
        }

        addAccountFilter(request, whereClause, params);
        addBusinessStatusFilter(request.status(), whereClause, params);

        String orderBy = " order by " + resolveSortColumn(request.sortBy()) + " " + resolveSortDirection(request.sortDirection());

        String countSql = "select count(*) from job j" + whereClause;
        String dataSql = """
                select j.job_id,
                       j.date_from,
                       j.date_to,
                       j.payment_type,
                       j.accounts,
                       j.status,
                       j.csv_file_location,
                       j.created_at,
                       j.updated_at
                from job j
                """ + whereClause + orderBy + " limit :limit offset :offset";

        return new QueryParts(countSql, dataSql, params);
    }

    private void addBusinessStatusFilter(BusinessStatus status,
                                         StringBuilder whereClause,
                                         MapSqlParameterSource params) {
        if (status == null) {
            return;
        }

        List<String> matchingStatuses = switch (status) {
            case CREATED -> List.of(JobStatus.CREATED.name());
            case IN_PROGRESS -> IN_PROGRESS_STATUSES.stream().map(Enum::name).toList();
            case COMPLETED -> List.of(JobStatus.CAN_BE_DOWNLOADED.name());
            case FAILED -> List.of(JobStatus.FAILED.name());
        };

        whereClause.append(" and j.status in (:statuses)");
        params.addValue("statuses", matchingStatuses);
    }

    private void addAccountFilter(JobStatusQueryRequest request,
                                  StringBuilder whereClause,
                                  MapSqlParameterSource params) {
        String iban = request.iban();
        String currencyCode = request.currencyCode();

        if (iban != null && currencyCode != null) {
            whereClause.append(" and exists (select 1 from unnest(j.accounts) acc where acc = :accountRef)");
            params.addValue("accountRef", iban + ":" + currencyCode);
            return;
        }

        if (iban != null) {
            whereClause.append(" and exists (select 1 from unnest(j.accounts) acc where split_part(acc, ':', 1) = :iban)");
            params.addValue("iban", iban);
        }

        if (currencyCode != null) {
            whereClause.append(" and exists (select 1 from unnest(j.accounts) acc where upper(split_part(acc, ':', 2)) = :currencyCode)");
            params.addValue("currencyCode", currencyCode);
        }
    }

    private long queryTotalElements(String countSql, MapSqlParameterSource params) {
        Long count = jdbcTemplate.queryForObject(countSql, params, Long.class);
        return count == null ? 0L : count;
    }

    private List<JobStatusItemResponse> queryItems(String dataSql, MapSqlParameterSource params) {
        return jdbcTemplate.query(dataSql, params, (rs, rowNum) -> mapRow(rs));
    }

    private JobStatusItemResponse mapRow(ResultSet rs) throws SQLException {
        String[] accountReferences = extractAccountReferences(rs);
        List<Account> accounts = mapAccounts(accountReferences);

        JobStatus status = JobStatus.valueOf(rs.getString("status"));
        return new JobStatusItemResponse(
                UUID.fromString(rs.getString("job_id")),
                rs.getObject("date_from", LocalDate.class),
                rs.getObject("date_to", LocalDate.class),
                com.payment.export.platform.domain.dto.PaymentType.valueOf(rs.getString("payment_type")),
                accounts,
                jobDataAccessMapper.toBusinessStatus(status),
                rs.getString("csv_file_location"),
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

    private String resolveSortColumn(String sortBy) {
        if (sortBy == null) {
            return "j.created_at";
        }

        return switch (sortBy) {
            case "fromDate" -> "j.date_from";
            case "toDate" -> "j.date_to";
            case "paymentType" -> "j.payment_type";
            case "account" -> "coalesce(j.accounts[1], '')";
            case "status" -> "j.status";
            case "updatedAt" -> "j.updated_at";
            case "createdAt" -> "j.created_at";
            default -> "j.created_at";
        };
    }

    private String resolveSortDirection(String sortDirection) {
        if (sortDirection == null) {
            return "DESC";
        }

        return "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
    }

    private record QueryParts(String countSql, String dataSql, MapSqlParameterSource params) {
    }
}

