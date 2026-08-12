package com.payment.export.platform.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

public record CreateJobRequest(@NotNull LocalDate dateFrom,
                               @NotNull LocalDate dateTo,
                               @NotNull @Pattern(regexp = "^(CT|DD)$", message = "type must be CT or DD") String type,
                               @NotEmpty @Valid List<AccountDto> accounts) {

    @AssertTrue(message = "dateFrom must be before or equal to dateTo")
    public boolean isDateRangeValid() {
        if (dateFrom == null || dateTo == null) {
            return true;
        }
        return !dateFrom.isAfter(dateTo);
    }
}
