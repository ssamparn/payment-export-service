package com.payment.export.platform.domain.dto.web.validator;

import com.payment.export.platform.domain.dto.web.request.CreateJobRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.collections4.CollectionUtils;

public class ValidCreateJobRequestValidator implements ConstraintValidator<ValidCreateJobRequest, CreateJobRequest> {

    @Override
    public boolean isValid(CreateJobRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (value.dateFrom() != null && value.dateTo() != null && value.dateFrom().isAfter(value.dateTo())) {
            valid = false;
            context.buildConstraintViolationWithTemplate("dateFrom must be less than or equal to dateTo")
                    .addPropertyNode("dateFrom")
                    .addConstraintViolation();
        }

        if (CollectionUtils.isEmpty(value.accounts())) {
            valid = false;
            context.buildConstraintViolationWithTemplate("accounts list must not be empty")
                    .addPropertyNode("accounts")
                    .addConstraintViolation();
        }

        return valid;
    }
}

